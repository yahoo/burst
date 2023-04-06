import React, {useMemo} from 'react';
import {useImmerReducer} from "use-immer";
import {Button, Table} from "react-bootstrap";
import {Conditional} from "./helpers";

const UpdateKeyAction = 'updateKey';
const UpdateValueAction = "updateValue";
const AddPropertyAction = "addProperty";
const RemovePropertyAction = "removeProperty";

const didUpdateKey = (idx, newKey) => ({type: UpdateKeyAction, payload: {idx, newKey}})
const didUpdateValue = (idx, newValue) => ({type: UpdateValueAction, payload: {idx, newValue}})
const didAddProperty = (newKey, newValue) => ({type: AddPropertyAction, payload: {newKey, newValue}})
const didRemoveProperty = (idx) => ({type: RemovePropertyAction, payload: {idx}})

const PropertyRow = ({
                         idx = 0,
                         propKey = '',
                         value = '',
                         isNew = false,
                         readOnly = false,
                         dispatch = action => null
                     }) => {
    const onChangeKey = e => dispatch(isNew ? didAddProperty(e.target.value, '') : didUpdateKey(idx, e.target.value));
    const onChangeValue = e => dispatch(isNew ? didAddProperty('', e.target.value) : didUpdateValue(idx, e.target.value));
    const onRemove = () => dispatch(didRemoveProperty(idx))

    return (
        <tr>
            <td><input className="w-100" value={propKey} readOnly={readOnly} onChange={onChangeKey}/></td>
            <td><input className="w-100" value={value} readOnly={readOnly} onChange={onChangeValue}/></td>
            <td><Conditional show={!(isNew || readOnly)}>{() =>
                <Button variant="outline-danger" onClick={onRemove} disabled={readOnly}> - </Button>
            }</Conditional></td>
        </tr>
    )
}

export const PropertyEditor = ({
                                   initialProperties = {},
                                   readOnly = false,
                                   watchProps = properties => null,
                                   onSave = properties => null
                               }) => {
    const propertyReducer = (properties, action) => {
        switch (action.type) {
            case UpdateKeyAction: {
                const {idx, newKey} = action.payload
                properties[idx].key = newKey
                break;
            }
            case UpdateValueAction: {
                const {idx, newValue} = action.payload
                properties[idx].value = newValue;
                break;
            }
            case AddPropertyAction: {
                const {newKey, newValue} = action.payload
                properties.push({key: newKey, value: newValue})
                break;
            }
            case RemovePropertyAction: {
                const {idx} = action.payload
                properties.splice(idx, 1)
                break;
            }
            default:
                throw new Error(`Unknown action type=${action.type}`)
        }
        if (watchProps) {
            watchProps(
                properties.reduce((props, {key, value}) => ({...props, [key]: value}), {})
            )
        }
    }

    const [properties, dispatch] = useImmerReducer(propertyReducer, Object.keys(initialProperties || {})
        .filter(key => key)
        .sort()
        .map(key => ({key, value: initialProperties[key]}))
    )
    const toRender = useMemo(
        () => readOnly ? properties : [...properties, {key: '', value: '', isNew: true}],
        [readOnly, properties]
    )

    return (
        <Table hover size="sm">
            <thead>
            <tr>
                <th>
                    <div className="b-flex-container">
                        property
                    </div>
                </th>
                <th>value</th>
                <th style={{width: '150px'}} className="text-right"/>
            </tr>
            </thead>
            <tbody>
            {toRender.map(({key, value, isNew}, i) => (
                <PropertyRow key={i}
                             idx={i} propKey={key} value={value} readOnly={readOnly} isNew={isNew} dispatch={dispatch}/>
            ))}
            </tbody>
        </Table>
    )
}
