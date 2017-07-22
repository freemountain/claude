import { injectable } from "inversify";
import { join } from "path";
import { ILogger } from "./models/logging";

@injectable()
export default class BaseController {
    constructor(
        protected namespace: string,
        protected logger: ILogger,
    ) {
    }

    protected applicationFile(...name: string[]) {
        return join(__dirname, "..", ...name);
    }
    protected dataFile(...name: string[]) {
        return join("/var/claude", ...name);
    }
}