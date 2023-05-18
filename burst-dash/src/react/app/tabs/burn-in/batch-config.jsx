import {Button} from "react-bootstrap";
import React from "react";
import {FaIcon} from "../../utility/fa-icon";
import {ConfigRow, DurationPicker, GrowingEditor, LoadQueryInput, MaxDurationInput} from "./helpers";
import {DatasetConfig} from "./dataset-config";
import {Conditional} from "../../components/helpers";

export const BatchConfig = ({batch = {}, readOnly = false, onChange = (prop = '', value) => undefined}) => {
    const {
        concurrency, // Int - number of concurrent datasets
        datasets, // Array[BurnInDatasetDescriptor] - a list of datasets to be queries
        defaultLoadQuery, // Option[String] - the query to use when loading a dataset, if not specified by the dataset descriptor
        queries, // Array[String] - a list of queries to be executed against each dataset on each iteration
        durationType, // String, // one of ["duration", "datasets"]
        desiredDatasetIterations, // Option[Int] - how long should this batch run, // a specified length of time, or a number of datasets
        desiredDuration, // Option[Duration] - the number of datasets that should be run
        maxDuration, // Option[Duration] - the length of time datasets in this batch should continue to be loaded
    } = batch;
    const onBatchChange = onChange


    return (
        <div className="row">
            <ConfigRow label="Enforce max duration">
                <MaxDurationInput duration={maxDuration} readOnly={readOnly}
                                  onCheck={checked => onBatchChange('maxDuration', checked ? '1 hour' : null)}
                                  onChange={(num, unit) => onBatchChange('maxDuration', `${num} ${unit}`)}/>
            </ConfigRow>
            <ConfigRow label="Concurrency">
                <input type="number" min={1} max={16} value={concurrency} readOnly={readOnly} onChange={e => {
                    onBatchChange('concurrency', +e.target.value)
                }}/>
            </ConfigRow>
            <ConfigRow label="Duration limit">
                <select value={durationType} disabled={readOnly}
                        onChange={e => onBatchChange('durationType', e.target.value)}>
                    <option value="duration">By Duration</option>
                    <option value="datasets">By Iterations</option>
                </select>
            </ConfigRow>
            <ConfigRow show={!!desiredDatasetIterations}>
                <input type="number" min={1} value={desiredDatasetIterations} className="me-2" readOnly={readOnly}
                       onChange={e => onBatchChange('desiredIterations', +e.target.value)}/>
                iterations
            </ConfigRow>
            <ConfigRow show={!!desiredDuration}>
                <DurationPicker value={desiredDuration} readOnly={readOnly}
                                onChange={(num, unit) => onBatchChange('desiredDuration', `${num} ${unit}`)}/>
            </ConfigRow>
            <ConfigRow label="Queries">
                {queries.map((q, i) => (
                    <div key={i} className="d-flex align-items-center config-row">
                        <GrowingEditor value={q} readOnly={readOnly} onChange={e => {
                            onBatchChange('queries', [...queries.slice(0, i), e.target.value, ...queries.slice(i + 1)])
                        }}/>
                        <Conditional show={!readOnly}>{() =>
                            <Button size="sm" variant="outline-danger" disabled={readOnly} onClick={() => {
                                onBatchChange('queries', [...queries.slice(0, i), ...queries.slice(i + 1)])
                            }}><FaIcon icon="trash" inheritColor/></Button>}
                        </Conditional>
                    </div>
                ))}
                <Button variant="outline-success" size="sm" disabled={readOnly}
                        onClick={() => onBatchChange('queries', [...queries, ''])}>Add
                    query</Button>
            </ConfigRow>
            <ConfigRow label="Default Load Query">
                <LoadQueryInput value={defaultLoadQuery} readOnly={readOnly}
                                onCheck={checked => onBatchChange('loadQuery', checked ? '' : null)}
                                onChange={e => onBatchChange('loadQuery', e.target.value)}/>
            </ConfigRow>
            <ConfigRow label={`Dataset Specs (${datasets.length})`}>
                <div className="datasets">
                    {datasets.map((d, idx) => (
                        <DatasetConfig key={idx} dataset={d} readOnly={readOnly}
                                       onDatasetChange={(key, update) => onBatchChange('dataset', {key, update, idx})}
                                       removeDataset={() => onBatchChange('removeDataset', idx)}/>
                    ))}
                </div>
                <Button variant="outline-success" size="sm" disabled={readOnly}
                        onClick={() => onBatchChange('addDataset')}>Add dataset</Button>
            </ConfigRow>
        </div>
    )
}
