import * as Docker from "dockerode";
import { copy, emptyDir, mkdirp, remove, writeFile } from "fs-extra";
import IDeployment from "./../../models/IDeployment";
import IDockerRunOptions from "./../../models/IDockerRunOptions";
import ILogger from "./../../models/ILogger";
import { INameSpaceController } from "./../../models/INameSpaceController";
import { IResourceController, IResourceControllerArg } from "./../../models/IResourceController";
import { IValidatonError, IValidator } from "./../../models/IValidator";

import IProxyDeploymentOptions from "./IProxyDeploymentOptions";

import DockerEvents = require("docker-events");

import * as R from "ramda";

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

export default class ProxyController implements IResourceController {
    public static create({ ctrl, validator }: IResourceControllerArg) {
        return new ProxyController(ctrl, validator);
    }

    public name = "proxy";
    private container: Docker.Container;
    private logger: ILogger;
    private started: boolean;
    private usedDomains: Set<string>;

    public constructor(
        private ctrl: INameSpaceController,
        private validator: IValidator,
    ) {
        this.logger = ctrl.getLogger(this);
        this.ctrl.onContainerChange((c) => this.handler(c));
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
        const etcVolume = this.ctrl.dataFile("volumes", "proxy-etc");
        await copy(this.ctrl.applicationFile("config", "nginx"), etcVolume);
        const mounts: R.Dictionary<string> = {};
        mounts[etcVolume] = "/etc/nginx";

        this.usedDomains = new Set();
        this.container = await this.ctrl.run({
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
            Image: "library/nginx",
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
        const dest = this.ctrl.dataFile("volumes", "proxy-etc", "conf.d", "proxy.conf");
        await remove(dest).catch(() => null);
        await writeFile(dest, config);
        this.logger.info(`reloading...`);
        await this.container.kill({ signal: "SIGHUP" });
    }
}
