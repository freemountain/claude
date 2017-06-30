import { Dictionary } from "ramda";

interface IDeploymentPodOptions {
        image: string;
        readonly: boolean;
        cmd: string[];
        env: string[];
        memoryLimit: number;
        volumes: Dictionary<string>;
    }

export default IDeploymentPodOptions;
