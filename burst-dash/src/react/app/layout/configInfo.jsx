import React from "react";
import request from "../utility/api-requests";
import {selectors} from "../store/reducers/host";
import {useSelector} from "react-redux";

const Pair = ({label, value, title = ""}) => (
    <div title={title}>
        <span className="label">{label}: </span>
        <span className="value">{value}</span>
    </div>
)

const ConfigInfo = () => {
    const configInfo = useSelector(selectors.gitInfo);
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
export default ConfigInfo
