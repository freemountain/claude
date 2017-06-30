export type EventType = "create" | "start" | "stop" | "die";

export interface IDockerEvent {
    status: EventType; // create...
    id: string;
    from: string; // image
    Type: string; // container
    Action: EventType;
    Actor: {
        ID: string,
        Attributes: { [name: string]: string; },
    };
    time: number;
    timeNano: number;
}
