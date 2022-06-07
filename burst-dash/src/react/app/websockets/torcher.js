import BurstWebSocket from './burst-web-socket';
import store from '../store';
import {setTorcherStatus, torcherEvents, updateSource} from "../store/reducers/torcher";

const TORCHER_STATUS = "TORCHER_STATUS";
const TORCHER_MESSAGE = "TORCHER_MESSAGE";
const TORCHER_SOURCE = "TORCHER_SOURCE";

const messages = [];
const dispatchMessages = () => {
    if (messages.length) {
        console.log(`Dispatching ${messages.length} torcher messages`);
        store.dispatch(torcherEvents(messages.splice(0, messages.length).reverse()));
    }
};
setInterval(dispatchMessages, 1000);

const postTorcherUpdate = (data) => {
    const { op, status, source } = data
    console.log(`Received torcher update: ${op}`);

    switch (op) {
        case TORCHER_STATUS:
            store.dispatch(setTorcherStatus(status));
            break;

        case TORCHER_MESSAGE:
            messages.push(data);
            break;

        case TORCHER_SOURCE:
            store.dispatch(updateSource(source));
            break;
    }
};

let ws;
const start = () => {
    if (!ws) {
    ws = new BurstWebSocket(postTorcherUpdate, "/torcher");
    }
}
export default start

