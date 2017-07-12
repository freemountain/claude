import * as Docker from "dockerode";
import { Labels } from "../utils";
import { IDockerEvent } from "./IDockerEvent";
import IDockerRunOptions from "./IDockerRunOptions";
import {ILogger} from "./ILogger";

export type ContainerHandler = (container: Docker.ContainerInspectInfo[], event: IDockerEvent) => Promise<void> | void;
export type ContainerFilter = (event: IDockerEvent) => boolean;

export interface IContainerController {
    start(): Promise<void>;
    assertStarted(options: IDockerRunOptions): Promise<Docker.Container>;
    assertRemoved(labels: Labels): Promise<void>;
    onContainerChange(
        handler: ContainerHandler,
        labels?: Labels,
        events?: string[],
    ): void;
    inspect(labels: Labels): Promise<Docker.ContainerInspectInfo[]>;
}
