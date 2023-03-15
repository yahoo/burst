import {configureStore} from "@reduxjs/toolkit";

import catalog from './reducers/catalog';
import data from './reducers/data';
import query from './reducers/query';
import profiler from './reducers/profiler';
import torcherTab from './reducers/torcher';
import workerTab from './reducers/workers';
import execution from './reducers/execution';
import thrift from './reducers/thrift'
import crosscutting from './reducers/crosscutting';
import settings from './reducers/settings';

export default configureStore({
    reducer: {
        catalog,
        crosscutting,
        data,
        execution,
        profiler,
        query,
        thrift,
        torcherTab,
        workerTab,
        settings,
    },
    devTools: {
        actionsBlacklist: ['UPDATE_WORKER_LIST']
    }
})
