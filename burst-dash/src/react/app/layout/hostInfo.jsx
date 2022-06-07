import React from "react";
import {get, set} from "../utility/local-storage";
import "./hostInfo.scss";
import request from "../utility/api-requests";

const fetchHostStateKey = 'fetchHostState';

class HostInfo extends React.Component {

    state = {
        hostInfo: null,
        running: get(fetchHostStateKey, true),
        online: false
    };

    componentDidMount() {
        if (this.state.running) {
            this.fetchHostInfo();
        }
    }

    fetchHostInfo = () => {
        const {handle} = this.state;
        request('/info/hostInfo', {withProgress: false, method: 'GET'})
            .then(data => this.setState({hostInfo: data, online: true}))
            .catch(error => this.setState({online: false}))
            .finally(() => {
                    if (!handle) {
                        this.setState(
                            {handle: setInterval(() => this.fetchHostInfo(), 15000)}
                        );
                    }
                }
            )
        ;
    };

    render() {
        const {hostInfo, online, running, handle} = this.state;

        const toggleFetch = () => {
            if (running) {
                clearInterval(handle)
                set(fetchHostStateKey, false);
                this.setState({running: false, handle: null})
            } else {
                this.fetchHostInfo()
                this.setState({running: true})
            }
        }

        if (!running) {
            return (
                <div className="host-info" onClick={toggleFetch}>Paused</div>
            )
        }

        let restText = "NA";
        let hostText = "NA";
        let upTimeText = "NA";
        let lavText = "NA";
        let osText = "NA";
        let memText = "NA";
        let coreText = "NA";
        let threadText = "NA";
        let gcText = "NA";

        if (hostInfo) {
            restText = `https://${hostInfo.hostAddress}:${hostInfo.restPort}`;
            hostText = `${hostInfo.hostName} (${hostInfo.hostAddress})`;
            upTimeText = hostInfo.uptime;
            lavText = hostInfo.loadAverage;
            osText = `${hostInfo.osName}, ${hostInfo.osVersion}, ${hostInfo.osArchitecture}`;
            memText = `${hostInfo.usedHeap} / ${hostInfo.committedHeap} / ${hostInfo.maxHeap} `;
            coreText = hostInfo.cores;
            threadText = `${hostInfo.currentThreads}/${hostInfo.peakThreads}`;
            gcText = hostInfo.gc;
            // we get this from the hostInfo and make it available globally...
            global.hostAddress = hostInfo.hostAddress;
        } else {
            this.fetchHostInfo();
        }

        const onlineStateStyle = {backgroundColor: '#F8F9F9', opacity: '1.0', filter: 'alpha(opacity = 100)'};
        const offlineStateStyle = {backgroundColor: '#FCF3CF', opacity: '0.4', filter: 'alpha(opacity = 40)'};

        let stateStyle = offlineStateStyle;

        if (online) {
            stateStyle = onlineStateStyle;
        } else {
            stateStyle = offlineStateStyle
        }

        return (
            <table style={stateStyle} className="host-info" onClick={toggleFetch}>
                <tbody>
                <tr>
                    <td className="label">Host:&#160;</td>
                    <td><a href={restText}>{hostText}</a></td>
                    <td className="spacer"/>
                    <td className="label">Up:&#160;</td>
                    <td> {upTimeText}, <span className="label">Load:&#160;</span>{lavText} </td>
                </tr>
                <tr>
                    <td className="label">OS:&#160;</td>
                    <td> {osText} </td>
                    <td className="spacer"/>
                    <td className="label">Mem:&#160;</td>
                    <td> {memText} </td>
                </tr>
                <tr>
                    <td className="label">CPU:&#160;</td>
                    <td><b>cores:</b>&#160;{coreText},&#160;
                        <b>threads:</b>&#160;{threadText}
                    </td>
                    <td className="spacer"/>
                    <td className="label">GC:&#160;</td>
                    <td> {gcText} </td>
                </tr>
                </tbody>
            </table>
        );
    }
}

export default HostInfo;
