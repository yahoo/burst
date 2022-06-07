import {combineReducers} from 'redux';
import request from '../../utility/api-requests';
import {actions as globalActions} from "./crosscutting";
import {fetchDomains} from "./catalog";

export function genKeyString(generationKey) {
    const {domainKey, viewKey, generationClock} = generationKey;
    return `${domainKey}.${viewKey}.${generationClock}`;
}

export function sliceKeyString(generationKey, slice) {
    const {sliceKey, hostname} = slice;
    return `${generationKey}.${sliceKey}.${hostname}`;
}

/**
 * Determine if two generation identities are the same
 * @param left the first generation's identity
 * @param right the second generation's identity
 */
export function isSameGeneration(left, right) {
    return left.domain === right.domain && left.view === right.view && left.clock === right.clock;
}

/*
 * Action names
 */
const FETCHED_GENERATIONS = 'FETCHED_GENERATIONS';
const REFRESH_GENERATIONS = 'REFRESH_GENERATIONS';
const FLUSHED_GENERATIONS = 'FLUSHED_GENERATIONS';
const FETCHED_SLICES = 'FETCHED_SLICES';

const destructureMetrics = generationMetrics => {
    const {generationKey, state: {label: state}, ...metrics} = generationMetrics;
    return {generationKey, state, metrics}
}

/*
 * Actions
 */
export function searchGenerations({domainKey, viewKey, params}) {
    return dispatch => {
        const gen = domainKey ? `gen=${domainKey}${viewKey ? `;${viewKey}` : ''}` : '';
        const values = Object.values(params);
        const queryParams = values.length ? `&${values.map(p => `params=${p.name}-${p.op}-${p.value}`).join('&')}` : '';
        request(`/cache/generations?${gen}${queryParams}`, {method: 'GET'})
            .then(json => {
                const domains = [];
                const generations = json.map(({generationMetrics}) => {
                    const {generationKey, state, metrics} = destructureMetrics(generationMetrics);
                    domains.push(generationKey.domainKey);
                    return {generationKey, state, metrics};
                });
                dispatch({type: FETCHED_GENERATIONS, generations});
                if (domains.length) {
                    return fetchDomains(domains)(dispatch)
                }
            })
            .catch(e => dispatch(globalActions.displayMessage(`Failed to fetch cache data: ${e.message}`)));
    }
}

export function fetchSlices(identity) {
    return dispatch => {
        request(`/cache/generations/${genKeyString(identity)}`, {method: 'GET'})
            .then(json => {
                const {generationMetrics, slices} = json;
                const {generationKey, state, metrics} = destructureMetrics(generationMetrics);
                dispatch({type: REFRESH_GENERATIONS, generations: [{generationKey, state, metrics}]});
                dispatch({
                    type: FETCHED_SLICES,
                    slices: {
                        [genKeyString(generationKey)]: slices.map(slice => {
                            const {sliceKey, hostname, generationMetrics} = slice;
                            const {state, metrics} = destructureMetrics(generationMetrics);
                            return {sliceKey, hostname, state, metrics};
                        })
                    }
                });
            })
            .catch(e => dispatch(globalActions.displayMessage(`Failed to fetch slices: ${e.message}`)));
    }
}

function cacheOperation(dispatch, action, identity) {
    return request('/cache/generations', {parameters: {action, generation: genKeyString(identity)}})
        .then(json => {
            dispatch(globalActions.displayMessage(json.length
                ? `Generation ${action}ed: ${json.length} generations, ${json.reduce((count, {metrics}) => count + metrics.sliceCount, 0)} slices`
                : `Failed to ${action} generation`));
            return json;
        })
        .catch(e => dispatch(globalActions.displayMessage(`Failed to ${action} generation: ${e.message}`)));
}

export function evictGeneration(identity) {
    return dispatch => cacheOperation(dispatch, "evict", identity)
        .then(json => {
            if (json.length) {
                dispatch({type: REFRESH_GENERATIONS, generations: json})
            }
        });
}

export function flushGeneration(identity) {
    return dispatch => cacheOperation(dispatch, "flush", identity)
        .then(json => {
            if (json.length) {
                dispatch({type: FLUSHED_GENERATIONS, generations: json})
            }
        });
}

/*
 * Reducers
 */
const generations = (state = [], action) => {
    switch (action.type) {
        case FETCHED_GENERATIONS:
            return action.generations;
        case REFRESH_GENERATIONS:
            const refreshed = action.generations;
            return state.map(g => {
                const updated = refreshed.findIndex(r => isSameGeneration(r.generationKey, g.generationKey));
                return updated !== -1 ? refreshed.splice(updated, 1)[0] : g;
            });
        case FLUSHED_GENERATIONS:
            const flushed = action.generations;
            return state.filter(g => !flushed.find(f => isSameGeneration(f.generationKey, g.generationKey)));
        default:
            return state;
    }
};
const slices = (state = {}, action) => {
    switch (action.type) {
        case FETCHED_SLICES:
            return {...state, ...action.slices};
        default:
            return state;
    }
};

export default combineReducers({
    generations,
    slices
})
