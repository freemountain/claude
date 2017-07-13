import * as Docker from "dockerode";
import * as es from "event-stream";
import { inject, injectable, interfaces } from "inversify";
import { get } from "lodash";
import * as R from "ramda";
import * as stream from "stream";
import { v4 } from "uuid";

import { ContainerFilter, ContainerHandler, IContainerController } from "./models/controller";
import { DockerEventType, IDockerEvent, IDockerRunOptions } from "./models/docker";
import { ILogger, LoggerFactory } from "./models/ILogger";
import ISettings from "./models/ISettings";
import { createLabelFilter, Labels, parseRepository } from "./utils";

type Handler = (error?: any, data?: any) => void;
type ContainerHandlers = Array<{ handler: ContainerHandler, filter: ContainerFilter }>;

@injectable()
export default class ContainerController implements IContainerController {
    private events: stream.Readable;
    private onChangeHandler: ContainerHandlers;
    private logger: ILogger;
    private docker: Docker;

    constructor(
        @inject("getDocker") getDocker: interfaces.Factory<Docker>,
        @inject("getLogger") getLogger: LoggerFactory,
        @inject("Settings") private settings: ISettings,
    ) {
        this.docker = getDocker() as Docker;
        this.logger = getLogger(this);
        this.onChangeHandler = [];
        this.events = new stream.PassThrough({ objectMode: true });

        this.events.pipe(es.map(async (event: IDockerEvent, cb: Handler) => {
            const { Action, id } = event;
            const handlers = this.onChangeHandler.filter(({ filter }) => filter(event));
            if (handlers.length === 0) {
                return cb();
            }

            const containerInfos = await this.inspect();
            await Promise.all(handlers.map(({ handler }) => handler(containerInfos, event)));
            cb();
        }));
    }

    public async start() {
        const actions = ["create", "start", "stop", "die"];
        const rawDockerEvents = await this.docker.getEvents();
        const labelFilter = createLabelFilter({
            namespace: this.settings.namespace,
        });

        rawDockerEvents
            .pipe(es.split("\n"))
            .pipe(es.map((data: string, cb: Handler) => cb(null, JSON.parse(data))))
            .pipe(es.map((data: IDockerEvent, cb: Handler) => {
                const result = data.Type === "container"
                    && actions.indexOf(data.Action) !== -1
                    && labelFilter(data.Actor.Attributes);
                if (result) {
                    return cb(null, data);
                }
                cb();
            }))
            .pipe(this.events as stream.PassThrough);
    }

    public onContainerChange(
        handler: ContainerHandler,
        labels: Labels = {},
        events: string[] = [],
    ) {
        const labelFilter = createLabelFilter(labels, { namespace: this.settings.namespace });
        const eventFilter = (event: string) => events.length === 0 ? true : events.indexOf(event) !== -1;
        const filter = (event: IDockerEvent) => eventFilter(event.Action) && labelFilter(event.Actor.Attributes);

        this.onChangeHandler.push({ handler, filter });
    }

    public removeHandler(h: ContainerHandler) {
        this.onChangeHandler = this.onChangeHandler.filter(({ handler }) => h !== handler);
    }

    public awaitContainerEvent(eventType: DockerEventType, labels: Labels): Promise<IDockerEvent> {
        return new Promise((resolve, reject) => {
            const handler = (container: any, event: IDockerEvent) => {
                this.removeHandler(handler);
                resolve(event);
            };
            this.onContainerChange(handler, labels, [eventType]);
        });
    }

    public async assertStarted(options: IDockerRunOptions): Promise<Docker.Container> {
        const cid = get(options, ["Labels", "claude.cid"], v4());
        const predefLabels = {
            "claude.cid": cid,
            "claude.clean": "true",
            "claude.namespace": this.settings.namespace,
        };

        Object.assign(options.Labels, predefLabels);
        await this.assertImage(options.Image);

        const name = options.Names[0];
        const getId = this.awaitContainerEvent("create", { cid });
        this.logger.info(`create container ${name}`, options);

        this.docker.run(options.Image, options.Cmd, this.logger.getLogStream("container", name), options);

        const { id } = await getId;
        return this.docker.getContainer(id);
    }

    public async assertRemoved(labels: Labels) {
        const filter = createLabelFilter(labels, { namespace: this.settings.namespace });
        const containers = (await this.docker.listContainers()).filter(({ Labels }) => filter(Labels));

        await Promise.all(containers.map(async (containerInfo) => {
            const container = this.docker.getContainer(containerInfo.Id);
            const cid = containerInfo.Labels["claude.cid"];
            const name = containerInfo.Names[0].slice(1);
            this.logger.info(`removing container ${name}`);
            const finished = this.awaitContainerEvent("die", { cid });
            container
                .remove({ force: true })
                .catch((e) => this.logger.warn(`proplems removing container ${name}`, e));

            await finished;
        }));
    }

    public async inspect(labels: Labels = {}) {
        const filter = createLabelFilter(labels, { namespace: this.settings.namespace });

        return Promise.all(
            (await this.docker.listContainers())
                .filter(({ Labels }) => filter(Labels))
                .map(({ Id }) => this.docker.getContainer(Id))
                .map((container) => container.inspect()),
        );
    }

    private async assertImage(image: string | { tag: string, repository: string }) {
        const { tag, repository } = typeof image === "string" ? parseRepository(image) : image;
        const existsImage = async (img: string) => {
            try {
                await this.docker.getImage(img).inspect();
                return true;
            } catch (e) {
                return false;
            }
        };
        const exists = await existsImage(`${repository}:${tag}`);

        if (exists) {
            return;
        }

        this.logger.info(`Pulling ${repository}:${tag}`);
        await new Promise((resolve, reject) => {
            this.docker.pull(`${repository}:${tag}`, {}, (err, stream) => {
                this.docker.modem.followProgress(stream, () => resolve());
            });
        });
    }
}
