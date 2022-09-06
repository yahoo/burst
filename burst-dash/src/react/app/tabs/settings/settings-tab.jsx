import React from "react";
import {Pane} from "../../components/pane";
import "./settings-tab.scss"
import request from "../../utility/api-requests"
import {Button} from "react-bootstrap";

class SettingsTab extends React.Component {
    state = {
        filter: "",
        settings: []
    };

    componentDidMount() {
        setInterval(() => this.fetchSettings, 15000)
        this.fetchSettings();
    }

    fetchSettings() {
        request('/info/configInfo', {method: 'GET', withProgress: false})
            .then(r => {
                const settings = Object.keys(r).sort()
                    .map(k => {
                        const def = r[k];
                        if (k !== "burst.fabric.cache.spindles") {
                            return {key: k, ...def}
                        } else {
                            return {key: k, ...def, value: def.value.split(";").join(";\n")}
                        }
                    })
                this.setState({settings})
            })
            .catch(e => console.error("failed to fetch settings"))
    }

    envKeyFor(keyName) {
        return keyName.replaceAll(".", "_").toUpperCase()
    }

    copyEnvFor(setting) {
        const {key = "", value} = setting
        const envKey = this.envKeyFor(key)
        navigator.clipboard.writeText(`${envKey}=${value}`)
            .catch(e => console.error(e))
    }

    render() {
        const {settings, filter} = this.state;
        return (
            <Pane id="workers" noFixedHeight>
                <Pane.Fixed>
                    <div>
                        <span className="ms-2" style={{fontSize: '1.2rem'}}>Filter</span>
                        <input value={filter} onChange={e => this.setState({filter: e.target.value})} />
                    </div>
                </Pane.Fixed>
                <Pane.Flex>
                    <table className="table table-striped">
                        <thead>
                        <tr>
                            <th>Setting</th>
                            <th>Description</th>
                            <th>Value</th>
                            <th>&nbsp;</th>
                        </tr>
                        </thead>
                        <tbody>
                        {settings
                            .filter(s => !filter || s.key.includes(filter) || `${s.value}`.includes(filter) || s.description.includes(filter))
                            .map(s => (
                            <tr key={s.key}>
                                <td style={{width: '20%'}}>{s.key}</td>
                                <td style={{width: '20%'}}>{s.source}</td>
                                <td style={{width: '20%'}}>{s.description}</td>
                                <td style={{width: '20%'}}><span className="mono">{s.value === '' ? `''` : `${s.value}`}</span></td>
                                <td style={{width: '20%', textAlign: 'right'}}>
                                    <Button variant="link" onClick={() => this.copyEnvFor(s)}>Copy Env</Button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </Pane.Flex>
            </Pane>
        );
    }
}

export default SettingsTab
