interface IDeploymentResources {
    http?: {
        domain: string;
        port: number;
        pod: string;
    };
    mysql?: {};
    volumes?: string[];
}

export default IDeploymentResources;
