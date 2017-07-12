import DockerEvents = require("docker-events");
import * as Docker from "dockerode";
import { Container, injectable, interfaces } from "inversify";
import { get } from "lodash";
import "reflect-metadata";
import ContainerController from "./ContainerController";
import DeploymentController from "./DeploymentController";
import { ILogger, LoggerFactory } from "./models/ILogger";

import { IResourceController, ResourceControllerFactory } from "./models/IResourceController";
import ProxyCtrl from "./resources/proxy/ProxyController";
import StorageCtrl from "./resources/storage/StorageController";
import RootLogger from "./RootLogger";

import sms = require("source-map-support");
import Validator from "./Validator";

sms.install();

@injectable()
class Settings {
  public namespace: string = "test";
}

const container = new Container();
container.bind("Validator").to(Validator).inSingletonScope();
container.bind("Settings").to(Settings);
container.bind("ContainerController").to(ContainerController).inSingletonScope();
container.bind("DeploymentController").to(DeploymentController).inSingletonScope();
container.bind("ProxyCtrl").to(ProxyCtrl);
container.bind("StorageCtrl").to(StorageCtrl);

container.bind<interfaces.Factory<RootLogger>>("getLogger").toFactory(() => (o: {} | string) => {
  if (typeof o === "string") {
    return new RootLogger(o);
  }

  return new RootLogger(get(o, "name", o.constructor.name));
});

container.bind<interfaces.Factory<Docker>>("getDocker").toFactory<Docker>(() => () => {
  return new Docker();
});

container.bind<interfaces.Factory<ResourceControllerFactory[]>>("getResourceController")
  .toFactory((context: interfaces.Context) => () => {
    return [
      context.container.get("ProxyCtrl"),
      context.container.get("StorageCtrl"),
    ];
  });

(async () => {
  const logger = container.get<LoggerFactory>("getLogger")("Root");
  process.on("SIGINT", () => process.exit());
  process.on("unhandledRejection", (error) => {
    logger.error(`unhandledRejection: ${error.message} ${error.stack}`);
    process.exit(-1);
  });

  const deployment = container.get<DeploymentController>("DeploymentController");
  await deployment.start();
  logger.info("started");
  deployment.assertDeployment("hello-foo", {
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
})();
/*
(async () => {
  const namespace = "test";
  const docker = new Docker();
  const validator = new Validator();
  const logger = new RootLogger("App");
  const container = new ContainerController(docker, logger, namespace);

  const app = new DeploymentController(docker, validator, container, logger, namespace, [
    ProxyCtrl.create,
    StorageCtrl.create,
  ]);

  process.on("SIGINT", () => app.stop().then(() => process.exit()));
  process.on("unhandledRejection", (error) => {
    logger.error(`unhandledRejection: ${error.message} ${error.stack}`);
    process.exit(-1);
  });

  setTimeout(() => null, 5000);
  await app.start();

  app.assertDeployment("hello-foo", {
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

  app.assertDeployment("hello-bar", {
    resources: {
      proxy: {
        domain: "bar.claude.dev",
        port: 80,
        target: "nginx",
      },
    },
    services: {
      nginx: {
        cmd: [],
        image: "kitematic/hello-world-nginx",
        readonly: true,
        resources: {},
      },
    },
  });

})();
*/
