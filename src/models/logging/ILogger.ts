type logHandler = (message: string, meta?: object) => void;

interface ILogger {
    error: logHandler;
    warn: logHandler;
    info: logHandler;
    verbose: logHandler;
    debug: logHandler;
}

export default ILogger;
