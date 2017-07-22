import { injectable } from "inversify";
import { get } from "lodash";
import { Writable } from "stream";

import { IApplicationLogEvent, ILogger } from "../models/logging";
import { inspect } from "../utils";

@injectable()
export default class Logger implements ILogger {
    private source: string;

    constructor(o: {}| string, private target: Writable) {
        this.source = typeof o === "string" ? o : get(o, "name", o.constructor.name);
    }

    public error(message: string, meta?: object) {
        this.target.write({ level: 0, levelName: "error", message, meta, source:  this.source });
    }

    public warn(message: string, meta?: object) {
        this.target.write({ level: 1, levelName: "warn", message, meta, source:  this.source });
    }

    public info(message: string, meta?: object) {
        this.target.write({ level: 2, levelName: "info", message, meta, source:  this.source });
    }

    public verbose(message: string, meta?: object) {
        this.target.write({ level: 3, levelName: "verbose", message, meta, source:  this.source });
    }

    public debug(message: string, meta?: object) {
        this.target.write({ level: 4, levelName: "debug", message, meta, source:  this.source });
    }
}
