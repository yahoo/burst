import request from '../../utility/api-requests';
import {actions as crosscutting} from "./crosscutting";
import {createAsyncThunk, createSlice} from "@reduxjs/toolkit";

const start = createAsyncThunk('profiler/start', async ({
                                                            domainPk,
                                                            viewPk,
                                                            source,
                                                            timezone,
                                                            concurrency,
                                                            executions,
                                                            reload
                                                        }, thunk) => {
    try {
        const parameters = {domain: domainPk, view: viewPk, source, timezone, concurrency, executions, reload};
        const response = await request('/profiler/run', {parameters})
        if (response.success === false) {
            throw new Error('Check the logs on the supervisor')
        }
    } catch (e) {
        thunk.dispatch(crosscutting.displayError(e, 'Failed to start profiler'))
        throw e
    }
})

const stop = createAsyncThunk('profiler/stop', async (none, thunk) => {
    try {
        const response = await request('/profiler/stop')
        if (response.success === false) {
            throw new Error('Check the logs on the supervisor')
        }
    } catch (e) {
        thunk.dispatch(crosscutting.displayError(e, 'Failed to stop profiler'))
        throw e
    }
})

const profilerSlice = createSlice({
    name: 'profiler',
    initialState: {
        attached: false,
        running: false,
        requestPending: false,
        config: {domainPk: 0, viewPk: 0, concurrency: 1, executions: 1, reload: 0, source: ''},
        events: [],
    },
    reducers: {
        attach: (state, action) => {
            const {
                config: {source, domain, view, timezone, concurrency, executions, loads, running},
                events
            } = action.payload;
            state.attached = true
            state.running = running
            state.config = {
                source, timezone, concurrency, executions, domainPk: domain, viewPk: view, reload: loads,
            }
            state.events = events.reverse()
        },
        receiveEvent: (state, action) => {
            const event = action.payload
            state.events.unshift(event)
            state.running = event.isRunning
        },
        updateConfig: (state, action) => {
            state.config = {...state.config, ...action.payload}
        }
    },
    extraReducers: {
        [start.pending]: (state) => {
            state.requestPending = true
        },
        [start.fulfilled]: (state) => {
            state.requestPending = false
            state.running = true
        },
        [start.rejected]: (state) => {
            state.requestPending = false
        },
        [stop.pending]: (state) => {
            state.requestPending = true
        },
        [stop.fulfilled]: (state) => {
            state.requestPending = false
            state.running = false
        },
        [stop.rejected]: (state) => {
            state.requestPending = false
        },
    }
})

export default profilerSlice.reducer;

const {attach, receiveEvent, updateConfig} = profilerSlice.actions;

export const actions = {
    attach,
    receiveEvent,
    updateConfig,
    start,
    stop,
}
