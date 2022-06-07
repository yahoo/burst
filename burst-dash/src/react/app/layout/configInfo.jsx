import React from "react";
import request from "../utility/api-requests";

const Pair = ({label, value, title = ""}) => (
    <div title={title}>
        <span className="label">{label}: </span>
        <span className="value">{value}</span>
    </div>
)

class ConfigInfo extends React.Component {
    state = {
        configInfo: null
    };

    componentDidMount() {
        this.fetchConfigInfo();
        setInterval(() => this.fetchConfigInfo(), 180_000);
    }

    fetchConfigInfo() {
        request('/info/buildInfo', {method: 'GET'})
            .then(data => this.setState({configInfo: data}))
            .catch(e => console.log("Failed to fetch config info", e));
    };

    render() {
        const {configInfo} = this.state;

        if (!configInfo) {
            return <div/>
        }

        const {build, commitId, branch} = configInfo;
        return (
            <div id="config-info">
                <Pair label="version" value={build}/>
                <Pair label="commit" value={commitId} title={`Branch: ${branch}`}/>
            </div>
        )
    }
}

export default ConfigInfo
