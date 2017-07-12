import { Writable } from "stream";

type logHandler = (message: string, meta?: object) => void;

export type LoggerFactory = (o: {}| string) => ILogger;

export interface ILogger {
    error: logHandler;
    warn: logHandler;
    info: logHandler;
    verbose: logHandler;
    debug: logHandler;
    getLogger(o: {}): ILogger;
    getLogStream(type: string, name: string): Writable;
}