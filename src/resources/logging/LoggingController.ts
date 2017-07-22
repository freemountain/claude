import * as Docker from "dockerode";
import { copy, remove, writeFile } from "fs-extra";
import { inject, injectable, interfaces } from "inversify";
import BaseController from "../../BaseController";
import { UDPClient } from "../../logging";
import { IContainerController, IResourceController, IResourceControllerArg } from "./../../models/controller";
import { IDockerEvent, IDockerRunOptions } from "./../../models/docker";
import IDeployment from "./../../models/IDeployment";
import ISettings from "./../../models/ISettings";
import { IValidatonError, IValidator } from "./../../models/IValidator";
import { ILogger, LoggerFactory } from "./../../models/logging";

@injectable()
export default class LoggingController extends BaseController implements IResourceController {
    public name = "proxy";
    private logioContainer: Docker.Container;
    private docker: Docker;

    public constructor(
        @inject("ContainerController") private container: IContainerController,
        @inject("Validator") private validator: IValidator,
        @inject("getLogger") getLogger: LoggerFactory,
        @inject("Settings") settings: ISettings,
        @inject("UDPClient") private logio: UDPClient,
        @inject("getDocker") getDocker: interfaces.Factory<Docker>,
    ) {
        super(settings.namespace, getLogger(LoggingController));
        this.docker = getDocker() as Docker;
    }

    public onAssertDeployment() {
        return Promise.resolve();
    }

    public onCleanDeployment() {
        return Promise.resolve();
    }

    public stop() {
        return Promise.resolve();
    }

    public async onVerifyDeployment(name: string, deployment: IDeployment): Promise<IValidatonError[]> {
        return Promise.resolve([]);
    }

    public async start() {
        this.logioContainer = await this.container.assertStarted({
            Cmd: [],
            Env: [],
            ExposedPorts: {
                "8888/tcp": {},
                "9999/udp": {},

            },
            Hostconfig: {
                AutoRemove: true,
                Binds: [],
                PortBindings: {
                    "8888/tcp": [{ HostIP: "0.0.0.0", HostPort: "8888" }],
                    "9999/udp": [{ HostIP: "0.0.0.0", HostPort: "9999" }],
                },
            },
            Image: "freemountain/rtail",
            Labels: {
                "claude.proxy.domain": "logs.claude.dev",
                "claude.proxy.port": "8888",
            },
            Names: ["rtail"],
            Volumes: {},
        });
        setTimeout(() => this.logio.start("192.168.99.100", 9999), 2000);
    }

    public onCreateContainerConfig(
        deployment: IDeployment,
        deploymenName: string,
        containerName: string,
        config: IDockerRunOptions,
    ): Promise<IDockerRunOptions> {
        return Promise.resolve(config);
    }
}
