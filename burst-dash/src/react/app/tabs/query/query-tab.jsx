import React, {useEffect, useState} from "react";
import {useDispatch, useSelector} from "react-redux";
import {Button, ButtonToolbar, Dropdown, Navbar, SplitButton} from "react-bootstrap";
import {Controlled as CodeMirror} from "react-codemirror2";

import './query-tab.scss';

import QueryResultBrowser from "./results/query-result";
import QueryLoadDialog from "./query-load-dialog";
import QueryParameters from "./query-parameters";
import QuerySaveDialog from './query-save-dialog';
import {actions as query} from "../../store/reducers/query";
import {codeMirrorJsonOptions, codeMirrorQueryOptions} from "../../utility/code-mirror";
import {Pane} from "../../components/pane";

const QueryTab = () => {
    const dispatch = useDispatch();
    const {
        saving, savedQuery,
        text, params, timezone, dataset: {domain, view},
        executing, execution
    } = useSelector(({query, crosscutting}) => {
        const {saving, savedQuery, allQueries, text, params, timezone, executing, execution} = query;
        return {
            saving,
            savedQuery,
            allQueries,
            text,
            params,
            timezone,
            executing,
            execution,
            dataset: crosscutting.selectedDataset
        }
    })
    useEffect(() => {
        dispatch(query.fetchQueries())
    }, [])
    const hasParams = params.length > 0
    const [showList, setShowList] = useState(false)
    const [showSaveAs, setShowSaveAs] = useState(false);
    const [showArgs, setShowArgs] = useState(hasParams);


    const disallowSave = saving || savedQuery.source === text
    const canExecute = text.length && domain && view && !executing;
    const saveQuery = () => {
        if (savedQuery?.pk) {
            dispatch(query.saveQuery({...savedQuery, source: text}))
        } else {
            setShowSaveAs(true)
        }
    }
    const execute = () => {
        if (canExecute) {
            dispatch(query.execute({source: text, domain, view, timezone, args: params}))
        }
    }
    const deleteQuery = () => dispatch(query.deleteQuery(savedQuery.pk))
    const paramsUpdated = (editor, data, value) => dispatch(query.setParams(value))
    const textUpdated = (editor, data, value) => dispatch(query.setText(value))

    return (
        <Pane>
            <Pane.Fixed overflow>
                <div className="d-flex">
                    <QueryParameters/>
                    <Navbar className="ms-4">
                        <Navbar.Text className="ms-2 query-title">
                            Query: <span className="text-body" title={savedQuery.moniker}>{savedQuery.moniker}</span>
                        </Navbar.Text>
                        <ButtonToolbar className="ms-2">
                            <Button variant="outline-dark" size="sm"
                                    onClick={() => setShowList(!showList)}>Open…</Button>
                            <SplitButton title="Save" variant="outline-success" size="sm" disabled={disallowSave}
                                         onClick={saveQuery}>
                                <Dropdown.Item onClick={() => setShowSaveAs(!showSaveAs)}>Save As…</Dropdown.Item>
                            </SplitButton>
                            <Button variant="outline-danger" size="sm" disabled={!savedQuery.pk} onClick={deleteQuery}>
                                Delete…
                            </Button>
                            <Button variant="outline-info" disabled={!canExecute} onClick={execute} size="sm">
                                Execute
                            </Button>
                            <Button variant="outline-dark" size="sm"
                                    onClick={() => setShowArgs(hasParams || !showArgs)}>
                                {showArgs ? 'Hide Args' : 'Show Args'}
                            </Button>
                        </ButtonToolbar>
                    </Navbar>
                </div>
                {showList && <QueryLoadDialog onHide={() => setShowList(false)}/>}
                {showSaveAs && <QuerySaveDialog onHide={() => setShowSaveAs(false)}/>}
            </Pane.Fixed>
            {(showArgs || hasParams) && <Pane.Fixed minHeight="0">
                <div><h6>Arguments:</h6></div>
            </Pane.Fixed>}
            {(showArgs || hasParams) && <Pane.Flex className="position-relative">
                <CodeMirror value={params} className="h-100" options={codeMirrorJsonOptions}
                            onBeforeChange={paramsUpdated}/>
            </Pane.Flex>}
            {(showArgs || hasParams) && <Pane.Fixed minHeight="0">
                <div><h6>Query Source:</h6></div>
            </Pane.Fixed>}
            <Pane.Flex className="position-relative">
                <CodeMirror value={text} className="h-100" options={{
                    ...codeMirrorQueryOptions, extraKeys: {'Cmd-Enter': execute, 'Ctrl-Enter': execute}
                }} onBeforeChange={textUpdated}/>
            </Pane.Flex>
            <Pane.Flex>
                <QueryResultBrowser {...execution}/>
            </Pane.Flex>
        </Pane>
    )
}

export default QueryTab
