import React, {useEffect} from "react";
import {Pane} from "../../components/pane";
import "./settings-tab.scss"
import {Button} from "react-bootstrap";
import {useDispatch, useSelector} from "react-redux";
import {actions, selectors} from '../../store/reducers/settings';

const copyEnvToClipboard = setting => {
    const {key = "", value} = setting
    const envKey = key.replaceAll(".", "_").toUpperCase()
    navigator.clipboard.writeText(`${envKey}=${value}`)
        .catch(e => console.error(e))
}

const SettingsRow = ({setting, editState, onChange, onSave, onCancel}) => (
    <tr>
        <td style={{width: '20%'}}>{setting.key}</td>
        <td style={{width: '10%'}}>{setting.source}</td>
        <td style={{width: '30%'}}>{setting.description}</td>
        <td>{
            editState !== undefined ?
                <input className="w-100" value={editState} onChange={e => onChange(e.target.value)}/> :
                <span className="mono">{setting.value === '' ? `''` : `${setting.value}`}</span>
        }</td>
        <td style={{minWidth: '17rem', textAlign: 'right'}}>
            {editState !== undefined ?
                <>
                    <Button variant="link" className="text-success" onClick={onSave}>Save</Button>
                    <Button variant="link" className="text-danger" onClick={onCancel}>Cancel</Button>
                </> :
                <Button variant="link" onClick={() => onChange(setting.value)}>Edit</Button>
            }
            <Button variant="link" onClick={() => copyEnvToClipboard(setting)}>Copy Env</Button>
        </td>
    </tr>
)

const SettingsTabFn = () => {
    const dispatch = useDispatch()
    const settings = useSelector(selectors.selectFilteredSettings)
    const {
        filter,
        editing
    } = useSelector(({settings}) => ({
        filter: settings.filter,
        editing: settings.editing,
    }));
    useEffect(() => {
        dispatch(actions.loadSettings())
    }, [])

    return (
        <Pane id="workers" noFixedHeight>
            <Pane.Fixed>
                <div>
                    <span className="mx-2" style={{fontSize: '1.2rem'}}>Filter</span>
                    <input value={filter} onChange={e => dispatch(actions.setFilter(e.target.value))}/>
                </div>
            </Pane.Fixed>
            <Pane.Flex>
                <table className="table table-striped">
                    <thead>
                    <tr>
                        <th>Setting</th>
                        <th>Source</th>
                        <th>Description</th>
                        <th>Value</th>
                        <th>&nbsp;</th>
                    </tr>
                    </thead>
                    <tbody>
                    {settings.map(setting => (
                            <SettingsRow
                                key={setting.key}
                                setting={setting}
                                editState={editing[setting.key]}
                                onChange={value => dispatch(actions.updateEdit({key: setting.key, value}))}
                                onSave={() => dispatch(actions.saveSetting({
                                    key: setting.key,
                                    value: editing[setting.key]
                                }))}
                                onCancel={() => dispatch(actions.clearEdit({key: setting.key}))}
                            />
                        )
                    )}
                    </tbody>
                </table>
            </Pane.Flex>
        </Pane>
    );
}

export default SettingsTabFn
