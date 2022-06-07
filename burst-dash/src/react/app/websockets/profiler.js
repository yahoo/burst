import BurstWebSocket from './burst-web-socket';
import store from '../store';
import {actions as profiler} from "../store/reducers/profiler";

const profilerListener = (data) => {
    const {msgType: type, config, events, event} = data;

    switch (type) {
        case "profiler-attach":
            store.dispatch(profiler.attach({config, events}))
            break;

        case "profiler-event":
            store.dispatch(profiler.receiveEvent(event))
            break;

    }
};

let ws;
const start = () => {
    if (!ws) {
        ws = new BurstWebSocket(profilerListener, "/profiler");
    }
}
export default start


