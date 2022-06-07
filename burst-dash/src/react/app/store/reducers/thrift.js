import {createSlice} from "@reduxjs/toolkit";

const findRequest = (state, ruid) => state.requests.findIndex(r => r.ruid === ruid)

const INITIAL_SORT_BY = {id: 'timestamp', desc: true}
const thrift = createSlice({
    name: 'thrift',
    initialState: {
        requests: [],
        filters: [],
        sortBy: [INITIAL_SORT_BY]
    },
    reducers: {
        allRequests: (state, action) => {
            state.requests = action.payload
        },
        receivedRequest: (state, action) => {
            const {payload: ruid} = action
            state.requests.unshift({ruid})
        },
        updateRequest: (state, action) => {
            const {payload: req} = action
            const idx = findRequest(state, req.ruid)
            if (idx !== -1) {
                state.requests[idx] = req
            }
        },
        requestException: {
            reducer: (state, action) => {
                const {ruid, error} = action.payload
                const idx = findRequest(state, ruid)
                if (idx !== -1) {
                    state.requests[idx].status = 'ExceptionStatus'
                    state.requests[idx].error = error
                }
            },
            prepare: (ruid, error) => {
                return {payload: {ruid, error}}
            }
        },
        toggleExpanded: (state, action) => {
            const ruid = action.payload;
            const idx = findRequest(state, ruid);
            if (idx !== -1) {
                state.requests[idx].expanded = !state.requests[idx].expanded
            }
        },
        setFilters: (state, action) => {
            state.filters = action.payload
        },
        setSortBy: (state, action) => {
            state.sortBy = action.payload
            if (state.sortBy.length === 0) {
                state.sortBy = [INITIAL_SORT_BY]
            }
        },
    }
})

const {
    allRequests,
    receivedRequest,
    updateRequest,
    requestException,
    toggleExpanded,
    setFilters,
    setSortBy,
} = thrift.actions
export const actions = {
    allRequests,
    receivedRequest,
    updateRequest,
    requestException,
    toggleExpanded,
    setFilters,
    setSortBy,
}
export default thrift.reducer
