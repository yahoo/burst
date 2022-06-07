import {baseURL} from '../utility/api-requests';

const RawWebsocketCache = {};

export class BurstWebSocket {
    constructor(msgHandler, endpoint) {
        this.handler = msgHandler;
        this.interval = null;
        this.connection = null;
        this.url = `wss://${baseURL}/ws${endpoint}`;
        RawWebsocketCache[this.url] = false;
        this.connect()
    }

    connect() {
        console.log(`BurstWebSocket(${this.url}) connecting...`);
        const connection = new WebSocket(this.url);
        connection.onopen = () => {
            if (RawWebsocketCache[this.url] !== false) {
                connection.close();
                return;
            }
            RawWebsocketCache[this.url] = connection;
            clearInterval(this.interval);
            this.interval = null;
            this.connection = connection;
            this.connectTime = Date.now();

            console.log(`BurstWebSocket(${this.url}) connection established`);

            connection.onmessage = event => {
                if (event.data !== "hello") {
                    let json;
                    try {
                        json = JSON.parse(event.data)
                    } catch (e) {
                        console.error('Failed to parse ws message as JSON', e)
                        return
                    }
                    this.handler(json)
                }
            };

            connection.onerror = e => {
                console.log(`BurstWebSocket(${this.url}) errored after ${Date.now() - this.connectTime}ms`, e);
                const conn = RawWebsocketCache[this.url];
                conn.onclose = undefined
                conn.onerror = undefined
                RawWebsocketCache[this.url] = undefined;
                this.reconnect();
            };
            connection.onclose = e => {
                console.log(`BurstWebSocket(${this.url}) closed after ${Date.now() - this.connectTime}ms`, e);
                const conn = RawWebsocketCache[this.url];
                conn.onclose = undefined
                conn.onerror = undefined
                RawWebsocketCache[this.url] = undefined;
                this.reconnect();
            };
        };
    }

    reconnect() {
        if (RawWebsocketCache[this.url] === false) {
            return;
        }
        RawWebsocketCache[this.url] = false;

        let wait = 100;
        let lastConnectTime = Date.now();
        this.interval = setInterval(() => {
            const now = Date.now();
            if ((now - lastConnectTime) <= wait) {
                return
            }
            lastConnectTime = now;
            wait = Math.min(wait * 2, 2000);
            console.log(`Attempting to reconnect to ${this.url}`);
            if (this.connection) {
                this.connection.close();
                this.connection = null;
            }
            this.connect()
        }, 100);
    }

    send(data) {
        if (!this.connection) {
            setTimeout(() => this.send(data), 250);
            return;
        }
        this.connection.send(JSON.stringify(data));
    }
}

export default BurstWebSocket
