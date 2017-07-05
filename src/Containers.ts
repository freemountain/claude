import DockerEvents = require("docker-events");
import * as Docker from "dockerode";
import * as es from "event-stream";
import * as R from "ramda";
import * as stream from "stream";
import ILogger from "./models/ILogger";
import { ContainerHandler } from "./models/INameSpaceController";

type Labels = R.Dictionary<string | number | boolean | null>;
type Handler = (error?: any, data?: any) => void;

const dispatchEvents = (handler: ContainerHandler[]) => {
    const writable = new stream.Writable({ objectMode: true });

    writable._write = (chunk, enc, next) => Promise
        .all(handler.map((h) => h(chunk)))
        .then(() => {console.log("hu");next();})
        .catch((error) => next(error));

    return writable;
};

export default class Containers {
    private events: DockerEvents;
    private eventSource: stream.Readable;
    private eventDrain: stream.Writable;
    private onChangeHandler: ContainerHandler[];

    constructor(
        private docker: Docker,
        private logger: ILogger,
        private namespace: string,
    ) {
        this.onChangeHandler = [];
        this.events = new DockerEvents({ docker });
        this.eventSource = new stream.PassThrough({ objectMode: true });
        this.eventDrain = dispatchEvents(this.onChangeHandler);
    }

    public start() {
        const actions = ["create", "start", "stop", "die"];
        const filter = this.createFilter({});

        this.events.on("_message", (msg) => this.eventSource.push(msg));

        this.eventSource
            .pipe(es.map(async (data: DockerEvents.IDockerEvent, cb: Handler) => {
                const { Action, id } = data;
                if (actions.indexOf(Action) === -1 || !filter(data.Actor.Attributes)) {
                    return cb();
                }
                const containerInfos = await Promise.all(
                    (await this.docker.listContainers())
                        .filter(({ Labels }) => filter(Labels))
                        .map(({ Id }) => this.docker.getContainer(Id))
                        .map((container) => container.inspect()),
                );

                cb(null, containerInfos);
            }))
            .pipe(this.eventDrain);
    }
    public createFilter(expectedLabels: Labels) {
        return (labels: R.Dictionary<string>) => R.pipe(
            R.keys,
            R.map((name: string) => {
                const value = labels[name];
                const labelName = `claude.${name}`;
                return value === null
                    ? !R.has(labelName)
                    : labels[labelName] === `${expectedLabels[labelName]}`;
            }),
            R.concat([labels[`claude.namespace`] === this.namespace]),
            R.all((isValid: boolean) => isValid),
        )(expectedLabels);
    }

    public onContainerChange(handler: ContainerHandler) {
        this.onChangeHandler.push(handler);
    }

    public awaitContainerEvent(event: DockerEvents.EventType, labels: Labels): Promise<string> {
        let handler: R.Arity1Fn;
        const filter = this.createFilter(labels);

        return new Promise((resolve, reject) => {
            handler = (msg: any) => {
                if (!filter(msg.Actor)) {
                    return;
                }
                this.events.removeListener(event, handler);
                resolve(msg.id);
            };
            this.events.on(event, handler);
        }).catch((error) => {
            this.events.removeListener(event, handler);
            return Promise.reject(error);
        });
    }
}
