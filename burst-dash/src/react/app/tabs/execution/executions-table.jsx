import React, {useEffect, useMemo} from "react";
import {useDispatch, useSelector} from "react-redux";
import {useFilters, usePagination, useSortBy, useTable} from "react-table-7"
import {Link, useRouteMatch} from "react-router-dom";

import {ColSort, DateCell, GuidCell, TextFilter} from "../../components/table-cells";
import {
    DataOverCell,
    DataSizeCell,
    DurationCell,
    ProgressBarCell,
    SkewCell,
    SkewHeader,
    WaveStatusCell,
    WaveStatusFilter,
    filterOver, filterState,
    sortDuration, sortOver,
} from "./cells";
import TablePagination from "../../components/table-pagination";
import {actions as execution} from '../../store/reducers/execution';

const summarizer = ({wave}) => wave ? Object.entries(wave.particleSummary).reduce((collector, [state, count]) => {
    if (state === "SUCCEEDED") {
        collector.success += count;
    } else if (state === "FAILED") {
        collector.failure += count;
    } else if (state === "CANCELLED") {

    } else if (state === "IN_PROGRESS" || state === "LATE") {
        collector.running += count;
    }
    collector.total += count;
    return collector;
}, {success: 0, running: 0, failure: 0, total: 0, key: wave.seqNum}) : null;

const ExecutionTable = ({children}) => <>{children}</>
const ExecutionRow = ({children}) => <>{children}</>

const ExecutionsTable = () => {
    const dispatch = useDispatch()
    const {url} = useRouteMatch();
    const {executions, initialFilters, initialSortBy} = useSelector(({execution}) => ({
        executions: execution.executions,
        initialFilters: execution.filters,
        initialSortBy: execution.sortBy,
    }))

    const columns = useMemo(() => [
        {Header: "Start", accessor: "beginMillis", Cell: DateCell, disableFilters: true},
        {
            Header: "GUID",
            accessor: "guid",
            Cell: ({value}) => (<Link to={`${url}/${value}`}><GuidCell value={value}/></Link>),
            Filter: TextFilter
        },
        {Header: "Seq", accessor: "wave.seqNum", className: "text-center", disableFilters: true},
        {
            Header: "State",
            id: "state",
            accessor: ({state, status, wave}) => ({state, status, waveState: wave?.state, waveStatus: wave?.status}),
            className: 'text-center',
            Cell: WaveStatusCell,
            Filter: WaveStatusFilter,
            filter: 'state',
        },
        {
            Header: "Over",
            accessor: "over",
            className: "text-center",
            Cell: DataOverCell,
            Filter: TextFilter,
            sortType: 'over',
            filter: 'over'
        },
        {
            Header: "Duration",
            id: "duration",
            className: "text-right",
            accessor: ({beginMillis, endMillis, wave}) => {
                const reqDuration = endMillis - beginMillis
                const {beginMillis: waveBegin, endMillis: waveEnd} = wave ?? {}
                const waveDuration = waveEnd - waveBegin
                return [reqDuration, waveDuration];
            },
            Cell: DurationCell,
            disableFilters: true,
            sortType: 'duration'
        },
        {
            Header: "Size",
            id: "wave.metrics",
            className: "text-right",
            accessor: ({wave}) => wave?.metrics?.generationMetrics?.byteCount,
            Cell: DataSizeCell,
            disableFilters: true
        },
        {Header: SkewHeader, accessor: "wave.skew", className: "text-right", Cell: SkewCell, disableFilters: true},
        {
            Header: "Particles",
            id: "statusBar",
            headerClass: "",
            className: "text-right",
            accessor: summarizer,
            Cell: ProgressBarCell,
            disableFilters: true,
            disableSortBy: true,
        },
    ], [url]);

    const data = useMemo(() => Object.values(executions).filter(e => e), [executions])
    const sortTypes = useMemo(() => ({over: sortOver, duration: sortDuration}), [])
    const filterTypes = useMemo(() => ({over: filterOver, state: filterState}), [])
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
        columns,
        data,
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
        dispatch(execution.setFilters(filters))
    }, [JSON.stringify(filters)])
    useEffect(() => {
        console.debug('exec sortBy:', sortBy)
        dispatch(execution.setSortBy(sortBy))
    }, [JSON.stringify(sortBy)])
    page.forEach(prepareRow)

    return (
        <ExecutionTable page={page}>
            <table className="table table-responsive-sm" {...getTableProps()}>
                <thead>
                {headerGroups.map(group => (
                    <tr {...group.getHeaderGroupProps()}>
                        {group.headers.map(column => (
                            <th {...column.getHeaderProps({className: column.headerClass ?? column.className})}>
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
                    <ExecutionRow key={row.original.guid} data={row.original} values={row.values}>
                        <tr {...row.getRowProps()}>
                            {row.cells.map((cell) => (
                                <td {...cell.getCellProps({className: cell.column.className})}>
                                    {cell.render('Cell')}
                                </td>
                            ))}
                        </tr>
                    </ExecutionRow>
                ))}
                </tbody>
            </table>
            <TablePagination pageIndex={pageIndex} pageCount={pageCount} onPrevious={previousPage} onNext={nextPage}
                             canPreviousPage={canPreviousPage} canNextPage={canNextPage} gotoPage={gotoPage}/>
        </ExecutionTable>
    )
};

export default ExecutionsTable
