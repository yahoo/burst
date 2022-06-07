import BurstWebSocket from "./burst-web-socket";
import store from "../store";
import {actions} from "../store/reducers/thrift";

const torcherListener = data => {
    console.log(data)
    const {
        msgType,
        requests, // AllRequests
        req, // RequestUpdated
        ruid, exception // RequestReceived + RequestEncounteredException
    } = data
    switch (msgType) {
        case 'AllRequests':
            store.dispatch(actions.allRequests(requests.reverse()));
            break;
        case 'RequestReceived':
            store.dispatch(actions.receivedRequest(ruid))
            break;
        case 'RequestUpdate':
            store.dispatch(actions.updateRequest(req))
            break;
        case 'RequestEncouteredException':
            store.dispatch(actions.requestException(ruid, exception))
            break;
    }
}

let ws
const start = () => {
    if (!ws) {
        ws = new BurstWebSocket(torcherListener, "/thrift")
    }
}

export default start
