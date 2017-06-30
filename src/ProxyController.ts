import * as Docker from "dockerode";
import { copy, emptyDir, mkdirp, remove, writeFile } from "fs-extra";
import IDeployment from "./models/IDeployment";
import IDockerRunOptions from "./models/IDockerRunOptions";
import ILogger from "./models/ILogger";
import { INameSpaceController } from "./models/INameSpaceController";
import { IResourceController } from "./models/IResourceController";

import DockerEvents = require("docker-events");

import * as R from "ramda";

const configTemplate = (container: Docker.ContainerInspectInfo) => {
    const ip = container.NetworkSettings.IPAddress;
    const domain = container.Config.Labels["claude.proxy.domain"];
    const port = container.Config.Labels["claude.proxy.port"];

    return `
upstream ${domain} {
			## Can be connect with "bridge" network
			# eager_mestorf
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
    public static create(ctrl: INameSpaceController) {
        return new ProxyController(ctrl);
    }

    public name = "proxy";
    private container: Docker.Container;
    private logger: ILogger;
    private started: boolean;
    private usedDomains: Set<string>;

    public constructor(
        private ctrl: INameSpaceController,
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

    public onVerifyDeployment(name: string, deployment: IDeployment): Promise<string[]> {
        const errors: string[] = [];
        const http = deployment.resources.http;

        if (!http) {
            return Promise.resolve(errors);
        }

        if (this.usedDomains.has(http.domain)) {
            errors.push(`domain "${http.domain}" is already in use`);
        }

        if (this.usedDomains.has(http.domain)) {
            errors.push(`domain "${http.domain}" is already in use`);
        }

        return Promise.resolve(errors);
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
            Image: "nginx",
            Labels: {},
            Names: ["proxy"],
            Volumes: { "/etc/nginx": {} },
        });
        this.started = true;
    }

    public onCreateContainerConfig(
        deployment: IDeployment,
        containerName: string,
        config: IDockerRunOptions,
    ): Promise<IDockerRunOptions> {
        const http = deployment.resources.http;

        if (http && containerName === http.pod) {
            config.Labels["claude.proxy.domain"] = http.domain;
            config.Labels["claude.proxy.port"] = `${http.port}`;
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
        this.logger.info(`create proxy config: ${config}. reloading...`);
        await this.container.kill({ signal: "SIGHUP" });
    }
}
