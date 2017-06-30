import IPodOptions from "./IDeploymentPodOptions";
import IDeploymentResources from "./IDeploymentResources";

interface IDeployment {
    name?: string;
    resources: IDeploymentResources;
    pods: { [name: string]: IPodOptions };
}

export default IDeployment;
