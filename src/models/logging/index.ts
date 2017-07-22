import { Writable } from "stream";
import IApplicationLogEvent from "./IApplicationLogEvent";
import ILogger from "./ILogger";
import ILogMessage from "./ILogMessage";

type LoggerFactory = (o: {} | string) => ILogger;
type LogStreamFactory = (stream: string) => Writable;

export {
    IApplicationLogEvent,
    ILogger,
    ILogMessage,
    LoggerFactory,
    LogStreamFactory,
};
