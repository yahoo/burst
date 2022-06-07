import BurstWebSocket from './burst-web-socket';
import store from '../store';
import {workersUpdate} from "../store/reducers/workers";

const websocketListener = (data) => {
    const {homogeneous, workers} = data;
    console.debug(`Received update for ${workers.length} workers`);
    store.dispatch(workersUpdate({homogeneous, workers}));
};

let ws;
const start = () => {
    if (!ws) {
        ws = new BurstWebSocket(websocketListener, "/workers");
    }
}

export default start

function setHomogeneousMode(homogeneous) {
    socket.send({action: "set-homogeneity", required: homogeneous})
}

export {
    setHomogeneousMode
}
