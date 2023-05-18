import {configureStore} from "@reduxjs/toolkit";

import catalog from './reducers/catalog';
import data from './reducers/data';
import query from './reducers/query';
import workerTab from './reducers/workers';
import execution from './reducers/execution';
import thrift from './reducers/thrift'
import burnIn from './reducers/burn-in'
import crosscutting from './reducers/crosscutting';
import settings from './reducers/settings';

export default configureStore({
    reducer: {
        catalog,
        crosscutting,
        data,
        execution,
        query,
        thrift,
        workerTab,
        settings,
        burnIn,
    },
    devTools: {
        actionsDenyList: ['UPDATE_WORKER_LIST']
    }
})
