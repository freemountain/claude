import { Writable } from "stream";
import { inspect } from "util";

import Logger from "./Logger";
import IApplicationLogEvent from "./models/IApplicationLogEvent";
import ILogger from "./models/ILogger";

export default class RootLogger extends Logger {
    constructor(name: string) {
        super((msg) => this.logHandler(msg), name);
    }

    public getContainerLogStream(name: string): Writable {
        let buffer = "";
        const writable = new Writable();

        writable._write = (chunk, encoding, done) => {
            const current: string = chunk.toString();
            for (const char of current) {
                buffer += char;
                if (char === "\n") {
                    process.stdout.write(`[CONTAINER] ${name}: ${buffer}`);
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

    private logHandler(event: IApplicationLogEvent) {
        process.stderr.write(
            `[${event.levelName.toUpperCase()}] ${event.source}: ${event.message} (${this.inspect(event.meta)})\n`,
        );
    }
}
