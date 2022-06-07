import React, {useMemo} from 'react';
import {useDispatch} from "react-redux";
import {Link, useHistory} from "react-router-dom";
import {actions as thrift} from '../../store/reducers/thrift';
import {actions as crosscutting} from '../../store/reducers/crosscutting'
import {actions as query} from '../../store/reducers/query';
import {makeFilterCell} from "../../components/table-cells";

/////////////////////////////
// Row Expander
/////////////////////////////
const canRowExpand = row => row.exception || row.source
export const Expander = ({row}) => {
    const dispatch = useDispatch();
    if (!canRowExpand(row.original)) {
        return null;
    }
    const {onClick, ...expanderProps} = row.getToggleRowExpandedProps();
    return (
        <span {...expanderProps} onClick={(...args) => {
            dispatch(thrift.toggleExpanded(row.original.ruid))
            onClick(...args)
        }}>
            <a className="btn-link"><span className={`fa fa-chevron-${row.isExpanded ? 'down' : 'right'}`}/></a>
        </span>
    );
}

export const TypeCell = ({value = ''}) => value
export const DomainCell = ({value = ''}) => {
    const dispatch = useDispatch()
    const jumpToCatalog = async () => {
        const {payload: domain} = await dispatch(crosscutting.fetchDomain(value))
        dispatch(crosscutting.selectDataset({domain}))
    }
    return value && value !== '???' ? <Link to={'/catalog'} onClick={jumpToCatalog}>{value}</Link> : value
}
export const ViewCell = ({value = '', row}) => {
    const dispatch = useDispatch()
    const jumpToCatalog = async () => {
        const {domainUdk} = row.original;
        const {payload: domain} = await dispatch(crosscutting.fetchDomain(domainUdk))
        const {payload: view} = await dispatch(crosscutting.fetchViewForDomain({domain: domainUdk, view: value}))
        dispatch(crosscutting.selectDataset({domain, view}))
    }
    return value && value !== '???' ? <Link to={'/catalog'} onClick={jumpToCatalog}>{value}</Link> : value
}
export const StatusCell = ({value}) => <b className={value}>{value}</b>


/////////////////////////////
// Request Detail row
/////////////////////////////
const Stacktrace = ({exception}) => (
    <pre className="stacktrace">{
        exception.localizedMessage
    }{
        exception.stackTrace.reduce((st, line) => `${st}\n  at ${line.fileName}:${line.lineNumber}`, "")
    }</pre>
)
export const DetailRow = ({colSpan = 1, guid, exception, source, params, results, domainUdk, viewUdk}) => {
    const dispatch = useDispatch()
    const history = useHistory();
    const selectDataset = async () => {
        const {payload: domain} = await dispatch(crosscutting.fetchDomain(domainUdk))
        const {payload: view} = await dispatch(crosscutting.fetchViewForDomain({domain: domainUdk, view: viewUdk}))
        dispatch(crosscutting.selectDataset({domain, view}))
    }
    const paramsText = useMemo(() => JSON.stringify(params, null, 2), [params])
    const showWave = async () => {
        history.push("/waves")
        // set wave filter
    }
    const openQuery = async () => {
        await selectDataset();
        history.push("/query")
        dispatch(query.setText(source))
        dispatch(query.setParams(paramsText))
    }
    return (
        <tr className="detail">
            <td colSpan={colSpan}>
                {source && (
                    <div className="container-fluid">
                        <div className="row">
                            <div className="col pl-0 query-source">
                                <pre>{source}</pre>
                            </div>
                            <div className="col query-params">
                                <pre>Params: {params ? paramsText : 'None'}</pre>
                            </div>
                            <div className="col pr-0 query-actions">
                                <a className="btn btn-link" onClick={showWave}>Show wave</a>
                                <a className="btn btn-link" onClick={openQuery}>Open query</a>
                            </div>
                        </div>
                    </div>
                )}
                {results && Object.keys(results).length > 0 && (
                    <pre>{Object.keys(results).map(key => `${key}: ${results[key]} row${results[key] > 1 ? 's' : ''}\n`)}</pre>
                )}
                {exception && <Stacktrace exception={exception}/>}
            </td>
        </tr>
    );
}
export const TypeFilter = makeFilterCell([
    "EnsureDomain",
    "FindDomain",
    "EnsureDomainContainsView",
    "ListViewsInDomain",
    "ExecuteQuery",
], 'RequestTypeFilter')
export const StatusFilter = makeFilterCell([
    {value: "InProgressStatus", title: "In Progress"},
    {value: "SuccessStatus", title: "Success"},
    {value: "NoDataStatus", title: "No Data"},
    {value: "ExceptionStatus", title: "Exception"},
    {value: "InvalidStatus", title: "Invalid"},
    {value: "NotReadyStatus", title: "Not Ready"},
    {value: "TimeoutStatus", title: "Timeout"},
    {value: "NotFoundStatus", title: "Not Found"}
], 'RequestStatusFilter')
