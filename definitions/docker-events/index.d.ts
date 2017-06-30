// Type definitions for [docker-events]
// Project: [docker-events]
// Definitions by: [~YOUR NAME~] <[~A URL FOR YOU~]>



declare module "docker-events" {
    import { EventEmitter } from "events";
    import * as Docker from "dockerode";
    export = DockerEvents;

    namespace DockerEvents {
        type EventType = "create" | "start" | "stop" | "die";

        export interface IDockerEvent {
            status: EventType; // create...
            id: string;
            from: string; //image
            Type: string; //container
            Action: EventType;
            Actor: {
                ID: string,
                Attributes: { [name: string]: string; }
            };
            time: number;
            timeNano: number;
        }
    }
     class DockerEvents extends EventEmitter {
        constructor(options: {
            docker: Docker
        });

        public start(): void;
        public stop(): void;

        //public on(name: string, cb: (payload: any) => void): this;

        public on(name: DockerEvents.EventType, cb: (payload: DockerEvents.IDockerEvent) => void): this;
        public on(name: "connect" | "disconnect", cb: () => void): this;
        public on(name: "_message", cb: (payload: DockerEvents.IDockerEvent) => void): this;
    }
}





