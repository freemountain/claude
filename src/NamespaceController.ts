import DockerEvents = require("docker-events");
import * as Docker from "dockerode";
import { copy, emptyDir, mkdirp } from "fs-extra";
import {
    defaults,
    flatten,
    fromPairs,
    has,
    values,
} from "lodash";
import { join } from "path";
import * as R from "ramda";
import { v4 } from "uuid";
import IDeployment from "./models/IDeployment";
import IDeploymentPodOptions from "./models/IDeploymentPodOptions";
import IDockerRunOptions from "./models/IDockerRunOptions";
import { IResourceController, ResourceControllerFactory } from "./models/IResourceController";

import IDockerNetworkInfo from "./models/IDockerNetworkInfo";

import { ContainerHandler, INameSpaceController } from "./models/INameSpaceController";
import RootLogger from "./RootLogger";

interface IDict<T> { [name: string]: T; }

type LabelsAA = IDict<string | number | boolean | null>;

interface ILabeld {
    Labels: IDict<string>;
}

export default class Application implements INameSpaceController {
    private network: any;
    private events: DockerEvents;
    private changeHandler: ContainerHandler[];
    private logger: RootLogger;
    private resourceControllers: IResourceController[];
    constructor(
        public docker: Docker,
        public namespace: string,
        public tld: string,
        controller: ResourceControllerFactory[],
    ) {
        this.events = new DockerEvents({ docker });
        this.changeHandler = [];
        this.events.on("_message", (msg) => this.handler(msg));
        this.logger = new RootLogger(this.constructor.name);
        this.resourceControllers = controller.map((f) => f(this));
    }

    public onContainerChange(handler: ContainerHandler) {
        this.changeHandler.push(handler);
    }
    public applicationFile(...name: string[]) {
        return join(__dirname, "..", ...name);
    }
    public dataFile(...name: string[]) {
        return join("/var/claude", ...name);
    }

    public getDomain(t: string[] | string = []) {
        const token = flatten([t]).concat(this.namespace).concat(this.tld);

        return token.join(".");
    }

    public async clean(): Promise<void> {
        const remove = async (containerInfo: Docker.ContainerInfo) => {
            this.logger.info("removing" + containerInfo.Id.slice(0, 8) + " " + containerInfo.Names);
            const container = this.docker.getContainer(containerInfo.Id);
            await container.stop();
            return container.remove().catch(() => null);
        };

        const containers = await this.filterRessources(this.docker.listContainers());

        await Promise.all(containers.map(remove));
        await emptyDir("/var/claude");
    }

    public async stop(): Promise<void> {
        this.events.stop();
        await Promise.all(this.resourceControllers.map((ctrl) => ctrl.stop()));
    }

    public getLogger(o: any) {
        return this.logger.getLogger(o);
    }

    public async start(): Promise<void> {
        await this.clean();

        this.events.start();

        await mkdirp("/var/claude/etc-volumes");
        await mkdirp("/var/claude/gen-volumes");

        const network = await this.assertNetwork(this.namespace);

        await Promise.all(this.resourceControllers.map((ctrl) => ctrl.start()));
    }

    public filterRessources<T extends ILabeld>(ressources: Promise<T[]>, labels: LabelsAA = {}): Promise<T[]> {
        const createLabelFilter = <T extends ILabeld>(name: string) => {
            const value = labels[name];
            const labelName = `claude.${name}`;

            if (value === null) {
                return (item: T) => !has(item.Labels, labelName);
            }

            return (item: T) => item.Labels[labelName] === `${labels[name]}`;
        };

        const filter = <T extends ILabeld>(item: T) => Object
            .keys(labels)
            .map(createLabelFilter).map((f) => f(item)).every((r) => r);

        return Promise.resolve(ressources).then((collection) => collection
            .filter((item) => has(item, "Labels"))
            .filter((item) => item.Labels[`claude.namespace`] === this.namespace)
            .filter(filter),
        );
    }

    public async assertContainerRemoved(filter: LabelsAA) {
        const containers = await this.filterRessources(this.docker.listContainers(), filter);

        return Promise.all(containers.map((container) => this.removeContainer(container.Id)));
    }

    public async removeContainer(id: string): Promise<void> {
        const container = this.docker.getContainer(id);
        try {
            await container.stop();
            await container.remove();
        } catch (e) {
            this.logger.warn(`could not remove container ${id.slice(0, 8)}`);
        }
    }

    public async assertNetwork(name: string): Promise<IDockerNetworkInfo> {
        let network: any;
        const labels = { nid: name };
        const networks = await this.filterRessources(this.docker.listNetworks(), labels);

        if (networks.length > 1) {
            throw new Error(`found more than one matching network`);
        }

        network = networks[0];

        if (!network) {
            await this.docker.createNetwork({
                Internal: true,
                Labels: this.createLabels(labels),
                Name: `claude-${name}`,
            });
            network = (await this.filterRessources(this.docker.listNetworks(), labels))[0];
            this.logger.info(`created network ${name}`, network);
        }

        if (!network) {
            throw new Error(`could not create network: ${name}`);
        }

        return network;
    }

    public async assertDeployment(name: string, deployment: IDeployment) {
        const network = this.assertNetwork(`claude-${this.namespace}-${name}`);
        this.assertContainerRemoved({
            did: name,
        });

        await Promise.all(Object.keys(deployment.pods).map(async (podName) => {
            const pod = deployment.pods[podName];

            let options: IDockerRunOptions = {
                Cmd: pod.cmd,
                Env: pod.env,
                ExposedPorts: {},
                Hostconfig: {
                    AutoRemove: true,
                    Binds: [],
                    PortBindings: {},
                },
                Image: pod.image,
                Labels: {},
                Names: [podName],
                Volumes: {},
            };

            options = await this.resourceControllers.reduce((prev, ctrl) => {
                return prev.then((opts) => ctrl.onCreateContainerConfig(deployment, podName, opts));
            }, Promise.resolve(options));

            this.run(options);
        }));
    }

    public async run(options: IDockerRunOptions): Promise<Docker.Container> {
        const cid = v4();
        const predefLabels = {
            "claude.cid": cid,
            "claude.clean": "true",
            "claude.namespace": this.namespace,
        };

        Object.assign(options.Labels, predefLabels);

        const getId = this.awaitStart(cid);
        this.docker.run(options.Image, options.Cmd, this.logger.getContainerLogStream("nginx-proxy"), options);

        const id = await getId;
        this.logger.info("create container", options);
        return this.docker.getContainer(id);
    }

    private createLabels(labels: LabelsAA): IDict<string> {
        const pairs = Object.keys(labels)
            .map((label) => [label, labels[label]])
            .concat([["namespace", this.namespace]])
            .map(([key, value]) => [`claude.${key}`, `${value}`]);

        return fromPairs(pairs);
    }

    private awaitStart(cid: string): Promise<string> {
        let handler: R.Arity1Fn;

        return new Promise((resolve, reject) => {
            handler = (msg: any) => {
                if (!msg.Actor.Attributes["claude.cid"]) {
                    return;
                }
                this.events.removeListener("create", handler);
                resolve(msg.id);
            };
            this.events.on("create", handler);
        }).catch((error) => {
            this.events.removeListener("create", handler);
            return Promise.reject(error);
        });
    }

    private async handler(msg: DockerEvents.IDockerEvent) {
        const actions = ["create", "start", "stop", "die"];
        if (msg.Actor.Attributes["claude.namespace"] !== this.namespace || actions.indexOf(msg.Action) === -1) {
            return;
        }
        const containerList = await this.filterRessources(this.docker.listContainers());
        const container = await Promise.all(containerList.map((info) => this.docker.getContainer(info.Id).inspect()));

        return Promise.all(this.changeHandler.map((h) => h(container)));
    }
}
