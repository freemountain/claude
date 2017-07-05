import IService from "./IService";

interface IDeployment {
    name?: string;
    resources: { [name: string]: any};
    services: { [name: string]: IService };
}

export default IDeployment;
