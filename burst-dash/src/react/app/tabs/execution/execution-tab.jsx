import React, {useMemo, useState} from "react";
import {Button} from "react-bootstrap";
import {Link, Route, Switch, useRouteMatch} from "react-router-dom";

import './execution-tab.scss';
import ExecutionsTable from "./executions-table";
import ExecutionView from "./execution-view";
import {Pane} from "../../components/pane";
import {clearExecutionList} from "../../websockets/execution";
import {useSelector} from "react-redux";
import {REQUEST_STATES} from "./cells";

const SummaryField = ({name, summary}) => <span className={name}>{name}: {summary[name] || 0}</span>;
export const StateSummary = ({items, states = REQUEST_STATES, sparse = false}) => {
    const byStatus = items.filter(i => i).reduce((all, item) => {
        all[item.state] = (all[item.state] || 0) + 1;
        return all
    }, {});
    return states.map((s) => ((byStatus[s] || !sparse) && <SummaryField key={s} name={s} summary={byStatus}/>));
};

/**
 * Browser component for the Fabric distributed executions
 */
const ExecutionTab = () => {
    const [clearAfter, setClearAfter] = useState(0);
    const [interval, setHandle] = useState(0);
    const {path} = useRouteMatch();
    const {executions, since} = useSelector(({execution}) => ({
        executions: execution.executions,
        since: execution.since
    }))
    const executionsList = useMemo(
        () => Object.values(executions).filter(e => e).sort((l, r) => l.beginMillis < r.beginMillis ? 1 : -1),
        [executions]);

    const clearExecutions = () => {
        const start = Date.now();
        let clearOn = start + 5000;

        if (!clearAfter) {
            setClearAfter(clearOn);
            const handle = setInterval(() => {
                const now = Date.now();
                if (clearOn < now) {
                    clearExecutionList();
                    clearInterval(handle);
                    setClearAfter(0);
                    setHandle(0);
                } else {
                    clearOn -= 1;
                    setClearAfter(clearOn);
                }
            }, 250);
            setHandle(handle);
        } else {
            clearInterval(interval);
            setClearAfter(0);
            setHandle(0);
        }
    };

    return (
        <Pane id="execution-tab" noFixedHeight>
            <Pane.Fixed className="d-block">
                <div className="d-flex space-between">
                    <Switch>
                        <Route exact path={path}>
                            <>
                                {since > 0
                                    ? <div className="summary">
                                        <span>Total executions: {executionsList.length}</span>
                                        <StateSummary items={executionsList}/></div>
                                    : <p>Loading data…</p>
                                }
                                <p>
                                    <Button size="sm" variant={clearAfter ? "danger" : "warning"}
                                            onClick={clearExecutions}>{
                                        clearAfter
                                            ? `Clearing executions in ${Math.ceil((clearAfter - Date.now()) / 1000)} (click to cancel)`
                                            : "Clear executions (affects all users)"
                                    }</Button>
                                </p>
                            </>
                        </Route>
                        <Route path={`${path}/:execution`} render={({match: {params: {execution}}}) => (
                            <>
                                <h3>{execution}</h3>
                                <Link to={path}>Back to list</Link>
                            </>
                        )}/>
                    </Switch>
                </div>
            </Pane.Fixed>
            <Pane.Flex className="align-items-center justify-content-between">
                <Switch>
                    <Route exact path={path}>
                        <ExecutionsTable/>
                    </Route>
                    <Route path={`${path}/:executionId`} render={({match: {params: {executionId}}}) => (
                        <ExecutionView guid={executionId}/>
                    )}>
                    </Route>
                </Switch>
            </Pane.Flex>
        </Pane>
    );
};

export default ExecutionTab
