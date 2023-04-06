import React, {useEffect, useRef} from "react";
import ReactTable from "react-table";
import {Alert, Button} from "react-bootstrap";
import {useDispatch, useSelector} from "react-redux";
import {useFilters, usePagination, useSortBy, useTable} from "react-table-7";

import {StateSummary} from "./execution-tab";
import {ColSort, DateCell, GuidCell, TextFilter} from "../../components/table-cells";
import {dateTime, elapsedTime} from "../../utility/burst-conversions";
import {getRequestDetails} from "../../websockets/execution";
import {DownloadLink} from "../../layout/helpers";
import {ToggleItem} from "../../components/toggle-item"
import {ExecutionTimeline} from "./execution-timeline"
import {
    DurationCell,
    filterState,
    PARTICLE_STATES,
    ParticleStatusCell,
    ParticleStatusFilter,
    sortDuration
} from "./cells";
import {actions as crosscutting} from '../../store/reducers/crosscutting';
import {actions as query} from '../../store/reducers/query'
import {useHistory} from "react-router-dom";
import OverLink from "../../components/over-link";
import {Item, Row} from "../../components/helpers";
import {GenerationMetrics} from "../../components/generation-metrics";
import {ExecutionMetrics} from "../../components/execution-metrics";
import {actions as execution} from "../../store/reducers/execution";
import TablePagination from "../../components/table-pagination";


const ExecutionView = ({guid}) => {
    const dispatch = useDispatch()
    const history = useHistory()
    const {execution} = useSelector(({execution}) => ({
        execution: execution.executions[guid]
    }))
    const execRef = useRef(null)
    const summaryKey = Object.entries(execution?.wave?.summary ?? {noWave: 1})
        .map(([state, count]) => `${state}:${count}`)
        .join('_');
    useEffect(() => {
        if (execution) {
            execRef.current = execution
        }
        getRequestDetails(guid)
    }, [guid, execution?.guid === guid, summaryKey, execution?.wave?.particles?.length])

    if (execution === null && !execRef.current) {
        return (
            <div className="p-2">
                Details for {guid} are no longer available.<br/>
            </div>
        )
    } else if (!execution && !execRef.current) {
        return (
            <div className="p-2">
                Loading…
            </div>
        )
    }

    const data = execRef.current ?? execution
    const {state, over, parameters, beginMillis, endMillis, status: message = '', source = []} = data;
    const paramsObj = parameters.reduce((all, p) => {
        all[p.name] = p.value
        return all
    }, {});
    const paramsJson = parameters.length > 0 ? JSON.stringify(paramsObj, null, 2) : ''

    const wave = data?.wave ?? {}
    const {skew, state: waveState, beginMillis: waveBeginMs, endMillis: waveEndMs, particles = []} = wave;
    const {generationMetrics = {}, executionMetrics = {}} = wave.metrics ?? {}; // metrics is Optional, None is sent as null
    const openQuery = async (text) => {
        dispatch(query.setParams(paramsJson))
        dispatch(query.setText(text))
        history.push('/query')
        await dispatch(crosscutting.fetchDomain(over.domainKey))
        await dispatch(crosscutting.fetchView(over.viewKey))
        await dispatch(crosscutting.selectDataset({domain: {pk: over.domainKey}, view: {pk: over.viewKey}}))
    }

    const downloadLink = <DownloadLink text="Save execution as json" filename={`exec_${guid}.json`} json={data}/>
    return (
        <div className="w-100 p-2">
            {!execution && (
                <Alert variant="danger">
                    <b>Caution:</b> The details of this execution are no longer cached on the supervisor.
                    If you navigate away from this page you will lose all details on this page.<br/>
                    {downloadLink}
                </Alert>
            )}
            <table className="summary-table">
                <tbody>
                <tr>
                    <Item label="Over" value={<OverLink over={over}/>}/>
                </tr>
                <tr>
                    <Item label="Request Status" value={state} valueClass={`status ${state}`}/>
                </tr>
                <tr>
                    <Item label="Request Start" value={dateTime(beginMillis)}/>
                    <Item label="Request End" value={dateTime(endMillis)}/>
                    <DurationItem begin={beginMillis} end={endMillis}/>
                </tr>
                {data.wave && (
                    <>
                        <tr>
                            <Item label="Wave Status" value={waveState} valueClass={`status ${waveState}`}/>
                        </tr>
                        <tr>
                            <Item label="Wave Start" value={dateTime(waveBeginMs) || 'N/A'}/>
                            <Item label="Wave End" value={dateTime(waveEndMs) || 'N/A'}/>
                            <DurationItem begin={waveBeginMs} end={waveEndMs} />
                            <Item label="Particle Skew" value={typeof skew === "number" ? skew.toFixed(2) : 'N/A'}/>
                        </tr>
                    </>
                )}
                </tbody>
            </table>
            <table className="summary-table">
                <tbody>
                {particles.length > 0 && (
                    <Row label="Particles" value={(<div className="summary">
                        <StateSummary key={summaryKey} items={particles} states={PARTICLE_STATES} sparse/>
                    </div>)}/>
                )}
                <Row label="Download" value={downloadLink}/>
                <Row label="Message" value={message.split('\n')[0]}/>
                </tbody>
            </table>
            <div>
                {message.indexOf('\n') !== -1 && (
                    <ToggleItem title="Error stack" show>
                        <pre className="p-2 burst-border">{message}</pre>
                    </ToggleItem>
                )}
                {wave.metrics && (
                    <>
                        <ToggleItem title="Execution Metrics" show>
                            <ExecutionMetrics metrics={executionMetrics}/>
                        </ToggleItem>
                        <ToggleItem title="Generation Metrics" show>
                            <GenerationMetrics metrics={generationMetrics}/>
                        </ToggleItem>
                    </>
                )}
                {parameters.length > 0 && (
                    <ToggleItem title="Parameters" show={true}>
                        <pre className="p-2 burst-border">{paramsJson}</pre>
                    </ToggleItem>
                )}
                {source.map((query, i) => (
                    <ToggleItem key={i} title={`Query ${i + 1}`} show={i === 0}>
                        <Button variant="outline-info" onClick={() => openQuery(query)} className="mb-2">
                            Open query
                        </Button>
                        <pre className="p-2 burst-border">{query}</pre>
                    </ToggleItem>
                ))}
                {particles?.length > 0 && (
                    <>
                        <ToggleItem title="Timeline">
                            <ExecutionTimeline execution={data}/>
                        </ToggleItem>
                        <ParticlesTable particles={particles}/>
                    </>
                )}
            </div>
        </div>
    );
}

export default ExecutionView

function DurationItem({begin, end}) {
    return <Item label="Duration" value={end >= begin ? elapsedTime(end - begin) : 'Running'}/>
}

const sortTypes = {
    duration: sortDuration
}
const filterTypes = {
    state: filterState,
}
const particleColumns = [
    {Header: "RUID", accessor: "ruid", minWidth: 150, Cell: GuidCell, Filter: TextFilter},
    {Header: "Host", accessor: "hostname", Filter: TextFilter},
    {
        Header: "State",
        id: "state",
        accessor: ({state, message}) => ({state, message}),
        className: 'text-center',
        Cell: ParticleStatusCell,
        Filter: ParticleStatusFilter,
        filter: 'state'
    },
    {Header: "Message", accessor: "message", className: "ws-pre", disableFilters: true},
    {Header: "Start", accessor: "beginMillis", Cell: DateCell, disableFilters: true},
    {
        Header: "Duration",
        id: "duration",
        accessor: ({beginMillis, endMillis}) => [endMillis - beginMillis],
        Cell: DurationCell,
        disableFilters: true,
        sortType: 'duration'
    },
]

const ParticleRow = ({children}) => children

function ParticlesTable({particles}) {
    const dispatch = useDispatch()
    const {initialSortBy, initialFilters} = useSelector(({execution}) => ({
        initialFilters: execution.particleFilters,
        initialSortBy: execution.particleSortBy,
    }))
    const {
        getTableProps,
        getTableBodyProps,
        headerGroups,
        page,
        prepareRow,
        gotoPage,
        canPreviousPage,
        canNextPage,
        pageCount,
        nextPage,
        previousPage,
        state: {pageIndex, filters, sortBy},
    } = useTable({
        columns: particleColumns,
        data: particles,
        sortTypes,
        filterTypes,
        initialState: {pageSize: 20, sortBy: initialSortBy, filters: initialFilters},
        autoResetExpanded: true,
        autoResetPage: false,
        autoResetSortBy: false,
        autoResetFilters: false,
    }, useFilters, useSortBy, usePagination)

    useEffect(() => {
        console.debug('exec filters:', filters)
        dispatch(execution.setParticleFilters(filters))
    }, [JSON.stringify(filters)])
    useEffect(() => {
        console.debug('exec sortBy:', sortBy)
        dispatch(execution.setParticleSortBy(sortBy))
    }, [JSON.stringify(sortBy)])
    page.forEach(prepareRow)

    return (
        <>
            <b>Particles</b>
            <table className="table table-responsive-sm mx-auto my-2 w-100 table-striped" {...getTableProps()}>
                <thead>
                {headerGroups.map(group => (
                    <tr {...group.getHeaderGroupProps()}>
                        {group.headers.map(column => (
                            <th {...column.getHeaderProps({className: column.className})}>
                                <div {...column.getSortByToggleProps()}>
                                    {column.render('Header')}
                                    <ColSort column={column}/>
                                </div>
                                <div>{column.canFilter ? column.render('Filter') : null}</div>
                            </th>
                        ))}
                    </tr>
                ))}
                </thead>
                <tbody {...getTableBodyProps()}>
                {page.map(row => (
                    <ParticleRow key={row.original.ruid}>
                        <tr {...row.getRowProps()}>
                            {row.cells.map((cell) => (
                                <td {...cell.getCellProps({className: cell.column.className})}>
                                    {cell.render('Cell')}
                                </td>
                            ))}
                        </tr>
                    </ParticleRow>
                ))}
                </tbody>
            </table>
            <div className="d-flex justify-content-center">
                <TablePagination pageIndex={pageIndex} pageCount={pageCount} onPrevious={previousPage} onNext={nextPage}
                                 isFirst={!canPreviousPage} isLast={!canNextPage} gotoPage={gotoPage}/>
            </div>

        </>
    )
}
