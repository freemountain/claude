import { injectable } from "inversify";
import * as R from "ramda";
import { Writable } from "stream";
import IApplicationLogEvent from "./models/IApplicationLogEvent";
import {ILogger} from "./models/ILogger";

type EventHandler = (event: IApplicationLogEvent) => void;

interface IConstructedObject {
    constructor: {
        name: string;
    };
}
@injectable()
export default class Logger implements ILogger {
    constructor(
        private handler: EventHandler,
        private name: string,
    ) { }

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
        const s = {};
        this.getLogger(s);
    }

    public getLogger(o: IConstructedObject | string): ILogger {
        const name = typeof o === "string" ? o : o.constructor.name;
        return new Logger(this.handler, name);
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
}
