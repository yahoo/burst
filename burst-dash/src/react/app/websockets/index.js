import startProfilerWS from './profiler';
import startTorcherWS from './torcher';
import startWorkersWS from './workers';
import startExecutionWS from './execution';
import startThriftWS from './thrift';

const start = () => {
    startExecutionWS()
    startWorkersWS()
    startTorcherWS()
    startProfilerWS()
    startThriftWS()
}

export default start;
