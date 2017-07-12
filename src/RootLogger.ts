import { injectable } from "inversify";
import { Writable } from "stream";
import { inspect } from "util";

import Logger from "./Logger";
import IApplicationLogEvent from "./models/IApplicationLogEvent";
import {ILogger} from "./models/ILogger";

@injectable()
export default class RootLogger implements ILogger {
    constructor(private name: string = "") {}

    public error(message: string, meta?: object) {
        this.handler({
            level: 0,
            levelName: "error",
            message,
            meta,
            source: this.name,
        });
    }

    public warn(message: string, meta?: object) {
        this.handler({
            level: 1,
            levelName: "warn",
            message,
            meta,
            source: this.name,
        });
    }

    public info(message: string, meta?: object) {
        this.handler({
            level: 2,
            levelName: "info",
            message,
            meta,
            source: this.name,
        });
    }

    public verbose(message: string, meta?: object) {
        this.handler({
            level: 3,
            levelName: "verbose",
            message,
            meta,
            source: this.name,
        });
    }

    public debug(message: string, meta?: object) {
        this.handler({
            level: 4,
            levelName: "debug",
            message,
            meta,
            source: this.name,
        });
    }

    public getLogger(o: {} | string): ILogger {
        const name = typeof o === "string" ? o : o.constructor.name;
        return new RootLogger(name);
    }

    public getLogStream(type: string, name: string): Writable {
        let buffer = "";
        const writable = new Writable();

        writable._write = (chunk, encoding, done) => {
            const current: string = chunk.toString();
            for (const char of current) {
                buffer += char;
                if (char === "\n") {
                    process.stdout.write(`[${type.toUpperCase()}] ${name}: ${buffer}`);
                    buffer = "";
                }
            }
        };
        return writable;
    }
    private inspect(data: any): string {
        try {
            return JSON.stringify(data, null, "  ");
        } catch (e) {
            return inspect(data);
        }
    }

    private handler(event: IApplicationLogEvent) {
        console.log("log h", this);
        process.stderr.write(
            `[${event.levelName.toUpperCase()}] ${event.source}: ${event.message} (${this.inspect(event.meta)})\n`,
        );
    }
}
