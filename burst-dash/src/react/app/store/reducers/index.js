/**
 * This is the entry point for the store to find reducers.
 *
 * To add a reducer to the application, import it here and then export it.
 * The reducer's export name will become the name it has in the redux state object.
 */

import {burstMasterApplication as legacyState} from "./legacyState";
import catalog from './catalog';
import data from './data';
import queryTab from './query';
import profilerTab from './profiler';
import torcherTab from './torcher';
import workerTab from './workers';
import executionTab from './execution';
import crosscutting from './crosscutting';

export {
    legacyState,
    queryTab,
    profilerTab,
    torcherTab,
    workerTab,
    catalog,
    data,
    executionTab,
    crosscutting
};
