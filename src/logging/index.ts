import { Container, injectable, interfaces } from "inversify";
import { PassThrough } from "stream";
import * as through from "through2";

import { IApplicationLogEvent, ILogMessage, LoggerFactory, LogStreamFactory } from "../models/logging";
import Logger from "./Logger";
import UDPClient from "./UDPClient";

const createMsg = (event: IApplicationLogEvent) => {
    const line = `${event.source}: ${event.message}`;
    return event.meta ? [ line, event.meta ] : line;
};

const createLogger = (context: interfaces.Context): LoggerFactory => {
    const logio = context.container.get<UDPClient>("UDPClient");

    const target = through.obj((event: IApplicationLogEvent, _, cb) => cb(null, {
        message: createMsg(event),
        stream: "controller",
    } as ILogMessage));

    target.pipe(logio.input);

    return (o: {} | string) => new Logger(o, target);
};

const createLogStream = (context: interfaces.Context): LogStreamFactory => {
    const logio = context.container.get<UDPClient>("UDPClient");

    return (stream: string) => {
        const logStream = new PassThrough({ objectMode: true });
        let message = "";

        logStream.on("data", (data: string) => {
            for (const char of data) {
                if (char !== "\n") {
                    message += char;
                    continue;
                }
                logio.send({  message, stream });
                message = "";
            }
        });

        return logStream;
    };
};

export {
    createLogger,
    createLogStream,
    UDPClient,
};
