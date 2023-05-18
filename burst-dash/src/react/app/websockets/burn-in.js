import BurstWebSocket from './burst-web-socket';
import store from "../store";
import {actions as burnIn} from "../store/reducers/burn-in";

const deleteNullKeys = obj => {
    for (const key of Object.keys(obj)) {
        if (obj[key] === null) {
            delete obj[key]
        }
    }
}

const removeScalaNones = config => {
    deleteNullKeys(config)
    for (const batch of config.batches) {
        deleteNullKeys(batch)
        for (const dataset of batch.datasets) {
            deleteNullKeys(dataset)
        }
    }
}

const burnInListener = (data) => {
    const {
        msgType,
        events, // events msg
        isRunning, // status msg
        config, // config msg
    } = data;

    switch (msgType) {
        case "StatusMsg":
            store.dispatch(burnIn.updateStatus({isRunning}))
            break;
        case "ConfigMsg":
            if (config !== null) {
                removeScalaNones(config)
            }
            store.dispatch(burnIn.setConfig({config}))
            break;
        case "EventsMsg":
            store.dispatch(burnIn.receivedEvents({events}))
            break;
    }
}

let ws
const start = () => {
    if (!ws) {
        ws = new BurstWebSocket(burnInListener, "/burn-in")
    }
}

export default start
