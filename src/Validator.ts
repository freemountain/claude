import * as Ajv from "ajv";
import { readdir, readJSON, stat } from "fs-extra";
import { join } from "path";
import { flatten, lens } from "ramda";
import { IValidatonError, IValidator } from "./models/IValidator";

export default class Validator implements IValidator {
    private ajv: Ajv.Ajv;

    constructor() {
        this.ajv = new Ajv();
        this.ajv.addMetaSchema(require("ajv/lib/refs/json-schema-draft-04.json"));
    }

    public async validate(schema: string, data: any, prefix: string = ""): Promise<IValidatonError[]> {
        const valid: boolean = await this.ajv.validate(schema, data);
        const errors = (this.ajv.errors || []).map(({ message, dataPath }) => ({
            message: message as string,
            path: `${prefix}${dataPath}`,
        }));
        return valid ? [] : errors;
    }

    public async load() {
        const files = (await this.scan(join(__dirname, "src")));
        const schemas = await Promise.all(files.map((f) => readJSON(f)));

        schemas
            .filter((schema) => !!schema["$schema"])
            .filter((schema) => !!schema["title"])
            .forEach((schema) => this.ajv.addSchema(schema, schema.title));
    }

    public async scan(dir: string): Promise<string[]> {
        const entries = await Promise.all((await readdir(dir)).map(async (entry) => {
            const path = join(dir, entry);
            const stats = await stat(path);

            if (stats.isFile()) {
                return [path];
            }

            if (stats.isDirectory()) {
                return this.scan(path);
            }

            return [];
        }));

        return flatten<string>(entries);
    }
}