import { injectable } from "inversify";
import { join } from "path";
import { ILogger } from "./models/ILogger";

@injectable()
export default class BaseController {
    protected logger: ILogger;

    constructor(
        protected namespace: string,
        logger: ILogger,
    ) {
        this.logger = logger.getLogger(this);
    }

    protected applicationFile(...name: string[]) {
        return join(__dirname, "..", ...name);
    }
    protected dataFile(...name: string[]) {
        return join("/var/claude", ...name);
    }
}