import DockerEvents = require("docker-events");
import * as Docker from "dockerode";
import Ctrl from "./NamespaceController";
import ProxyCtrl from "./resources/proxy/ProxyController";
import StorageCtrl from "./resources/storage/StorageController";

import sms = require("source-map-support");
import Validator from "./Validator";

import * as parse from "./shim";

sms.install();

(async () => {
  const docker = new Docker();
  const validator = new Validator();
  const app = new Ctrl(docker, validator, "test", "claude.dev", [
    ProxyCtrl.create,
    StorageCtrl.create,
  ]);
  const logger = app.getLogger("root");

  process.on("SIGINT", () => app.stop().then(() => process.exit()));
  process.on("unhandledRejection", (error) => {
    logger.error(`unhandledRejection: ${error.message} ${error.stack}`);
    process.exit(-1);
  });
  /*
    await app.start();
  
    app.assertDeployment("hello-foo", {
      resources: {
        proxy: {
          domain: "foo.claude.dev",
          target: "nginx",
          port: 80,
        },
        storage: {
          volumes: ["website"],
        }
      },
      services: {
        nginx: {
          cmd: [],
          image: "kitematic/hello-world-nginx",
          readonly: true,
          resources: {
            storage: {
              website: "/website_files",
            }
          },
        },
      },
    });
  
    app.assertDeployment("hello-bar", {
      resources: {
        proxy: {
          domain: "bar.claude.dev",
          target: "nginx",
          port: 80,
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
  */

  const options = {
    Image: "library/nginx:1.13.2-alpine"
  };

  logger.info(`Pulling image ${options.Image}`);

  let info: any;
  let isError: boolean = true;
  try {
    const img = docker.getImage(options.Image);
    info = await img.inspect();
    isError = false;
  } catch (e) {
    info = e;
  } finally {
    logger.info(`Getting image ${options.Image} ${isError ? "failed" : "success"}`, info);
  }


  await new Promise((resolve, reject) => {
    docker.pull(options.Image, {}, (err, stream) => {
      docker.modem.followProgress(stream, () => resolve(), (e: any) => {
        logger.info(`Pulling ${options.Image}`, e);
      });
    });
  })

  isError = true;
  try {
    const img = docker.getImage(options.Image);
    info = await img.inspect();
    isError = false;
  } catch (e) {
    info = e;
  } finally {
    logger.info(`Getting image ${options.Image} ${isError ? "failed" : "success"}`, info);
  }
})();
