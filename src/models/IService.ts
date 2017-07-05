interface IService {
    image: string;
    readonly: boolean;
    cmd: string[];
    resources: { [name: string]: {} };
}

export default IService;
