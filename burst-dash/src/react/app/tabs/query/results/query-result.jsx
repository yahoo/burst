import React from "react";
import QueryGroupMetrics from "./queryGroupMetrics";
import QueryResultSet from "./queryResultSet";
import {useSelector} from "react-redux";

const QueryResult = () => {
    const {result, executing} = useSelector(({query}) => ({
        result: query.execution,
        executing: query.executing,
    }))

    if (!result || executing) {
        return (
            <div className="w-100 burst-empty-message">
                {executing ? 'query request pending…' : 'no query executions yet…'}
            </div>
        );
    }

    if (!result.resultStatus.isSuccess) {
        return (
            <div className="b-flex-col h-100 query-results burst-border">
                    <pre className="error-message">
                        <div className="error-header">--------------ERROR------------</div>
                        {result.resultMessage}
                    </pre>
            </div>
        );
    }

    const {resultGroup} = result;
    return (
        <div className="b-flex-col h-100 burst-border query-results">
            <QueryGroupMetrics resultGroup={resultGroup}/>
            {Object.values(resultGroup.resultSets).map((resultSet, i) => {
                return <QueryResultSet key={i} queryUid={resultGroup.groupKey.groupUid} resultSet={resultSet}/>;
            })}
            <button onClick={() => console.log(resultGroup)}>Print Results To Console</button>
        </div>
    );
}

export default QueryResult;
