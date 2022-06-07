import React, {useEffect, useMemo} from 'react';
import {useExpanded, useFilters, usePagination, useSortBy, useTable, useFlexLayout} from "react-table-7";
import {useDispatch, useSelector} from 'react-redux'

import './thrift-tab.scss';
import {
    DetailRow,
    DomainCell,
    Expander,
    StatusCell, StatusFilter,
    TypeCell, TypeFilter,
    ViewCell
} from './cells';
import {Pane} from "../../components/pane";
import {ColSort, DateCell, GuidCell, TextFilter} from "../../components/table-cells";
import TablePagination from "../../components/table-pagination";
import {actions as thrift} from '../../store/reducers/thrift'

const expanderRowSpan = (row, i) => i === 0 && row.isExpanded ? 2 : undefined

const columns = [
    {id: 'exapnder', Cell: Expander, width: 40},
    {Header: 'Date', accessor: 'timestamp', Cell: DateCell, disableFilters: true, width: 80},
    {Header: 'Req ID', accessor: row => row.guid || row.ruid, Cell: GuidCell, Filter: TextFilter, filter: 'includes', width: 200},
    {Header: 'Status', accessor: 'status', Cell: StatusCell, Filter: StatusFilter, filter: 'equals', width: 120},
    {Header: 'Method', accessor: 'method', Cell: TypeCell, Filter: TypeFilter},
    {Header: 'Domain', accessor: 'domainUdk', Cell: DomainCell, Filter: TextFilter, width: 80},
    {Header: 'View', accessor: 'viewUdk', Cell: ViewCell, Filter: TextFilter, width: 80},
]

const ThriftTable = ({children}) => <>{children}</>
const ThriftRequest = ({children}) => <>{children}</>

const ThriftTab = () => {
    const dispatch = useDispatch()
    const {requests, initialSortBy, initialFilters} = useSelector(({thrift}) => ({
        requests: thrift.requests,
        initialSortBy: thrift.sortBy,
        initialFilters: thrift.filters,
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
        state: {pageIndex, sortBy, filters},
    } = useTable({
        columns,
        data: requests,
        initialState: {pageSize: 20, sortBy: initialSortBy, filters: initialFilters},
        defaultCanFilter: false,
        autoResetExpanded: true,
        autoResetPage: false,
        autoResetSortBy: false,
        autoResetFilters: false,
    }, useFilters, useSortBy, useExpanded, usePagination)
    useEffect(() => {
        console.debug('thrift filters:', filters)
        dispatch(thrift.setFilters(filters))
    }, [JSON.stringify(filters)])
    useEffect(() => {
        console.debug('thrift sortBy:', sortBy)
        dispatch(thrift.setSortBy(sortBy))
    }, [JSON.stringify(sortBy)])
    page.forEach(prepareRow)
    const numDataCols = columns.length - 1

    return (
        <Pane id="thriftTab" noFixedHeight>
            <Pane.Flex>
                <ThriftTable page={page}>
                    <table className="table table-responsive-sm" {...getTableProps()}>
                        <thead>
                        {headerGroups.map(headerGroup => (
                            <tr {...headerGroup.getHeaderGroupProps()}>
                                {headerGroup.headers.map(column => (
                                    <th {...column.getHeaderProps()}>
                                        <div {...column.getSortByToggleProps()}>
                                            {column.render('Header')}
                                            <ColSort column={column} />
                                        </div>
                                        <div>{column.canFilter ? column.render('Filter') : null}</div>
                                    </th>
                                ))}
                            </tr>
                        ))}
                        </thead>
                        <tbody {...getTableBodyProps()}>
                        {page.map((row) => (
                            <ThriftRequest key={row.original.ruid} data={row.original}>
                                <tr {...row.getRowProps()}>
                                    {row.cells.map((cell, ci) => (
                                        <td rowSpan={expanderRowSpan(row, ci)} {...cell.getCellProps()}>
                                            {cell.render('Cell')}
                                        </td>
                                    ))}
                                </tr>
                                {row.isExpanded && <DetailRow colSpan={numDataCols} {...row.original}/>}
                            </ThriftRequest>
                        ))}
                        </tbody>
                    </table>
                </ThriftTable>
            </Pane.Flex>
            <Pane.Fixed className="justify-content-end align-items-center">
                <TablePagination pageIndex={pageIndex} pageCount={pageCount} onPrevious={previousPage} onNext={nextPage}
                                 canPreviousPage={canPreviousPage} canNextPage={canNextPage} gotoPage={gotoPage}/>
            </Pane.Fixed>
        </Pane>
    );
}

export default ThriftTab
