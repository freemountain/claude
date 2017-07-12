export type DockerEventType = "create" | "start" | "stop" | "die";

export interface IDockerEvent {
    status: DockerEventType; // create...
    id: string;
    from: string; // image
    Type: string; // container
    Action: DockerEventType;
    Actor: {
        ID: string,
        Attributes: { [name: string]: string; },
    };
    time: number;
    timeNano: number;
}
