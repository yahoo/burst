import BurstWebSocket from './burst-web-socket';
import store from '../store';
import {actions as execution} from "../store/reducers/execution";

const waves = [];
let then = Date.now();
setInterval(() => {
    const wavesToUpdate = waves.splice(0, waves.length);
    if (wavesToUpdate.length) {
        const now = Date.now();
        console.debug(`Dispatching ${wavesToUpdate.length} wave updates, ${now - then}ms since last update`);
        then = now;
        store.dispatch(execution.postUpdates(wavesToUpdate))
    }
}, 500);

const executionListener = (data) => {
    const {msgType: type, since, guid, request} = data;

    switch (type) {
        case "relay-status":
            store.dispatch(execution.reset(since))
            break;

        case "request-update":
            waves.push(request);
            break;

        case "request-details":
            console.log(`Updating details for execution '${guid}'`);
            store.dispatch(execution.detailsFetched({guid, request}))
            break;
    }
};

let ws
const start = () => {
    if (!ws) {
        ws = new BurstWebSocket(executionListener, "/execution");
    }
};

export default start;

function clearExecutionList() {
    if (!ws) {
        setTimeout(() => clearExecutionList(), 100)
        return
    }

    console.log("clearing executions");
    ws.send({action: "clear-executions"})
}

function getRequestDetails(guid) {
    if (!ws) {
        setTimeout(() => getRequestDetails(guid), 100)
        return
    }
    console.log(`Requesting details for ${guid}`);
    ws.send({action: "get-request-details", guid})
}

export {
    clearExecutionList,
    getRequestDetails,
}
