import * as Docker from "dockerode";
import { copy, emptyDir, mkdirp, remove, writeFile } from "fs-extra";
import { join } from "path";
import IDeployment from "./../../models/IDeployment";
import IService from "./../../models/IService";

import IDockerRunOptions from "./../../models/IDockerRunOptions";
import ILogger from "./../../models/ILogger";
import { INameSpaceController } from "./../../models/INameSpaceController";
import { IResourceController, IResourceControllerArg } from "./../../models/IResourceController";
import { IValidatonError, IValidator } from "./../../models/IValidator";
import IStorageDeploymentOptions from "./IStorageDeploymentOptions";
import IStorageServiceOptions from "./IStorageServiceOptions";

import DockerEvents = require("docker-events");

import * as R from "ramda";

export default class StorageController implements IResourceController {
    public static create({ ctrl, validator }: IResourceControllerArg) {
        return new StorageController(ctrl, validator);
    }

    public name = "volumes";
    private logger: ILogger;

    public constructor(
        private ctrl: INameSpaceController,
        private validator: IValidator,
    ) {
        this.logger = ctrl.getLogger(this);
    }

    public async onAssertDeployment(name: string, deployment: IDeployment) {
        const storage: IStorageDeploymentOptions = deployment.resources.storage;

        if (!storage.volumes) {
            return Promise.resolve();
        }

        await Promise.all(storage.volumes
            .map((v) => this.ctrl.dataFile("volumes", name, v))
            .map((v) => mkdirp(v)));
    }

    public onCleanDeployment(name: string) {
        return Promise.resolve();
    }

    public stop() {
        return Promise.resolve();
    }

    public async onVerifyDeployment(name: string, deployment: IDeployment): Promise<IValidatonError[]> {
        const storage: IStorageDeploymentOptions = deployment.resources.storage;
        if (!storage) {
            return [];
        }

        let errors = await this.validator.validate("IStorageDeploymentOptions", storage, "/resources/storage");
        if (errors.length > 0) {
            return errors;
        }

        for (const serviceName of Object.keys(deployment.services)) {
            const service = deployment.services[serviceName];
            const serviceStorage = service.resources.storage;
            const prefix = `/${serviceName}/resources/storage`;
            if (!serviceStorage) {
                continue;
            }
            const serviceErrors = await this.validator.validate("IStorageServiceOptions", serviceStorage, prefix);
            const missingVolumes: IValidatonError[] = R
                .difference(Object.keys(serviceStorage), storage.volumes)
                .map((volume) => ({
                    message: `could not find volume "${volume}"`,
                    path: prefix,
                }));
            errors = errors.concat(serviceErrors, missingVolumes);
        }

        return errors;
    }

    public async start() {
        await mkdirp(this.ctrl.dataFile("volumes"));
    }

    public onCreateContainerConfig(
        deployment: IDeployment,
        deploymenName: string,
        containerName: string,
        config: IDockerRunOptions,
    ): Promise<IDockerRunOptions> {
        const container = deployment.services[containerName];
        const storage: IStorageServiceOptions = container.resources.storage;
        const getHostPath = (name: string) => this.ctrl.dataFile("volumes", deploymenName, name);
        if (!storage) {
            return Promise.resolve(config);
        }

        const binds = R.pipe(
            R.toPairs,
            R.map(([volume, mount]: [string, string]) => (`${getHostPath(volume)}:${mount}`)),
        )(storage);

        type Pair = R.KeyValuePair<string, {}>;
        const volumes = R.pipe(
            R.values,
            R.map((name) => ([name, {}])),
            (p: Pair[]) => R.fromPairs(p),
        )(storage);

        config.Hostconfig.Binds = config.Hostconfig.Binds.concat(binds);
        Object.assign(config.Volumes, volumes);

        return Promise.resolve(config);
    }

    private async handler(containers: Docker.ContainerInspectInfo[]) {
        //
    }
}