import * as Docker from "dockerode";
import { copy, remove, writeFile } from "fs-extra";
import { inject, injectable } from "inversify";
import BaseController from "../../BaseController";
import { IContainerController } from "./../../models/IContainerController";
import IDeployment from "./../../models/IDeployment";
import IDockerRunOptions from "./../../models/IDockerRunOptions";
import {ILogger, LoggerFactory} from "./../../models/ILogger";
import { IResourceController, IResourceControllerArg } from "./../../models/IResourceController";
import ISettings from "./../../models/ISettings";
import { IValidatonError, IValidator } from "./../../models/IValidator";

import IProxyDeploymentOptions from "./IProxyDeploymentOptions";

const configTemplate = (container: Docker.ContainerInspectInfo) => {
    const ip = container.NetworkSettings.IPAddress;
    const domain = container.Config.Labels["claude.proxy.domain"];
    const port = container.Config.Labels["claude.proxy.port"];

    return `
upstream ${domain} {
			server ${ip}:${port};
}

server {
	server_name ${domain};
	listen 80;
	access_log /dev/stderr vhost;
	location / {
		proxy_pass http://${domain};
	}
}
`;
};

@injectable()
export default class ProxyController extends BaseController implements IResourceController {
    public name = "proxy";
    private started: boolean;
    private usedDomains: Set<string>;
    private proxyContainer: Docker.Container;

    public constructor(
        @inject("ContainerController") private container: IContainerController,
        @inject("Validator") private validator: IValidator,
        @inject("getLogger") getLogger: LoggerFactory,
        @inject("Settings") settings: ISettings,
    ) {
        super(settings.namespace, getLogger(ProxyController));
        this.container.onContainerChange((c) => this.handler(c));
        this.started = false;
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
        const proxy: IProxyDeploymentOptions = deployment.resources.proxy;
        if (!proxy) {
            return [];
        }

        const errors = await this.validator.validate("IProxyDeploymentOptions", proxy, "/resources/proxy");

        if (errors.length > 0) {
            return errors;
        }

        if (this.usedDomains.has(proxy.domain)) {
            errors.push({
                message: `domain "${proxy.domain}" is already in use`,
                path: "/domain",
            });
        }

        return errors;
    }

    public async start() {
        const etcVolume = this.dataFile("volumes", "proxy-etc");
        await copy(this.applicationFile("config", "nginx"), etcVolume);
        const mounts: R.Dictionary<string> = {};
        mounts[etcVolume] = "/etc/nginx";

        this.usedDomains = new Set();
        this.proxyContainer = await this.container.assertStarted({
            Cmd: [],
            Env: [],
            ExposedPorts: {
                "80/tcp": {},
            },
            Hostconfig: {
                AutoRemove: true,
                Binds: [`${etcVolume}:/etc/nginx`],
                PortBindings: {
                    "80/tcp": [{ HostIP: "0.0.0.0", HostPort: "80" }],
                },
            },
            Image: "library/nginx:stable-alpine",
            Labels: {},
            Names: ["proxy"],
            Volumes: { "/etc/nginx": {} },
        });
        this.started = true;
    }

    public onCreateContainerConfig(
        deployment: IDeployment,
        deploymenName: string,
        containerName: string,
        config: IDockerRunOptions,
    ): Promise<IDockerRunOptions> {
        const proxy: IProxyDeploymentOptions = deployment.resources.proxy;

        if (proxy && containerName === proxy.target) {
            config.Labels["claude.proxy.domain"] = proxy.domain;
            config.Labels["claude.proxy.port"] = `${proxy.port}`;
        }

        return Promise.resolve(config);
    }

    private async handler(containers: Docker.ContainerInspectInfo[]) {
        const upstream = containers.filter((c) => c.Config.Labels["claude.proxy.domain"]);

        if (!this.started || upstream.length === 0) {
            return;
        }

        this.logger.info(`create proxy routes of ${upstream.length} containers`);

        const config = upstream.map((configTemplate)).join("\n");
        const dest = this.dataFile("volumes", "proxy-etc", "conf.d", "proxy.conf");
        await remove(dest).catch(() => null);
        await writeFile(dest, config);
        this.logger.info(`reloading...`);
        await this.proxyContainer.kill({ signal: "SIGHUP" });
    }
}
