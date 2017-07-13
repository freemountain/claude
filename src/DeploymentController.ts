import DockerEvents = require("docker-events");
import * as Docker from "dockerode";
import { copy, emptyDir, mkdirp } from "fs-extra";
import { inject, injectable, interfaces } from "inversify";
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
import {
    IContainerController,
    IDeploymentController,
    IResourceController,
    ResourceControllerFactory,
} from "./models/controller";
import { IDockerNetworkInfo, IDockerRunOptions } from "./models/docker";
import IDeployment from "./models/IDeployment";
import { ILogger, LoggerFactory } from "./models/ILogger";
import IService from "./models/IService";
import ISettings from "./models/ISettings";
import { IValidatonError } from "./models/IValidator";

import Validator from "./Validator";

import ContainerController from "./ContainerController";

import { createLabelFilter, createLabels, Labels, parseRepository } from "./utils";

@injectable()
export default class DeploymentController implements IDeploymentController {
    private network: any;
    private logger: ILogger;
    private resourceControllers: IResourceController[];
    private namespace: string;
    private docker: Docker;

    constructor(
        @inject("getDocker") getDocker: interfaces.Factory<Docker>,
        @inject("Validator") private validator: Validator,
        @inject("ContainerController") private containers: IContainerController,
        @inject("getLogger") getLogger: LoggerFactory,
        @inject("Settings") settings: ISettings,
        @inject("getResourceController") getResourceController: interfaces.Factory<IResourceController[]>,
    ) {
        this.docker = getDocker() as Docker;
        this.namespace = settings.namespace;
        this.logger = getLogger(this);
        this.resourceControllers = (getResourceController() as IResourceController[]);
    }

    public async stop(): Promise<void> {
        await Promise.all(this.resourceControllers.map((ctrl) => ctrl.stop()));
    }

    public async start(): Promise<void> {
        await this.validator.load();
        await this.containers.start();
        await this.clean();

        await mkdirp("/var/claude/etc-volumes");
        await mkdirp("/var/claude/gen-volumes");
        const network = await this.assertNetwork(this.namespace);

        await Promise.all(this.resourceControllers.map((ctrl) => ctrl.start()));
    }

    public async cleanDeployment(name: string) {
        await this.resourceControllers.map((ctrl) => ctrl.onCleanDeployment(name));
    }

    public async assertDeployment(name: string, deployment: IDeployment) {
        const network = this.assertNetwork(`claude-${this.namespace}-${name}`);

        await this.containers.assertRemoved({
            did: name,
        });

        await this.cleanDeployment(name);

        const allErrors = flatten(await Promise.all(
            this.resourceControllers.map((ctrl) => ctrl.onVerifyDeployment(name, deployment)),
        ));

        if (allErrors.length > 0) {
            throw allErrors;
        }

        await Promise.all(Object.keys(deployment.services).map(async (serviceName) => {
            const service = deployment.services[serviceName];

            let options: IDockerRunOptions = {
                Cmd: service.cmd,
                Env: [],
                ExposedPorts: {},
                Hostconfig: {
                    AutoRemove: true,
                    Binds: [],
                    PortBindings: {},
                },
                Image: service.image,
                Labels: {},
                Names: [serviceName],
                Volumes: {},
            };

            options = await this.resourceControllers.reduce((prev, ctrl) => {
                return prev.then((opts) => ctrl.onCreateContainerConfig(deployment, name, serviceName, opts));
            }, Promise.resolve(options));

            return this.containers.assertStarted(options);
        }));
    }

    private async assertNetwork(name: string): Promise<IDockerNetworkInfo> {
        let network: any;
        const labels = { nid: name, namespace: this.namespace };
        const filter = createLabelFilter(labels);
        const networks = (await this.docker.listNetworks()).filter(({ Labels }) => filter(Labels));
        if (networks.length > 1) {
            throw new Error(`found more than one matching network`);
        }

        network = networks[0];

        if (!network) {
            await this.docker.createNetwork({
                Internal: true,
                Labels: createLabels(labels),
                Name: `claude-${name}`,
            });
            network = (await this.docker.listNetworks()).filter(({ Labels }) => filter(Labels))[0];
        }

        if (!network) {
            throw new Error(`could not create network: ${name}`);
        }

        return network;
    }

    private async clean(): Promise<void> {
        await this.containers.assertRemoved({
            ctrl: null,
            namespace: this.namespace,
        });

        await emptyDir("/var/claude");
    }
}
