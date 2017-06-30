import DockerEvents = require("docker-events");
import * as Docker from "dockerode";
import Ctrl from "./NamespaceController";
import ProxyCtrl from "./ProxyController";
import sms = require("source-map-support");

sms.install();

(async () => {
  const docker = new Docker();
  const app = new Ctrl(docker, "test", "claude.dev", [
    ProxyCtrl.create,
  ]);
  const logger = app.getLogger("root");

  process.on("SIGINT", () => app.stop().then(() => process.exit()));
  process.on("unhandledRejection", (error) => {
    logger.error(`unhandledRejection: ${error.message} ${error.stack}`);
    process.exit(-1);
  });

  await app.start();

  app.assertDeployment("hello-foo", {
    pods: {
      nginx: {
        cmd: [],
        env: [],
        image: "kitematic/hello-world-nginx",
        memoryLimit: 1,
        readonly: true,
        volumes: {
          "/var": "/website_files",
        },
      },
    },
    resources: {
      http: {
        domain: "foo.claude.dev",
        pod: "nginx",
        port: 80,
      },
    },
  });

  app.assertDeployment("hello-bar", {
    pods: {
      nginx: {
        cmd: [],
        env: [],
        image: "kitematic/hello-world-nginx",
        memoryLimit: 1,
        readonly: true,
        volumes: {
          "/var": "/website_files",
        },
      },
    },
    resources: {
      http: {
        domain: "bar.claude.dev",
        pod: "nginx",
        port: 80,
      },
    },
  });
})();
