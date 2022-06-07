import {combineReducers} from 'redux';
import request from '../../utility/api-requests'
import {actions as globalActions} from "./crosscutting";

const TORCHER_START = 'TORCHER_START';
const TORCHER_EVENT = 'TORCHER_EVENT';
const TORCHER_SOURCE = 'CURRENT_SOURCE';
const TORCHER_STATUS = 'EXECUTION_STATUS';

export const torcherEvents = (events) => ({type: TORCHER_EVENT, events});
const events = (state = [], action) => {
    switch (action.type) {
        case TORCHER_START:
            return [];
        case TORCHER_EVENT:
            return [...action.events, ...state];
    }
    return state;
};

export const updateSource = (source) => ({type: TORCHER_SOURCE, source});
const source = (state = '', action) => {
    switch (action.type) {
        case TORCHER_SOURCE:
            return action.source;
    }
    return state;
};


export const startTorcher = (source) => {
    return dispatch =>
        request('/torcher/startTorcher', {parameters: {source}})
            .then(dispatch({type: TORCHER_START}))
            .catch(error => dispatch(globalActions.displayError(error, 'Failed to start torcher')))
};
export const stopTorcher = () => {
    return dispatch =>
        request('/torcher/stopTorcher', {method: 'GET'})
            .catch(error => dispatch(globalActions.displayError(error, 'Failed to stop torcher')))
};
export const setTorcherStatus = (status) => ({type: TORCHER_STATUS, status});
const status = (state = {running: false}, action) => {
    switch (action.type) {
        case TORCHER_STATUS:
            const {summary, ...rest} = action.status;
            return {...rest};
    }
    return state;
};
const summary = (state = null, action) => {
    switch (action.type) {
        case TORCHER_STATUS:
            const {summary} = action.status;
            return summary;
    }
    return state;
};



export default combineReducers({
    events,
    source,
    status,
    summary,
});
