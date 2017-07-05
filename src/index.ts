import DockerEvents = require("docker-events");
import * as Docker from "dockerode";
import Ctrl from "./NamespaceController";
import ProxyCtrl from "./resources/proxy/ProxyController";
import StorageCtrl from "./resources/storage/StorageController";

import sms = require("source-map-support");
import Validator from "./Validator";

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

})();
