import IDeployment from "./IDeployment";

export interface IDeploymentController {
    start(): Promise<void>;
    assertDeployment(name: string, deployment: IDeployment): Promise<void>;
    cleanDeployment(name: string): Promise<void>;
}

export default IDeploymentController;