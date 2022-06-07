import React from "react";
import ReactTable from "react-table";
import cn from 'classnames';

import {dateTime, hhmmss_mmmddFormat, prettyTimeFromNanos} from '../../utility/burst-conversions';

const DateCell = ({value}) => dateTime(value, hhmmss_mmmddFormat);
const CommitCell = ({value, original}) => <span title={original.commitId}>{value}</span>;
const PingCell = ({value}) => prettyTimeFromNanos(value);

const stateCellProps = (state, row) => ({className: cn('text-center', 'node-state', row && row.row.state)});
const shaCellProps = (state, row) => ({className: cn('text-center', 'sha', {mismatch: row && row.original.mismatched})});
const shaAccessor = ({commitId}) => (commitId || '').slice(0, 7).toLocaleUpperCase();

const columns = [
    {Header: 'ID', accessor: 'nodeId', width: 50},
    {Header: 'PID', accessor: 'workerProcessId', width: 50},
    {Header: 'State', accessor: 'state', width: 100, getProps: stateCellProps},
    {Header: 'SHA', id: 'commit', width: 80, accessor: shaAccessor, Cell: CommitCell, getProps: shaCellProps},
    {Header: 'Name', accessor: 'nodeMoniker'},
    {Header: 'Node', accessor: 'nodeName'},
    {Header: 'Address', accessor: 'nodeAddress', width: 120},
    {Header: 'Connected', accessor: 'connectionTime', width: 165, Cell: DateCell},
    {Header: 'Last Message', accessor: 'lastUpdateTime', width: 165, Cell: DateCell},
    {Header: 'Ping', accessor: 'assessLatencyNanos', width: 80, Cell: PingCell, className: 'text-right'},
];

class WorkerTable extends React.Component {
    render() {
        const {workers, meta} = this.props;
        if (!meta.initialized || !workers.allIds.length) {
            return (
                <div className="w-100">
                    <div className="burst-empty-message">{
                        meta.initialized ? 'No workers in fabric yet...' : 'No data received from master'
                    }</div>
                </div>
            )
        }

        return (
            <div className="w-100">
                <ReactTable
                    columns={columns} data={workers.allIds.map(id => workers.byId[id])}
                    minRows={workers.allIds.length > 10 ? 10 : workers.allIds.length}
                    defaultPageSize={50}
                    pageSizeOptions={[25, 50, 100]}
                    className="-striped -highlight -scroll-headers" resizable={false}/>
            </div>
        );
    }
}

export default WorkerTable
