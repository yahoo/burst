import React, {useEffect} from "react";
import {Controlled as CodeMirror} from 'react-codemirror2'
import {Button, FormControl, Navbar} from "react-bootstrap";
import {useDispatch, useSelector} from "react-redux";
import {codeMirrorQueryOptions} from "../../utility/code-mirror";
import PropertyEditor from "../../utility/PropertyEditor";
import {dateTime} from '../../utility/burst-conversions';
import {actions as catalog} from '../../store/reducers/catalog'
import {actions as crosscutting} from '../../store/reducers/crosscutting'

const LabeledItem = ({label, item}) => (
    <span className="burst-labeled-item">
        <span className="burst-label">{label} </span>
        <span className="burst-value">{item}</span>
    </span>
);

const EntityEditor = () => {
    const dispatch = useDispatch();
    const {type, pk, modified, saving, entity} = useSelector(state => state.catalog.editor)
    useEffect(() => {
        if (type === 'domain') {
            dispatch(crosscutting.fetchDomain(pk))
        } else if (type === 'view') {
            dispatch(crosscutting.fetchView(pk))
        } else if (type) {
            console.warn('Unknown editor type:', type)
        }
    }, [type, pk])
    if (!entity.moniker) {
        return <div className="w-100 burst-empty-message">select from search results for details...</div>;
    }
    if (entity.pk !== pk) {
        return <div className="w-100 burst-empty-message">loading...</div>;
    }

    const propsName = `${type}Properties`
    const {
        // generic fields
        moniker, labels = {}, udk, [propsName]: properties = {},
        // view-specific fields
        generationClock, schemaName, viewMotif, storeProperties = {}, domainFk,
    } = entity;
    const isView = type === "view";
    const entityName = type[0].toUpperCase() + type.slice(1);
    const fieldUpdater = prop => e => dispatch(catalog.edit({prop, value: e.target.value}))
    const propsUpdater = prop => value => dispatch(catalog.edit({prop, value}))
    const updateViewMotif = (editor, data, value) => dispatch(catalog.edit({prop: 'viewMotif', value}))
    const revGenClock = () => dispatch(catalog.bumpGenerationClock({view: entity}))
    const schemaView = schemaName === 'unity' ?
        schemaName :
        <FormControl as="select" style={{width: '5em'}} value={schemaName || ''} onChange={fieldUpdater('schemaName')}>
            <option value="quo">quo</option>
            <option value="unity">unity</option>
        </FormControl>

    const validateViewMotif = () => dispatch(catalog.validateMotif({schemaName, motif: viewMotif}));
    return (
        <div id="catalog-editor" className="burst-border">
            <div className="w-100 d-flex flex-wrap align-items-center entity-header">
                {isView && <LabeledItem key="domainFk" label="Domain" item={domainFk}/>}
                <LabeledItem key="pk" label={entityName} item={pk}/>
                {isView && <LabeledItem key="generation" label="Gen Clock" item={dateTime(generationClock)}/>}
                {isView &&
                <Button variant="outline-warning" size="sm" onClick={revGenClock} disabled={modified || saving}>Rev
                    GC</Button>}
                <LabeledItem key="moniker" label="Name" item={(
                    <FormControl size="sm" style={{width: '25em'}} value={moniker || ''}
                                 onChange={fieldUpdater('moniker')}/>
                )}/>
                <LabeledItem key="UDK" label="UDK" item={(
                    <FormControl size="sm" style={{width: '10em'}} value={udk || ''} onChange={fieldUpdater('udk')}/>
                )}/>
                {isView && <LabeledItem key="Schema" label="Schema" item={schemaView}/>}
                {modified && (<>
                    <span className="flex-grow-1"/>
                    <Button variant="outline-success" size="sm"
                            onClick={() => dispatch(catalog.save({type, entity}))} disabled={saving}>Save</Button>
                </>)}
            </div>

            <div className="container-fluid">
                <h3 className="mt-2">{entityName} Properties</h3>
                <PropertyEditor id="properties-editor" properties={{...properties}} onChange={propsUpdater(propsName)}/>

                {isView && (
                    <>
                        <h3>Store Properties</h3>
                        <PropertyEditor id="store-props-editor" properties={{...storeProperties}}
                                        onChange={propsUpdater('storeProperties')}/>
                    </>
                )}

                <h3>Labels</h3>
                <PropertyEditor id="labels-editor" properties={{...labels}} onChange={propsUpdater('labels')}/>

                {isView && (
                    <>
                        <Navbar className="w-100 mb-2">
                            <b className="col-sm-6">View Definition</b>
                            <Button variant="outline-secondary" size="sm"
                                    onClick={validateViewMotif}>Validate</Button>
                        </Navbar>
                        <div className="position-relative">
                            <CodeMirror value={viewMotif}
                                        options={codeMirrorQueryOptions}
                                        onBeforeChange={updateViewMotif}/>
                        </div>
                    </>
                )}
            </div>
        </div>
    )
}

export default EntityEditor
