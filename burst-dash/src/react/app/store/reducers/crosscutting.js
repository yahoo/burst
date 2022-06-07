import {createAsyncThunk, createSlice} from "@reduxjs/toolkit";
import request from "../../utility/api-requests";

export function createSparseActionFactory(type, payload, keys) {
    return params => {
        const update = {};
        keys.forEach(k => {
            if (params.hasOwnProperty(k)) update[k] = params[k];
        });
        return {type, [payload]: update};
    };
}

const clearNotification = createAsyncThunk('global/clear-message', async (after = 4000) => {
    await new Promise(resolve => setTimeout(() => resolve(), after))
})

const globalSlice = createSlice({
    name: 'global',
    initialState: {
        message: {title: '', message: '', variant: ''},
        selectedDataset: {domain: 0, view: 0}
    },
    reducers: {
        displayError: {
            reducer: (state, action) => {
                state.message = action.payload
            },
            prepare: (error, title, ...toLog) => {
                console.log(...toLog, error)
                return {
                    payload: {title, message: error.message, variant: 'warning'}
                }
            }
        },
        displayMessage: {
            reducer: (state, action) => {
                state.message = action.payload
            },
            prepare: (message, title, variant = 'warning', ...toLog) => {
                console.log(...toLog)
                return {
                    payload: {title, message, variant}
                }
            }
        },
        selectDataset: (state, action) => {
            const {domain = {}, view = {}} = action.payload
            if (view.pk) {
                state.selectedDataset = {domain: view.domainFk ?? domain.pk, view: view.pk}
            } else if (domain.pk) {
                state.selectedDataset = {domain: domain.pk, view: 0}
            } else {
                state.selectedDataset = {domain: 0, view: 0}
            }
        }
    },
    extraReducers: builder => builder
        .addCase(clearNotification.fulfilled, (state) => {
            state.message = {title: '', message: '', variant: ''}
        })
})

const {selectDataset, displayMessage, displayError} = globalSlice.actions

const fetchDomain = createAsyncThunk('catalog/fetchDomain', async (pkOrUdk, thunk) => {
    try {
        const domain = await request(`/catalog/domains/${pkOrUdk}`, {method: 'GET'});
        if (domain.success === false) {
            throw new Error(domain.message)
        }
        return domain;
    } catch (e) {
        thunk.dispatch(displayError(e, 'Failed to fetch domain'))
        throw e;
    }
})
const fetchView = createAsyncThunk('catalog/fetchView', async (pkOrUdk, thunk) => {
    try {
        const view =  await request(`/catalog/views/${pkOrUdk}`, {method: 'GET'})
        if (view.success === false) {
            throw new Error(view.message)
        }
        return view;
    } catch (e) {
        thunk.dispatch(displayError(e, 'Failed to fetch view'))
        throw e;
    }
})
const fetchViewForDomain = createAsyncThunk('catalog/fetchViewForDomain', async ({domain, view}, thunk) => {
    try {
        const response =  await request(`/catalog/domains/${domain}/${view}`, {method: 'GET'})
        if (response.success === false) {
            throw new Error(response.message)
        }
        return response;
    } catch (e) {
        thunk.dispatch(displayError(e, `Failed to fetch view ${view} in domain ${domain}`))
        throw e;
    }
})

export const actions = {
    selectDataset,
    clearNotification,
    displayMessage,
    displayError,
    fetchDomain,
    fetchView,
    fetchViewForDomain,
}

export default globalSlice.reducer;
