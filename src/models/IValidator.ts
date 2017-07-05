export interface IValidatonError {
    path: string;
    message: string;
}
export interface IValidator {
    validate(schema: string, data: any, prefix?: string): Promise<IValidatonError[]>;
}