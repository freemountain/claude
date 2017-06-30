interface IClaudeRunOptions {
    name: string;
    image: string;
    labels: { [label: string]: string };
    cmd: string[];
    env: { [name: string]: string };
    mounts: { [host: string]: string };
    ports: { [host: string]: string };
}

export default IClaudeRunOptions;
