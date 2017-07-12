import { IContainerController } from "./IContainerController";
import IDeployment from "./IDeployment";
import IDockerRunOptions from "./IDockerRunOptions";
import IFactory from "./IFactory";
import {ILogger} from "./ILogger";
import { IValidatonError, IValidator } from "./IValidator";

export interface IResourceControllerArg {
    container: IContainerController;
    validator: IValidator;
    logger: ILogger;
    namespace: string;
}

export type ResourceControllerFactory = IFactory<IResourceController, IResourceControllerArg>;

export interface IResourceController {
    name: string;
    start(): Promise<void>;
    stop(): Promise<void>;
    onVerifyDeployment(name: string, deployment: IDeployment): Promise<IValidatonError[]>;
    onAssertDeployment(name: string, deployment: IDeployment): Promise<void>;
    onCleanDeployment(name: string): Promise<void>;
    onCreateContainerConfig(
        deployment: IDeployment,
        deploymenName: string,
        containerName: string,
        config: IDockerRunOptions,
    ): Promise<IDockerRunOptions>;
}
