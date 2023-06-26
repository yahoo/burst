import {createAsyncThunk, createSlice} from "@reduxjs/toolkit";
import {actions as crosscutting} from "./crosscutting";
import request, {asJson} from "../../utility/api-requests";
import merge from 'lodash.merge'

const startBurnIn = createAsyncThunk('burn-in/start', async ({config}, thunk) => {
    try {
        const resp = await request('/burn-in/start', asJson(config))
        const {running} = resp;
        return {running};
    } catch (e) {
        thunk.dispatch(crosscutting.displayError(e, 'Http Error'))
        throw e
    }
})

const stopBurnIn = createAsyncThunk('burn-in/stop', async (ignored, thunk) => {
    try {
        const {running} = await request('/burn-in/stop', asJson())
        return {running};
    } catch (e) {
        thunk.dispatch(crosscutting.displayError(e, 'Http Error'))
        throw e
    }
})

const emptyBatch = {
    concurrency: 1,
    datasets: [],
    queries: [''],
    durationType: 'duration',
    desiredDuration: '1 hour'
}

const defaultDataset = {
    datasetSource: 'byPk',
    pk: 0
}

const burnInSlice = createSlice({
    name: 'burn-in',
    initialState: {
        running: false,
        config: {
            batches: [{...emptyBatch}],
        },
        events: []
    },
    reducers: {
        receivedEvents: (state, action) => {
            const {events} = action.payload
            for (const event of events) {
                state.events.unshift(event)
                state.events.splice(1500, Infinity)
            }
        },
        updateStatus: (state, action) => {
            const {isRunning, config} = action.payload
            if (config) {
                state.config = config;
            }
            state.running = isRunning;
        },
        setConfig: (state, action) => {
          const {config} = action.payload
          if (config) {
              state.config = config
          }
          state.events = [];
        },
        updateConfig: (state, action) => {
            const {config} = action.payload
            if (!config) {
                console.warn('No config detected in burn-in/updateConfig action');
            }
            state.config = merge({}, state.config, config)
        },
        updateBatch: (state, action) => {
            const {idx, prop, value} = action.payload;
            const batch = state.config.batches[idx];
            switch (prop) {
                case "concurrency":
                    batch.concurrency = value;
                    break;
                case "queries":
                    batch.queries = value;
                    break;
                case "loadQuery":
                    batch.defaultLoadQuery = value;
                    break;
                case "maxDuration":
                    batch.maxDuration = value;
                    break;
                case "durationType":
                    batch.durationType = value;
                    if (value === 'duration') {
                        batch.desiredDuration = '1 hour'
                        batch.desiredDatasetIterations = null
                    } else if (value === 'datasets') {
                        batch.desiredDuration = null;
                        batch.desiredDatasetIterations = 100
                    }
                    break;
                case "desiredDuration":
                    batch.desiredDuration = value;
                    break;
                case "desiredIterations":
                    batch.desiredDatasetIterations = value;
                    break;
                case "addDataset":
                    batch.datasets.push({...defaultDataset})
                    break;
                case "removeDataset":
                    batch.datasets = [...batch.datasets.slice(0, value), ...batch.datasets.slice(value + 1)]
                    break;
                case "dataset":
                    const {key, update, idx} = value
                    const dataset = batch.datasets[idx];
                    switch (key) {
                        case "datasetSource":
                            dataset.datasetSource = update
                            delete dataset.pk
                            delete dataset.udk
                            delete dataset.label
                            delete dataset.labelValue
                            delete dataset.domain
                            delete dataset.view
                            switch (dataset.datasetSource) {
                                case "byPk":
                                    dataset.pk = 0
                                    break;
                                case "byUdk":
                                    dataset.udk = ''
                                    break;
                                case "byProperty":
                                    dataset.label = ''
                                    break;
                                case "generate":
                                    dataset.domain = {
                                        domainProperties: {
                                            'burst.store.name': ''
                                        }
                                    }
                                    dataset.view = {
                                        schemaName: '',
                                        storeProperties: {
                                            'burst.samplestore.source.name': ''
                                        },
                                        viewMotif: 'VIEW v {}',
                                        viewProperties: {}
                                    }
                                    break;
                            }
                            break;
                        case "copies":
                            dataset.copies = update
                            break;
                        case "pk":
                            dataset.pk = update
                            break;
                        case "udk":
                            dataset.udk = update
                            break;
                        case "label":
                            dataset.label = update
                            break;
                        case "labelValue":
                            dataset.labelValue = update
                            break;
                        case "domain":
                            dataset.domain = update
                            break;
                        case "view":
                            dataset.view = update
                            break;
                        case "loadQuery":
                            dataset.loadQuery = update
                            break;
                        case "reloadEvery":
                            dataset.reloadEvery = update
                            break;
                    }
                    break;
            }
        },
        addBatch: (state) => {
            state.config.batches.push({...emptyBatch});
        },
        removeBatch: (state, action) => {
            const idx = action.payload;
            state.config.batches = [...state.config.batches.slice(0, idx), ...state.config.batches.slice(idx + 1)]
        }
    },
    extraReducers: {
        [startBurnIn.fulfilled]: (state, action) => {
            const {running} = action.payload
            state.running = running
        },
        [stopBurnIn.fulfilled]: (state, action) => {
            const {running} = action.payload
            state.running = running
        },
    }
})

const {
    receivedEvents,
    updateStatus,
    setConfig,
    updateConfig,
    updateBatch,
    addBatch,
    removeBatch,
} = burnInSlice.actions

export const actions = {
    startBurnIn,
    stopBurnIn,
    receivedEvents,
    updateStatus,
    setConfig,
    updateConfig,
    updateBatch,
    addBatch,
    removeBatch,
}

export default burnInSlice.reducer;
