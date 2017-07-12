import * as Docker from "dockerode";
import { Container, injectable, interfaces } from "inversify";
import { get } from "lodash";
import "reflect-metadata";
import ContainerController from "./ContainerController";
import DeploymentController from "./DeploymentController";
import main from "./main";
import { ResourceControllerFactory } from "./models/controller";
import { ILogger } from "./models/ILogger";
import ProxyCtrl from "./resources/proxy/ProxyController";
import StorageCtrl from "./resources/storage/StorageController";
import RootLogger from "./RootLogger";
import sms = require("source-map-support");
import Validator from "./Validator";

sms.install();

const container = new Container();

container.bind("Validator").to(Validator).inSingletonScope();
container.bind("ContainerController").to(ContainerController).inSingletonScope();
container.bind("DeploymentController").to(DeploymentController).inSingletonScope();
container.bind("ProxyCtrl").to(ProxyCtrl);
container.bind("StorageCtrl").to(StorageCtrl);
container.bind("main").toFactory(main);

container.bind<interfaces.Factory<ILogger>>("getLogger").toFactory(() => (o: {} | string) => {
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

export default container;
