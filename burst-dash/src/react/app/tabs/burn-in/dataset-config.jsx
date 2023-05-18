import {Button} from "react-bootstrap";
import {FaIcon} from "../../utility/fa-icon";
import {ConditionalInput, ConfigRow, GrowingEditor, LoadQueryInput} from "./helpers";
import {Conditional} from "../../components/helpers";
import {PropertyEditor} from "../../components/property-editor";
import React from "react";

export const DatasetConfig = ({
                                  dataset,
                                  readOnly = false,
                                  onDatasetChange = (property, value) => null,
                                  removeDataset = (e) => null
                              }) => {
    const {
        datasetSource, // String - one of ["byPk", "byUdk", "byProperty", "generate"] // how is this dataset defined, loading by pk, udk, matching labels, or generated from an inline definition
        copies, // Option[Int] - the number of times to copy this dataset in the run
        pk, // Option[Long] - the pk of the view to copy, only used when datasetSource == byPk
        udk, // Option[String] -  the udk of the view to copy, only used when datasetSource == byUdk
        label, // Option[String] - a property that must be present on the view, only used when datasetSource == byProperty
        labelValue, // Option[String]
        domain, // Option[BurnInDomain] - a domain definition used to create this dataset, only used when datasetSource == generate
        view, // Option[BurnInView] - a view definition used to create this dataset, only used when datasetSource == generate
        loadQuery, // Option[String] - the query that used for the initial dataset load
        reloadEvery, // Option[Int] - force a reload of this dataset (by increasing the generationClock) after every N queries
    } = dataset

    return (
        <div className="d-flex dataset">
            <div className="d-inline-flex align-items-center mx-3">
                <Conditional show={!readOnly}>{() =>
                    <Button variant="outline-danger" size="sm" onClick={removeDataset}>
                        <FaIcon icon="trash" inheritColor/>
                    </Button>
                }</Conditional>
            </div>
            <div className="flex-grow-1">
                <ConfigRow label="source">
                    <select value={datasetSource} disabled={readOnly}
                            onChange={e => onDatasetChange('datasetSource', e.target.value)}>
                        <option value="byPk">By PK</option>
                        <option value="byUdk">By UDK</option>
                        <option value="byProperty">By Property</option>
                        <option value="generate">Generated</option>
                    </select>
                </ConfigRow>
                <ConfigRow label="Copies">
                    <input type="number" value={copies || 1} min={1} readOnly={readOnly} onChange={e => onDatasetChange('copies', +e.target.value)}/>
                </ConfigRow>
                <ConfigRow label="View PK" show={!!pk || pk === 0}>
                    <input type="number" value={pk} readOnly={readOnly}
                           onChange={e => onDatasetChange('pk', +e.target.value)}/>
                </ConfigRow>
                <ConfigRow label="View UDK" show={!!udk || udk === ''}>
                    <input value={udk} readOnly={readOnly} onChange={e => onDatasetChange('udk', e.target.value)}/>
                </ConfigRow>
                <ConfigRow label="View label" show={!!label || label === ''}>
                    <input value={label} readOnly={readOnly} onChange={e => onDatasetChange('label', e.target.value)}/>
                </ConfigRow>
                <ConfigRow label="View label value" show={!!label || label === ''}>
                    <ConditionalInput value={labelValue} readOnly={readOnly}
                                      onCheck={checked => onDatasetChange('labelValue', checked ? '' : null)}>
                        <input value={labelValue} readOnly={readOnly}
                               onChange={e => onDatasetChange('labelValue', e.target.value)}/>
                    </ConditionalInput>
                </ConfigRow>
                <Conditional show={!!domain}>{() => <>
                    <ConfigRow label="Domain properties">
                        <PropertyEditor initialProperties={domain.domainProperties} watchProps={domainProperties => {
                            onDatasetChange('domain', {domainProperties})
                        }}/>
                    </ConfigRow>
                    <hr className="m-0"/>
                    <ConfigRow label="Schema">
                        <input value={view.schemaName} readOnly={readOnly}
                               onChange={e => onDatasetChange('view', {...view, schemaName: e.target.value})}/>
                    </ConfigRow>
                    <ConfigRow label="View properties">
                        <PropertyEditor initialProperties={view.viewProperties} watchProps={viewProperties => {
                            onDatasetChange('view', {...view, viewProperties})
                        }}/>
                    </ConfigRow>
                    <ConfigRow label="Store properties">
                        <PropertyEditor initialProperties={view.storeProperties} watchProps={storeProperties => {
                            onDatasetChange('view', {...view, storeProperties})
                        }}/>
                    </ConfigRow>
                    <ConfigRow label="View motif" className="align-items-start">
                        <GrowingEditor value={view.viewMotif} readOnly={readOnly}
                                  onChange={e => onDatasetChange('view', {...view, viewMotif: e.target.value})}/>
                    </ConfigRow>
                </>}</Conditional>
                <ConfigRow label="Custom load query">
                    <LoadQueryInput value={loadQuery} readOnly={readOnly}
                                    onCheck={checked => onDatasetChange('loadQuery', checked ? '' : null)}
                                    onChange={e => onDatasetChange('loadQuery', e.target.value)}/>
                </ConfigRow>
                <ConfigRow label="Reload every N queries">
                    <ConditionalInput value={reloadEvery} readOnly={readOnly}
                                      onCheck={checked => onDatasetChange('reloadEvery', checked ? 1 : null)}>
                        <input type="number" value={reloadEvery}
                               onChange={e => onDatasetChange('reloadEvery', +e.target.value)}/>
                    </ConditionalInput>
                </ConfigRow>
            </div>
        </div>
    )
}
