import * as Docker from "dockerode";
import { Container, injectable, interfaces } from "inversify";
import { get } from "lodash";
import "reflect-metadata";
import { PassThrough } from "stream";
import ContainerController from "./ContainerController";
import DeploymentController from "./DeploymentController";
import { createLogger, createLogStream, UDPClient } from "./logging";
import main from "./main";
import { ResourceControllerFactory } from "./models/controller";
import { ILogger } from "./models/logging";

import sms = require("source-map-support");
import Validator from "./Validator";

import LoggingController from "./resources/logging/LoggingController";
import ProxyController from "./resources/proxy/ProxyController";
import StorageController from "./resources/storage/StorageController";

sms.install();

const container = new Container();

container.bind("Validator").to(Validator).inSingletonScope();
container.bind("ContainerController").to(ContainerController).inSingletonScope();
container.bind("DeploymentController").to(DeploymentController).inSingletonScope();
container.bind("ProxyController").to(ProxyController);
container.bind("StorageController").to(StorageController);
container.bind("LoggingController").to(LoggingController);
container.bind("main").toFactory(main);

container.bind("UDPClient").to(UDPClient).inSingletonScope();
container.bind<interfaces.Factory<ILogger>>("getLogger").toFactory(createLogger);
container.bind<interfaces.Factory<ILogger>>("getLogStream").toFactory(createLogStream);

container.bind<interfaces.Factory<Docker>>("getDocker").toFactory<Docker>(() => () => {
  return new Docker();
});

container.bind<interfaces.Factory<ResourceControllerFactory[]>>("getResourceController")
  .toFactory((context: interfaces.Context) => () => {
    return [
      context.container.get("LoggingController"),
      context.container.get("ProxyController"),
      context.container.get("StorageController"),
    ];
  });

export default container;
