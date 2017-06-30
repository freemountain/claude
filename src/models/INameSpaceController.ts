import * as Docker from "dockerode";
import IDockerRunOptions from "./IDockerRunOptions";
import ILogger from "./ILogger";

export type ContainerHandler = (container: Docker.ContainerInspectInfo[]) => Promise<void> | void;

export interface INameSpaceController {
    docker: Docker;
    namespace: string;
    tld: string;
    applicationFile(...name: string[]): string;
    dataFile(...name: string[]): string;
    getDomain(t?: string[] | string): string;
    run(options: IDockerRunOptions): Promise<Docker.Container>;
    onContainerChange(handler: ContainerHandler): void;
    getLogger(o: {}): ILogger;
}
