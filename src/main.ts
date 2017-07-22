import { Container, injectable, interfaces } from "inversify";
import { IDeploymentController } from "./models/controller";
import ISettings from "./models/ISettings";
import { LoggerFactory } from "./models/logging";

@injectable()
class Setings implements ISettings {
    public namespace: string = "test";
}

const main = (context: interfaces.Context) => async () => {
    const { container } = context;
    container.bind("Settings").to(Setings);

    const logger = container.get<LoggerFactory>("getLogger")("Root");

    process.on("SIGINT", () => process.exit());
    process.on("unhandledRejection", (error) => {
        console.error(`unhandledRejection: ${error.message} ${error.stack}`);
        process.exit(-1);
    });

    const deployment: IDeploymentController = container.get<IDeploymentController>("DeploymentController");
    await deployment.start();

    await deployment.assertDeployment("hello-foo", {
        resources: {
            proxy: {
                domain: "foo.claude.dev",
                port: 80,
                target: "nginx",
            },
            storage: {
                volumes: ["website"],
            },
        },
        services: {
            nginx: {
                cmd: [],
                image: "kitematic/hello-world-nginx",
                readonly: true,
                resources: {
                    storage: {
                        website: "/website_files",
                    },
                },
            },
        },
    });
};

export default main;
