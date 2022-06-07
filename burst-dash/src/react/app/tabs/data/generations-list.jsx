import React, {Component} from "react";
import ReactTable from "react-table";
import cn from "classnames";

import {byteCount, commaNumber, dateTime, elapsedTime} from "../../utility/burst-conversions";
import {genKeyString} from "../../store/reducers/data";

const LoadCell = ({loadAt, loadTook, count}) => (
    <div className="d-flex justify-content-between px-2">
        {count && <span>{count}x </span>}
        <span style={{minWidth: 70, textAlign: 'left'}}>{elapsedTime(loadTook)}</span>
        <span>@</span>
        <span style={{minWidth: 150, textAlign: 'right'}}>{dateTime(loadAt)}</span>
    </div>
);

const generationColumns = [
    {
        Header: 'Identity',
        columns: [
            {Header: 'Domain', accessor: 'generationKey.domainKey', width: 100},
            {Header: 'View', accessor: 'generationKey.viewKey', width: 100},
            {
                Header: 'Generation',
                accessor: 'generationKey.generationClock',
                width: 160,
                Cell: ({value}) => <span title={dateTime(value)}>{value}</span>
            },
        ]
    },
    {
        Header: 'State',
        columns: [
            {
                accessor: 'state',
                getProps: (state, row) => ({className: cn('text-center', row && row.row.state)}),
                width: 75
            }
        ]
    },
    {
        Header: 'Metrics',
        columns: [
            {Header: 'Items', accessor: 'metrics.itemCount', Cell: ({value}) => commaNumber(value)},
            {Header: 'Slices', accessor: 'metrics.sliceCount', Cell: ({value}) => commaNumber(value), minWidth: 50},
            {Header: 'Regions', accessor: 'metrics.regionCount', Cell: ({value}) => commaNumber(value), minWidth: 75},
            {Header: 'Bytes', accessor: 'metrics.byteCount', Cell: ({value}) => byteCount(value)},
            {
                Header: 'Cold Load',
                accessor: 'metrics.coldLoadAt',
                Cell: ({original: {metrics}}) =>
                    <LoadCell loadAt={metrics.coldLoadAt} loadTook={metrics.coldLoadTook}/>,
                width: 250
            },
            {Header: 'Warm Loads', accessor: 'metrics.warmLoadCount', Cell: ({value}) => commaNumber(value)},
            {Header: () => <>Skew<sub>t</sub></>, accessor: 'metrics.timeSkew', Cell: ({value}) => value.toFixed(2), width: 60},
            {Header: () => <>Skew<sub>s</sub></>, accessor: 'metrics.sizeSkew', Cell: ({value}) => value.toFixed(2), width: 60},
        ]
    }
];

const sliceColumns = [
    {
        Header: 'Identity',
        columns: [
            {Header: 'Slice', accessor: 'sliceKey', width: 50},
            {Header: 'Worker', accessor: 'hostname'}
        ]
    },
    {
        Header: 'State',
        columns: [
            {
                accessor: 'state',
                getProps: (state, row) => ({className: cn('text-center', row && row.row.state)}),
                width: 75
            }
        ]
    },
    {
        Header: 'Metrics',
        columns: [
            {Header: 'Items', accessor: 'metrics.itemCount', Cell: ({value}) => commaNumber(value), minWidth: 60},
            {Header: 'pItems', accessor: 'metrics.potentialItemCount', Cell: ({value}) => commaNumber(value), minWidth: 60},
            {Header: 'Regions', accessor: 'metrics.regionCount', Cell: ({value}) => commaNumber(value), minWidth: 50},
            {Header: 'Bytes', accessor: 'metrics.byteCount', Cell: ({value}) => byteCount(value), minWidth: 50},
            {
                Header: 'Cold Load',
                accessor: 'metrics.coldLoadAt',
                Cell: ({original: {metrics}}) =>
                    <LoadCell loadAt={metrics.coldLoadAt} loadTook={metrics.coldLoadTook}/>,
                width: 250
            },
            {
                Header: 'Warm Load',
                accessor: 'metrics.warmLoadAt',
                Cell: ({original: {metrics}}) =>
                    <LoadCell count={metrics.warmLoadCount} loadAt={metrics.warmLoadAt} loadTook={metrics.warmLoadTook}/>,
                width: 300
            },
        ]
    }
];

/**
 * The list/table containing the fetched generation rows
 */
class GenerationsList extends Component {
    state = {
        expanded: {},
        fetching: {},
    };

    expandRow = (expanded) => {
        this.setState({expanded});
    };

    setFetching = (key, isFetching = true) => {
        const {fetching} = this.state;
        const wasFetching = fetching.hasOwnProperty(key);
        if (isFetching) {
            fetching[key] = true;
        } else {
            delete fetching[key];
        }
        if (wasFetching !== isFetching) {
            this.setState(fetching);
        }
        return wasFetching;
    };

    fetch = async (generationKey) => {
        const key = genKeyString(generationKey);
        const fetching = this.setFetching(key);
        if (!fetching) {
            this.props.onSelect(generationKey);
        }
    };

    flush = ({data: generationKey}) => this.props.flushGeneration(generationKey());

    evict = ({data: generationKey}) => this.props.evictGeneration(generationKey());

    render() {
        const {generations, slices: allSlices} = this.props;

        return (
            <ReactTable
                columns={generationColumns} data={generations}
                defaultPageSize={50}
                pageSizeOptions={[50, 100, 500]}
                onExpandedChange={this.expandRow}
                expanded={this.state.expanded}
                SubComponent={({original}) => {
                    const genKey = genKeyString(original.generationKey);
                    const {[genKey]: slices = []} = allSlices;
                    if (!slices.length) {
                        this.fetch(original.generationKey);
                        return (
                            <div className="burst-empty-message small">Loading Slices…</div>
                        )
                    }

                    return (
                        <ReactTable
                            columns={sliceColumns} data={slices}
                            pageSize={slices.length > 10 ? 10 : slices.length}
                            renderPageSizeOptions={() => null}
                            className="-striped mx-auto w-95"/>
                    );
                }}
                className="-striped -hover -scroll-headers -scroll" resizable={false}/>
        );
    }
}

export default GenerationsList
