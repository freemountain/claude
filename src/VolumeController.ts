import * as Docker from "dockerode";
import { copy, emptyDir, mkdirp, remove, writeFile } from "fs-extra";
import IDeployment from "./models/IDeployment";
import IDeploymentPodOptions from "./models/IDeploymentPodOptions";

import IDockerRunOptions from "./models/IDockerRunOptions";
import ILogger from "./models/ILogger";
import { INameSpaceController } from "./models/INameSpaceController";
import { IResourceController } from "./models/IResourceController";

import DockerEvents = require("docker-events");

import * as R from "ramda";

export default class VolumeController implements IResourceController {
    public static create(ctrl: INameSpaceController) {
        return new VolumeController(ctrl);
    }

    public name = "volumes";
    private logger: ILogger;

    public constructor(
        private ctrl: INameSpaceController,
    ) {
        this.logger = ctrl.getLogger(this);
    }

    public async onAssertDeployment(name: string, deployment: IDeployment) {
        const volumes = deployment.resources.volumes;

        if (!volumes) {
            return Promise.resolve();
        }

        const p = R.pipe(
            R.values,
            R.map((pod: IDeploymentPodOptions) => R.keys(pod.volumes)),
        )(deployment.pods);

        await Promise.all(volumes
            .map((v) => this.ctrl.dataFile("volumes", name, v))
            .map((v) => mkdirp(v)));
    }

    public onCleanDeployment(name: string) {
        return Promise.resolve();
    }

    public stop() {
        return Promise.resolve();
    }

    public onVerifyDeployment(name: string, deployment: IDeployment): Promise<string[]> {
        return Promise.resolve([]);
    }

    public async start() {
        await mkdirp(this.ctrl.dataFile("volumes"));
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
        //
    }
}
