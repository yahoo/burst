import request, {asJson, maxFetchSize} from '../../utility/api-requests';
import {actions as crosscutting} from "./crosscutting";
import {createAsyncThunk, createSlice} from "@reduxjs/toolkit";

const sortQueries = (left, right) => {
    if (left.languageType === right.languageType) {
        return left.moniker < right.moniker ? -1 : 1;
    }
    switch (left.languageType) {
        case 'Eql':
            return -1;
        case 'Silq':
            return right.languageType === 'Eql' ? 1 : -1;
        case 'Hydra':
            return right.languageType === 'Gist' ? -1 : 1;
        case 'Gist':
            return 1;
    }
    return -1;
}

const fetchTimezones = createAsyncThunk('query/fetchTimezones', async (ignored, thunk) => {
    try {
        return await request('/info/timezones', {method: 'GET'})
    } catch(e) {
        thunk.dispatch(crosscutting.displayError(e, 'Failed to fetch timezones', error.message))
        throw e
    }
});

const fetchQueries = createAsyncThunk('query/fetchSavedQueries', async (ignored, thunk) => {
    try {
        return await request('/catalog/allQueries', {parameters: {limit: maxFetchSize}})
    } catch (e) {
        thunk.dispatch(crosscutting.displayError(e, 'Failed to load queries', 'LoadQueryList -'))
        throw e
    }
})

const createQuery = createAsyncThunk('query/createQuery', async ({text, moniker, language}, thunk) => {
    try {
        const parameters = {moniker, language, source: text}
        const response = await request('/catalog/newQuery', {parameters})
        if (response.success === false) {
            throw new Error(response.message)
        }
        return response;
    } catch(e) {
        thunk.dispatch(crosscutting.displayError(e, 'Failed to create query', 'CreateQuery -'))
        throw e
    }
})

const saveQuery = createAsyncThunk('query/saveQuery', async (query, thunk) => {
    try {
        const response = await request('/catalog/updateQuery', asJson(query))
        if (response.success === false) {
            throw new Error(response.message)
        }
        return response
    } catch (e) {
        thunk.dispatch(crosscutting.displayError(e, 'Failed to save query', 'SaveQuery -'))
        throw e
    }
})

const deleteQuery = createAsyncThunk('query/deleteQuery', async (pk, thunk) => {
    try {
        const response = await request('/catalog/deleteQuery', {parameters: {pk}})
        if (response.success === false) {
            throw new Error(response.message)
        }
        return {pk};
    } catch (e) {
        thunk.dispatch(crosscutting.displayError(e, 'Failed to delete query', 'DeleteQuery -'))
    }
})

const execute = createAsyncThunk('query/execute', async (execution, thunk) => {
    const {timezone, domain, view, source, args} = execution;
    const parameters = {timezone, domain, view, source, args}
    try {
        return await request('/query/executeGroup', {parameters})
    } catch (e) {
        thunk.dispatch(crosscutting.displayError(e, 'Failed to execute query', 'ExecuteQuery -'))
        throw e
    }
})

const querySlice = createSlice({
    name: 'query',
    initialState: {
        // runtime
        text: '',
        params: '',
        timezone: 'UTC',

        // for persistence
        saving: false,
        savedQuery: {
            pk: 0,
            moniker: 'New Query',
            language: 'Eql',
        },
        allQueries: [],

        timezones: [],

        // results
        executing: false,
        execution: null,
        executions: [],
    },
    reducers: {
        setText: (state, action) => {
            state.text = action.payload
        },
        setParams: (state, action) => {
            state.params = action.payload;
        },
        setTimezone: (state, action) => {
            state.timezone = action.payload
        },
        setSavedQuery: (state, action) => {
            const query = action.payload
            state.savedQuery = query
            state.text = query.source
        }
    },
    extraReducers: {
        [fetchTimezones.fulfilled]: (state, action) => {
            state.timezones = action.payload;
        },
        [fetchQueries.fulfilled]: (state, action) => {
            state.allQueries = action.payload.sort(sortQueries)
        },
        [createQuery.pending]: (state) => {
            state.saving = true
        },
        [createQuery.rejected]: (state) => {
            state.saving = false
        },
        [createQuery.fulfilled]: (state, action) => {
            state.saving = false
            state.savedQuery = action.payload
            state.allQueries.push(action.payload)
            state.allQueries.sort(sortQueries)
        },
        [saveQuery.pending]: (state) => {
            state.saving = true
        },
        [saveQuery.rejected]: (state) => {
            state.saving = false
        },
        [saveQuery.fulfilled]: (state, action) => {
            const query = action.payload
            state.saving = false
            state.savedQuery = query
            const idx = state.allQueries.findIndex(q => q.pk === query.pk)
            if (idx !== -1) {
                state.allQueries[idx] = query;
            }
        },
        [deleteQuery.fulfilled]: (state, action) => {
            const {pk} = action.payload
            state.allQueries = state.allQueries.filter(q => q.pk !== pk)
            state.savedQuery = {pk: 0, moniker: 'New Query', language: 'Eql'}
            state.text = ''
        },
        [execute.pending]: (state) => {
            state.executing = true
            state.execution = null
        },
        [execute.rejected]: (state, action) => {
            state.executing = false
        },
        [execute.fulfilled]: (state, action) => {
            state.executing = false
            state.execution = action.payload
        },
    }
});

const {setText, setParams, setTimezone, setSavedQuery} = querySlice.actions;

export default querySlice.reducer;

export const actions = {
    setText,
    setParams,
    fetchTimezones,
    setTimezone,
    setSavedQuery,
    fetchQueries,
    createQuery,
    saveQuery,
    deleteQuery,
    execute,
}
