import {createAsyncThunk, createSlice} from "@reduxjs/toolkit";
import request from "../../utility/api-requests";
import {get} from "../../utility/local-storage";

/** @type number */
let fetchHostHandle = undefined;
/** @type number */
let fetchScalingHandle = undefined;
/** @type number */
let fetchGitHandle = undefined;

const fetchHostInfo = createAsyncThunk('system/fetchHostInfo', async (ignored, thunk) => {
    try {
        return request('/info/hostInfo', {withProgress: false, method: 'GET'})
    } catch (e) {
        this.setState({online: false})
    }
});

const fetchScalingInfo = createAsyncThunk('system/fetchScalingInfo', async (ignored, thunk) => {
    try {
        return request('/info/keda', {withProgress: false, method: 'GET'})
    } catch (e) {
        this.setState({online: false})
    }
})

const fetchGitInfo = createAsyncThunk('system/fetchGitInfo', async (ignored, thunk) => {
    try {
        return request('/info/buildInfo', {withProgress: false, method: 'GET'})
    } catch (e) {
        this.setState({online: false})
    }
})

const startFetches = createAsyncThunk('system/toggleFetches', async (ignored, thunk) => {
    const globalState = thunk.getState()
    const { system } = globalState
    if (!fetchHostHandle) {
        thunk.dispatch(fetchHostInfo())
        fetchHostHandle = setInterval(() => {
            if (system.hostInfoRunning) {
                thunk.dispatch(fetchHostInfo())
            }
        }, 15000)
    }
    if (!fetchScalingHandle) {
        thunk.dispatch(fetchScalingInfo())
        fetchScalingHandle = setInterval(() => {
            if (system.hostInfoRunning) {
                thunk.dispatch(fetchScalingInfo())
            }
        }, 5000)
    }
    if (!fetchGitHandle) {
        thunk.dispatch(fetchGitInfo())
        fetchGitHandle = setInterval(() => {
            if (system.hostInfoRunning) {
                thunk.dispatch(fetchGitInfo())
            }
        }, 60000)
    }
})

const stopFetches = createAsyncThunk('system/stopFetches', async (ignored, thunk) => {
    if (fetchHostHandle) {
        clearInterval(fetchHostHandle)
        fetchHostHandle = undefined
    }
    if (fetchScalingHandle) {
        clearInterval(fetchScalingHandle)
        fetchScalingHandle = undefined
    }
    if (fetchGitHandle) {
        clearInterval(fetchGitHandle)
        fetchGitHandle = undefined
    }
})

const systemSlice = createSlice({
    name: 'system',
    initialState: {
        online: false,
        hostInfoRunning: get('hostInfoRunning', true),
        hostInfo: {},
        scalingInfo: {},
        gitInfo: {},
    },
    extraReducers: builder => builder
        .addCase(fetchHostInfo.fulfilled, (state, action) => {
            state.hostInfo = action.payload;
            state.online = true;
        })
        .addCase(fetchHostInfo.rejected, (state, action) => {
            state.online = false;
        })
        .addCase(fetchScalingInfo.fulfilled, (state, action) => {
            state.scalingInfo = action.payload;
        })
        .addCase(fetchGitInfo.fulfilled, (state, action) => {
            state.gitInfo = action.payload;
        })
        .addCase(startFetches.fulfilled, (state, action) => {
            state.hostInfoRunning = !state.hostInfoRunning
        })
});

export const actions = {
    fetchHostInfo,
    fetchScalingInfo,
    fetchGitInfo,
    startFetches,
    stopFetches,
}

const getHostState = fn => globalState => fn(globalState[systemSlice.name]);
export const selectors = {
    getHostState: getHostState(s => s),
    online: getHostState(state => state.online),
    hostInfoRunning: getHostState(state => state.hostInfoRunning),
    hostInfo: getHostState(state => state.hostInfo),
    scalingInfo: getHostState(state => state.scalingInfo),
    gitInfo: getHostState(state => state.gitInfo),
}

export const reducer = systemSlice.reducer

export default systemSlice;
