export const ALL_STATE = 'ALL';
export const LIVE_STATE = 'LIVE';
export const DEAD_STATE = 'DEAD';
export const FLAKY_STATE = 'FLAKY';
export const TARDY_STATE = 'TARDY';
export const EXILED_STATE = 'EXILED';


const UPDATE_WORKER_LIST = 'UPDATE_WORKER_LIST';

export const workersUpdate = ({homogeneous, workers}) => ({type: UPDATE_WORKER_LIST, homogeneous, workers});

const INITIAL_STATE = {
    meta: {initialized: false},
    workers: {byId: {}, allIds: [], summary: {count: 0}}
};

const mapState = state => {
    switch (state.code) {
        case 1:
            return 'Live';
        case 2:
            return 'Tardy';
        case 3:
            return 'Flaky';
        case 4:
            return 'Dead';
        case 5:
            return 'Exiled';
        default:
            return 'Unknown';
    }
}


export default (state = INITIAL_STATE, action) => {
    switch (action.type) {
        case UPDATE_WORKER_LIST:
            const {workers, homogeneous} = action;
            const summary = {count: 0};
            const byId = workers.reduce((all, w) => {
                const {assessment, nodeId, state, ...rest} = w;
                const stateStr = mapState(state);
                summary[stateStr] = (summary[stateStr] || 0) + 1;
                summary.count += 1;
                all[nodeId] = {nodeId, state: stateStr, ...rest};
                return all
            }, {});
            return {
                ...state,
                meta: {initialized: true, homogeneous},
                workers: {
                    byId,
                    summary,
                    allIds: action.workers.map(w => w.nodeId)
                }
            };
    }
    return state;
}
