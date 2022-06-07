import {createSlice} from "@reduxjs/toolkit";

const DEFAULT_SORT_BY = {id: 'beginMillis', desc: true};
const waveSlice = createSlice({
    name: 'execution',
    initialState: {
        since: 0,
        executions: {},
        filters: [],
        sortBy: [DEFAULT_SORT_BY],
        particleFilters: [],
        particleSortBy: [DEFAULT_SORT_BY]
    },
    reducers: {
        reset: (state, action) => {
            state.since = action.payload;
            state.executions = {}
        },
        postUpdates: (state, action) => {
            const updates = action.payload
            updates.forEach(u => {
                const {particles = []} = state.executions?.[u.guid]?.wave ?? {}
                if (u?.wave?.particles?.length < particles.length) {
                    u.wave.particles = particles
                }
                state.executions[u.guid] = u
            })
        },
        detailsFetched: (state, action) => {
            const {guid, request} = action.payload;
            state.executions[guid] = request
        },
        setFilters: (state, action) => {
            state.filters = action.payload
        },
        setSortBy: (state, action) => {
            state.sortBy = action.payload
            if (state.sortBy.length === 0) {
                state.sortBy = [DEFAULT_SORT_BY]
            }
        },
        setParticleFilters: (state, action) => {
            state.particleFilters = action.payload
        },
        setParticleSortBy: (state, action) => {
            state.particleSortBy = action.payload
        },
    }
})

export default waveSlice.reducer;

const { reset, postUpdates, detailsFetched, setFilters, setSortBy, setParticleFilters, setParticleSortBy } = waveSlice.actions;

export const actions = {
    reset,
    postUpdates,
    detailsFetched,
    setFilters,
    setSortBy,
    setParticleFilters,
    setParticleSortBy,
}
