import request, {asJson, JsonMimeTypeHeader} from '../../utility/api-requests';
import {createAsyncThunk, createSlice} from "@reduxjs/toolkit";
import {actions as crosscutting} from "./crosscutting";

const loadSettings = createAsyncThunk('settings/load', async (ignored, thunkApi) => {
    const {dispatch} = thunkApi;
    try {
        const response = await request('/info/settings', {method: 'GET'})
        return Object.keys(response).sort()
            .map(k => {
                const entry = response[k];
                if (k !== "burst.fabric.cache.spindles") {
                    return {key: k, ...entry}
                } else {
                    return {key: k, ...entry, value: entry.value.split(";").join(";\n")}
                }
            });
    } catch (e) {
        dispatch(crosscutting.displayError(e, `Failed to fetch settings`))
    }
})

const saveSetting = createAsyncThunk('settings/save', async ({key, value}, thunkApi) => {
    const {dispatch} = thunkApi;
    try {
        const response = await request('/info/settings', asJson({key, value}))
        dispatch(loadSettings())
        dispatch(clearEdit({key}))
    } catch (e) {
        dispatch(crosscutting.displayError(e, `Failed to update setting ${key}`))
    }
})

const settingsSlice = createSlice({
    name: 'settings',
    initialState: {
        filter: '',
        settings: [],
        editing: {},
    },
    reducers: {
        setFilter: (state, action) => {
            state.filter = action.payload;
        },
        updateEdit: (state, action) => {
            const {key, value} = action.payload;
            state.editing[key] = value;
        },
        clearEdit: (state, action) => {
            const {key} = action.payload;
            delete state.editing[key]
        },
    },
    extraReducers: {
        [loadSettings.fulfilled]: (state, action) => {
            state.settings = action.payload;
        }
    }
});

const {
    updateEdit,
    clearEdit,
    setFilter,
} = settingsSlice.actions;

export const actions = {
    loadSettings,
    saveSetting,
    updateEdit,
    clearEdit,
    setFilter,
}

export const selectors = {
    selectSettings: state => state.settings,
    selectFilteredSettings: state => {
        const {settings, filter} = state.settings
        return settings.filter(s => !filter || s.key.includes(filter) || `${s.value}`.includes(filter) || s.description.includes(filter))
    }
}

export default settingsSlice.reducer
