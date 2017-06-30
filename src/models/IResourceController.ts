import IDeployment from "./IDeployment";
import IDockerRunOptions from "./IDockerRunOptions";
import IFactory from "./IFactory";
import { INameSpaceController } from "./INamespaceController";
export type ResourceControllerFactory = IFactory<IResourceController, INameSpaceController>;

export interface IResourceController {
    name: string;
    start(): Promise<void>;
    stop(): Promise<void>;
    onVerifyDeployment(name: string, deployment: IDeployment): Promise<string[]>;
    onAssertDeployment(name: string, deployment: IDeployment): Promise<void>;
    onCleanDeployment(name: string): Promise<void>;
    onCreateContainerConfig(
        deployment: IDeployment,
        containerName: string,
        config: IDockerRunOptions,
        ): Promise<IDockerRunOptions>;
}
