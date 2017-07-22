import * as dgram from "dgram";
import { injectable } from "inversify";
import { Socket } from "net";
import { PassThrough } from "stream";

import { ILogMessage } from "../models/logging";

@injectable()
export default class UDPClient {
    public input: PassThrough;
    private buffer: ILogMessage[];
    private socket: dgram.Socket;
    private server: { host: string, port: number };

    constructor() {
        this.input = new PassThrough({ objectMode: true });
        this.socket = dgram.createSocket("udp4");
        this.buffer = [];

        this.input.on("data", (msg: ILogMessage) => {
            if (this.server) {
                this.write(msg);
            } else {
                this.buffer.push(msg);
            }
        });
    }

    public send(msg: ILogMessage) {
        this.input.push(msg);
    }

    public start(host: string, port: number) {
        this.server = { host, port };

        let msg = this.buffer.shift();
        while (msg) {
            this.write(msg);
            msg = this.buffer.shift();
        }
    }

    private write(msg: ILogMessage) {
        const buffer = new Buffer(JSON.stringify({
                content: msg.message,
                id: msg.stream,
        }));
        this.socket.send(buffer, 0, buffer.length, this.server.port, this.server.host);
    }
}
