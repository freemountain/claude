interface IDockerNetworkInfo {
    Name: string;
    Id: string;
    Created: string;
    Scope: string;
    Driver: string;
    EnableIPv6: boolean;
    IPAM: { Driver: string, Options?: any, Config?: any[] };
    Internal: boolean;
    Attachable: boolean;
    Ingress: boolean;
    Containers: object;
    Options: { [name: string]: string };
    Labels: { [label: string]: string };
}

export default IDockerNetworkInfo;
