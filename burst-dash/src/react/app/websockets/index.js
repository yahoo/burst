import startWorkersWS from './workers';
import startExecutionWS from './execution';
import startThriftWS from './thrift';
import startBurnInWS from './burn-in';

const start = () => {
    startExecutionWS()
    startWorkersWS()
    startThriftWS()
    startBurnInWS()
}

export default start;
