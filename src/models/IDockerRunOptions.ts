
const s = {
    Env: [
        "AAA=23",
    ],
    ExposedPorts: {
        "80/tcp": {},
    },
    Hostconfig: {
        AutoRemove: true,
        Binds: [
            "/var/claude/volumes/proxy-etc:/etc/nginx",
        ],
        PortBindings: {
            "80/tcp": [
                {
                    HostIP: "0.0.0.0",
                    HostPort: 80,
                },
            ],
        },
    },
    Labels: {
        "claude.cid": "ed8409bd-1cc6-44ad-9af9-318d0eb8751c",
        "claude.clean": true,
        "claude.namespace": "test",
    },
    Names: [
        "proxy",
    ],
    Tty: true,
    Volumes: {
        "/etc/nginx": {},
    },
};

import { Dictionary } from "ramda";

interface IDockerRunOptions {
    Env: string[];
    ExposedPorts: Dictionary<{}>;
    Hostconfig: {
        AutoRemove: boolean,
        Binds: string[],
        PortBindings: Dictionary<Array<{
            HostIP: string,
            HostPort: string,
        }>>;
    };
    Image: string;
    Cmd: string[];
    Labels: Dictionary<string>;
    Names: string[];
    Volumes: Dictionary<{}>;
}

export default IDockerRunOptions;
