import React from "react";
import {Button, ButtonGroup, Table} from "react-bootstrap";
import PropTypes from "prop-types";

const PropertyRow = ({propKey, value, editing, readOnly, onSave, onCancel, onDelete, onEdit, onKeyUpdate, onValueUpdate}) => (
    <tr className="tr-middle">
        <td>{editing ?
            <input className="w-100" value={propKey} onChange={(e) => onKeyUpdate(e.target.value)}/> : propKey}</td>
        <td>{editing ?
            <input className="w-100" value={value} onChange={(e) => onValueUpdate(e.target.value)}/> : value}</td>
        {!readOnly && (
            <td className="text-right">
                <ButtonGroup>
                    <Button variant={editing ? 'outline-success' : 'outline-warning'} size="sm"
                            onClick={editing ? onSave : onEdit}>{editing ? 'Save' : 'Edit'}</Button>
                    <Button variant="outline-danger" size="sm"
                            onClick={editing ? onCancel : onDelete}>{editing ? 'Cancel' : '–'}</Button>
                </ButtonGroup>
            </td>
        )}
    </tr>
);

class PropertyEditor extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            newPair: false,
            editing: new Set(),
            snapshot: {},
            properties: props.properties
        };
    }

    componentWillReceiveProps(nextProps, nextContext) {
        const {properties = {}} = nextProps;
        this.setState({properties});
    }

    toggleEdit(key, restore = false) {
        const {editing, properties, snapshot} = this.state;
        if (editing.has(key)) {
            editing.delete(key);
            if (restore) {
                delete properties[key];
                const {key: oldKey, value: oldValue} = snapshot[key];
                properties[oldKey] = oldValue;
                delete snapshot[key];
            }
        } else {
            editing.add(key);
            snapshot[key] = {key, value: properties[key]};
        }
        this.setState({editing, snapshot, properties});
    }

    notifyChange() {
        this.props.onChange(this.state.properties)
    }

    removeKey(key) {
        const {properties} = this.state;
        delete properties[key];
        this.setState({properties});
        this.notifyChange();
    }

    updateKey(oldKey, newKey) {
        const {properties, editing, snapshot} = this.state;

        properties[newKey] = properties[oldKey];
        delete properties[oldKey];
        snapshot[newKey] = snapshot[oldKey];
        delete snapshot[oldKey];

        editing.delete(oldKey);
        editing.add(newKey);

        this.setState({properties, editing});
        this.notifyChange();
    }

    updateVal(key, newVal) {
        const {properties} = this.props;
        properties[key] = newVal;
        this.setState({properties});
        this.notifyChange();
    }

    render() {
        const {readOnly} = this.props;
        const {properties, editing, newPair} = this.state;
        const editingNewKey = newPair !== false;
        const tuples = Object.keys(properties || {})
            .filter(key => key)
            .sort()
            .map(key => ({key, value: properties[key]}));

        return (
            <div>
                <Table hover size="sm">
                    <thead>
                    <tr>
                        <th>
                            <div className="b-flex-container">
                                property
                            </div>
                        </th>
                        <th>value</th>
                        <th width="150" className="text-right">{!editingNewKey && !readOnly && (
                            <Button variant="outline-success" size="sm"
                                    onClick={() => this.setState({newPair: {key: '', value: ''}})}>+</Button>
                        )}
                        </th>
                    </tr>
                    </thead>
                    <tbody>
                    {tuples.map(({key, value}, i) => (
                        <PropertyRow
                            key={i}
                            propKey={key} value={value}
                            editing={editing.has(key)} readOnly={readOnly}
                            onKeyUpdate={(newKey) => this.updateKey(key, newKey)}
                            onValueUpdate={(newVal) => this.updateVal(key, newVal)}
                            onEdit={() => this.toggleEdit(key)}
                            onDelete={() => this.removeKey(key)}
                            onCancel={() => this.toggleEdit(key, true)}
                            onSave={() => this.toggleEdit(key)}
                        />)
                    )}
                    {editingNewKey && (
                        <PropertyRow
                            propKey={newPair.key} value={newPair.value} editing
                            onKeyUpdate={(key) => this.setState({newPair: {...newPair, key}})}
                            onValueUpdate={(value) => this.setState({newPair: {...newPair, value}})}
                            onCancel={() => this.setState({newPair: false})}
                            onSave={() => {
                                this.updateVal(newPair.key, newPair.value);
                                this.setState({newPair: false})
                            }}
                        />
                    )}
                    </tbody>
                </Table>
            </div>
        );
    }
}

PropertyEditor.propTypes = {
    properties: PropTypes.object.isRequired,
    onChange: PropTypes.func,
    readOnly: PropTypes.bool,
};

export default PropertyEditor
