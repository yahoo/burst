import request, {asJson} from '../../utility/api-requests';
import {actions as crosscutting} from './crosscutting';
import {createAsyncThunk, createSlice} from "@reduxjs/toolkit";

/*
 * Action names
 */
const FETCHED_DOMAINS = 'FETCHED_DOMAINS';
const FETCHED_VIEWS = 'FETCHED_VIEWS';
const FETCHED_VIEW = 'FETCHED_VIEW';
const CREATED_ENTITY = 'CREATED_ENTITY';

/*
 * Helpers
 */
const mapByPk = (list) => list.reduce((collect, entry) => {
    collect[entry.pk] = entry;
    return collect;
}, {});

/*
 * Actions
 */
const validateMotif = createAsyncThunk('catalog/validateMotif', async ({schemaName, motif}, thunk) => {
    try {
        const json = await request('/catalog/validateMotif', {parameters: {motif, schemaName}})
        if (json.success) {
            thunk.dispatch(crosscutting.displayMessage('Valid motif', 'Success', 'success'))
        } else {
            thunk.dispatch(crosscutting.displayMessage(json.message || 'Unknown failure validating motif', 'Failure', 'danger'))
        }
    } catch (e) {
        console.log('validateMotifResponseFailure', e);
        thunk.dispatch(crosscutting.displayError(e, 'Http Error'))
    }
})

const getDomains = (forIds = null, dispatch) =>
    request(`/catalog/domains${forIds ? `?pks=${forIds.join(',')}` : ''}`, {method: 'GET', withProgress: false})
        .then(json => dispatch({type: FETCHED_DOMAINS, domains: mapByPk(json)}));

/**
 * Fetches one or more domains. Domains will be fetched in batches of up to 100
 * @param forIds - the pks of the domains to fetch, all domains fetched if this parameter is omitted
 */
export function fetchDomains(forIds = null) {
    return dispatch => {
        const needsSplit = forIds && forIds.length > 100;
        if (!needsSplit) {
            return getDomains(forIds, dispatch);
        }

        const reqs = [];
        while (forIds.length > 100) {
            reqs.push(forIds.splice(0, 100));
        }
        reqs.push(forIds);
        return reqs.reduce(
            (promise, ids) => promise.then(() => getDomains(ids, dispatch)),
            Promise.resolve()
        );
    }
}

/**
 * Fetches all the views for the specified domain
 * @param domain - the domain's pk
 */
export function fetchViewsForDomain(domain) {
    return dispatch => {
        request(`/catalog/domains/${domain}/views`, {method: 'GET'})
            .then(json => dispatch({type: FETCHED_VIEWS, domain, views: mapByPk(json)}));
    }
}

const search = createAsyncThunk('catalog/search', async ({domain, view, limit = 200, offset}, thunk) => {
    try {
        return await request('/catalog/search', {parameters: {domain, view, limit, offset}})
    } catch (e) {
        thunk.dispatch(crosscutting.displayError(e, 'Failed to fetch search tree'))
    }
})

const save = createAsyncThunk('catalog/save', async ({type, entity}, thunk) => {
    try {
        const response = await request(`/catalog/update${type[0].toUpperCase()}${type.slice(1)}`, asJson(entity))
        return {type, [type]: response};
    } catch (e) {
        thunk.dispatch(crosscutting.displayError(e, `Failed to save ${type}`))
    }
})

const createView = createAsyncThunk('catalog/newView', async ({domainPk}, thunk) => {
    try {
        const now = new Date()
        const parameters = {domainPk, moniker: `New View ${now.toLocaleString()}`, schemaName: 'unity'};
        const response = await request('/catalog/newView', {parameters})
        if (response.pk) {
            thunk.dispatch(crosscutting.selectDataset(({domain: {pk: response.domainFk}, view: response})))
            return response
        } else {
            dispatch(crosscutting.displayMessage(response.message, 'Failed to create view'));
            thunk.rejectWithValue(response.message);
        }
    } catch (e) {
        thunk.dispatch(crosscutting.displayError(e, 'Failed to create new view'))
        throw e;
    }
})

const bumpGenerationClock = createAsyncThunk('catalog/revViewGC', async ({view}, thunk) => {
    try {
        const response = await request(`/catalog/updateView`, asJson(view))
        if (response.success === false) {
            dispatch(crosscutting.displayMessage(response.message, 'Failed to update view'));
        } else {
            return response
        }
    } catch (e) {
        thunk.dispatch(crosscutting.displayError(e, 'Failed to update generation clock'))
        throw e
    }

})

/*
 * Reducers
 */
const cleanEditorState = (type = null, entity = {}) => ({
    type,
    pk: entity?.pk ?? 0,
    saving: false,
    modified: false,
    entity,
});
const fetchedView = (state, action) => {
    const view = action.payload;
    state.views[view.pk] = view
    if (state.editor.type === 'view') {
        state.editor = cleanEditorState('view', view)
    }
    const dIdx = state.tree.findIndex(d => d.pk === view.domainFk)
    if (dIdx === -1) {
        return
    }
    const vIdx = state.tree[dIdx]?.children.findIndex(v => v.pk === view.pk)
    if (vIdx !== -1) {
        state.tree[dIdx].children[vIdx].moniker = view.moniker
    } else {
        state.tree[dIdx].children.push({pk: view.pk, moniker: view.moniker, udk: view.udk})
    }
}
const catalogSlice = createSlice({
    name: 'catalog',
    initialState: {
        searchPending: false,
        tree: [],
        expanded: [],
        selected: {type: null, pk: 0},
        editor: cleanEditorState(),
        domains: {},
        views: {},
    },
    reducers: {
        toggleDomain: (state, action) => {
            const {domain: {pk}} = action.payload;
            const idx = state.expanded.indexOf(pk)
            if (idx === -1) {
                state.expanded.push(pk);
            } else {
                state.expanded.splice(idx, 1);
            }
        },
        edit: (state, action) => {
            const {prop, value} = action.payload
            state.editor.modified = true;
            state.editor.entity[prop] = value;
        }
    },
    extraReducers: {
        [crosscutting.selectDataset]: (state, action) => {
            const {domain, view} = action.payload;
            if (domain && !state.expanded.includes(domain.pk)) {
                state.expanded.push(domain.pk)
            }
            if (view) {
                state.editor = cleanEditorState('view', state.views?.[view.pk] ?? {pk: view.pk})
            } else if (domain) {
                state.editor = cleanEditorState('domain', state.domains?.[domain.pk] ?? {pk: domain.pk})
            } else {
                state.editor = cleanEditorState()
            }
        },
        [search.pending]: (state) => {
            state.searchPending = true;
        },
        [search.fulfilled]: (state, action) => {
            state.searchPending = false;
            const domainList = action.payload.sort((l, r) => l.moniker < r.moniker ? -1 : 1)
            state.tree = domainList
            domainList.forEach(({pk, moniker, children}) => {
                state.domains[pk] = {pk, moniker}
                children.forEach(({pk: viewPk, moniker}) => {
                    state.views[viewPk] = {pk: viewPk, moniker, domainFk: pk};
                })
            })
        },
        [search.rejected]: (state) => {
            state.searchPending = false;
        },
        [crosscutting.fetchDomain.fulfilled]: (state, action) => {
            const domain = action.payload
            state.domains[domain.pk] = domain
            const idx = state.tree.findIndex(d => d.pk === domain.pk)
            if (idx !== -1) {
                state.tree[idx].moniker = domain.moniker;
            } else {
                const {pk, moniker, udk} = domain
                state.tree.push({pk, moniker, udk, children: []})
            }
            if (state.editor.type === 'domain' && state.editor.pk === domain.pk) {
                state.editor = cleanEditorState('domain', domain)
            }
        },
        [crosscutting.fetchView.fulfilled]: fetchedView,
        [crosscutting.fetchViewForDomain.fulfilled]: fetchedView,
        [save.pending]: (state) => {
            state.editor.saving = true;
        },
        [save.fulfilled]: (state, action) => {
            const {type, domain, view} = action.payload;
            state.editor = cleanEditorState(type, type === 'domain' ? domain : view)
            if (type === 'domain') {
                state.domains[domain.pk] = domain;
                const idx = state.tree.findIndex(d => d.pk === domain.pk)
                state.tree[idx].moniker = domain.moniker;
            } else if (type === 'view') {
                state.views[view.pk] = view;
                const dIdx = state.tree.findIndex(d => d.pk === view.domainFk)
                const vIdx = state.tree[dIdx].children.findIndex(v => v.pk === view.pk)
                state.tree[dIdx].children[vIdx].moniker = view.moniker
            }
        },
        [save.rejected]: (state) => {
            state.editor.saving = false;
        },
        [createView.fulfilled]: (state, action) => {
            const {payload: view} = action
            state.tree.forEach(d => {
                if (d.pk === view.domainFk) {
                    d.children.push({pk: view.pk, moniker: view.moniker})
                }
            })
            state.views[view.pk] = view
        },
        [bumpGenerationClock.fulfilled]: (state, action) => {
            const {payload: view} = action;
            state.views[view.pk] = view;
            state.editor = cleanEditorState('view', view)
        }
    }
})

const {toggleDomain, edit} = catalogSlice.actions

export const actions = {
    toggleDomain,
    validateMotif,
    search,
    edit,
    save,
    createView,
    bumpGenerationClock,
}

export default catalogSlice.reducer;
