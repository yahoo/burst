import React from "react";
import {useDispatch, useSelector} from "react-redux";
import {Pane} from "../../components/pane";
import {Button} from "react-bootstrap";
import {actions as burnIn} from '../../store/reducers/burn-in';
import {actions as crosscutting} from '../../store/reducers/crosscutting';
import {DateCell} from "../../components/table-cells";
import {saveAs} from 'file-saver'
import cn from 'classnames';
import {BatchConfig} from "./batch-config";

import "./burn-in-config.scss"
import {FaIcon} from "../../utility/fa-icon";
import {ConfigRow, DurationPicker, MaxDurationInput} from "./helpers";
import {Conditional} from "../../components/helpers";

const BatchLabel = ({i = 0, readOnly = false}) => {
    const dispatch = useDispatch()
    const removeBatch = () => dispatch(burnIn.removeBatch(i));
    return <>
        Batch {i + 1}<br/>
        <Conditional show={!readOnly}>{() =>
            <Button variant="outline-danger" onClick={removeBatch} disabled={readOnly}><FaIcon icon="trash" inheritColor/></Button>
        }</Conditional>
    </>
}
const BurnInTab = () => {
    const {
        running,
        config,
        events,
    } = useSelector(({burnIn}) => ({
        running: burnIn.running,
        config: burnIn.config,
        events: burnIn.events,
    }));
    const dispatch = useDispatch();
    const btnAction = running ?
        () => dispatch(burnIn.stopBurnIn()) :
        () => dispatch(burnIn.startBurnIn({config}));


    const addBatch = () => dispatch(burnIn.addBatch());
    const setMaxDuration = maxDuration => dispatch(burnIn.updateConfig({config: {maxDuration}}))

    const onDropHandler = async (e) => {
        let files = []
        if (e.dataTransfer.items) {
            files = [...e.dataTransfer.items].filter(i => i.kind === 'file').map(i => i.getAsFile());
        } else {
            files = e.dataTransfer.files
        }
        if (files.length === 0) {
            dispatch(crosscutting.displayMessage('No files detected in drop contents', 'No file found'))
            return;
        } else if (files.length > 1) {
            dispatch(crosscutting.displayMessage('Please drop a single file containing the burn-in configuration', 'Too many files'))
            return;
        }
        const text = await files[0].text();
        try {
            const config = JSON.parse(text);
            dispatch(burnIn.updateStatus({config, isRunning: false}));
        } catch (e) {
            // not valid json!
        }

    };
    const downloadJsonConfig = () => {
        const configJson = new Blob([
            JSON.stringify(config, null, 2)
        ], {type: "application/json;charset=utf-8"});
        saveAs(configJson, 'burn-in-config.json')
    };
    const filteredEvents = events.filter(({eventType}) => eventType === "event");
    return (
        <Pane onDrop={running ? undefined : onDropHandler}>
            <Pane.Fixed>
                <div className="d-flex flex-row justify-content-between align-items-center ">
                    <div className="flex-column">
                        <h2>Configuration</h2>
                        {!running && <small>Drop local config file to load it</small>}
                    </div>
                    <div>
                        <Button variant={`outline-${running ? "danger" : "success"}`} onClick={btnAction}>
                            {running ? "Stop" : "Start"} Burn-In
                        </Button>
                        <Button variant="link" onClick={downloadJsonConfig}>Download as json</Button>
                    </div>
                </div>
            </Pane.Fixed>
            <Pane.Flex className={cn("position-relative burst-border", running ? "b-pane-25" : "b-pane-75")}>
                <div className="burn-in-config row w-100 px-1 py-2">
                    <h4>Global</h4>
                    <ConfigRow label="Max duration">
                        <MaxDurationInput duration={config.maxDuration}
                                          onCheck={checked => setMaxDuration(checked ? '1 hour' : null)}
                                          onChange={(num, unit) => setMaxDuration(`${num} ${unit}`)}/>
                    </ConfigRow>
                    <h4>Batches</h4>
                    <div className="batches">
                        {config.batches.map((b, idx) => (
                            <ConfigRow key={idx} label={<BatchLabel i={idx} readOnly={running}/>} className="batch-row">
                                <BatchConfig batch={b} readOnly={running} onChange={
                                    (prop, value) => dispatch(burnIn.updateBatch({idx, prop, value}))
                                }/>
                            </ConfigRow>
                        ))}
                    </div>
                    <Conditional show={!running}>{() =>
                        <ConfigRow label={
                            <Button size="sm" variant="outline-success" onClick={addBatch} disabled={running}>
                                Add Batch
                            </Button>
                        }/>
                    }</Conditional>
                </div>
            </Pane.Flex>
            <Pane.Flex className={cn(running ? "b-pane-75" : "b-pane-25")}>
                <div className="w-100 px-1 pt-2">
                    <table className="table table-sm table-striped table-responsive">
                        <tbody>
                        {filteredEvents.map((evt, idx) => (
                            <tr key={`${idx}-${evt.time}-${evt.level.name}`}>
                                <td style={{width: '5rem'}}>{evt.level.name}</td>
                                <td style={{width: '10rem'}}><DateCell value={evt.time}/></td>
                                <td className="event-message">{evt.message}</td>
                            </tr>
                        ))}
                        {filteredEvents.length === 0 && (
                            <tr>
                                <td>No Burn-in events</td>
                            </tr>
                        )}
                        </tbody>
                    </table>
                </div>
            </Pane.Flex>
        </Pane>
    );
}

export default BurnInTab;
