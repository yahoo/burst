import React from "react";
import PropTypes from "prop-types";
import {
    commaNumber,
    dateTime,
    elapsedTime,
    elapsedTimeNs,
    prettyByteRate,
    prettySizeFromBytes,
    ratio
} from "../../utility/burst-conversions";
import {Table} from "react-bootstrap";

const eventLoadLatency = (event) => {
    if (event.loadCount <= 0) return "---";
    return elapsedTime(event.loadTime / event.loadCount)
};

const eventLoadSize = (event) => {
    if (event.loadCount <= 0) return "---";
    return prettySizeFromBytes(event.loadSize / event.loadCount)
};

const eventScanLatency = (event) => {
    if (event.scanCount <= 0) return "---";
    return elapsedTimeNs(event.scanTime / event.scanCount)
};

const eventLoadRate = (event) => {
    if (event.loadCount <= 0) return "---";
    return prettyByteRate(event.loadSize / event.loadCount, (event.loadTime * 1000) / event.loadCount)
};

const eventScanRate = (event) => {
    if (event.scanCount <= 0) return "---";
    let num = event.scanCount / (event.scanTime / 1e9);
    return ratio(num)
};

const MESSAGE_STYLE = {
    margin: '0.1em',
    backgroundColor: 'white',
    fontFamily: 'monospace',
    overflow: 'wrap',
    whiteSpace: 'pre-wrap',
    wordWrap: 'break-word',
}

const ProfilerEvent = ({event, showMessage, onToggle}) => <>
    <tr className="event">
        {/* --------------------event--------------------------   */}
        <td className="text-body">{event.index}</td>
        <td className="timestamp">{dateTime(event.time)}</td>
        <td className="w-6">{elapsedTime(event.elapsed)}</td>

        {/* ----------------- data -----------------------------   */}
        <td>{commaNumber(event.loadCount)}</td>
        <td>{eventLoadSize(event)}</td>
        <td className="w-6">{eventLoadLatency(event)}</td>
        <td className="w-6">{eventLoadRate(event)}</td>

        {/* ----------------------scans------------------------   */}
        <td>{commaNumber(event.success)}</td>
        <td>{commaNumber(event.failure)}</td>
        <td className="w-6">{eventScanLatency(event)}</td>
        <td className="scan-rate">{eventScanRate(event)}</td>

        {/* ------------------message----------------------------   */}
        <td>
            <pre className={event.isError ? 'error' : ''}>{event.message}</pre>
            <a className="pull-right pointer" onClick={() => onToggle(event.index)}>
                {showMessage ? 'Hide' : 'Show'} message
            </a>
        </td>
    </tr>
    {showMessage && (
        <tr className="message">
            <td colSpan="12">
                <pre className={event.isError ? 'error' : ''} style={MESSAGE_STYLE}>{event.message}</pre>
            </td>
        </tr>
    )}
</>


class ProfilerResults extends React.Component {
    state = {
        showEventMessage: new Set()
    };

    toggleMessage = (index) => {
        const {showEventMessage} = this.state;
        if (showEventMessage.has(index)) {
            showEventMessage.delete(index);
        } else {
            showEventMessage.add(index);
        }
        this.setState({showEventMessage})
    };

    render() {
        const {events} = this.props;
        const {showEventMessage: showMessage} = this.state;
        if (events === null || events.length === 0) {
            return <div className="burst-empty-message w-100"> no profiler events... </div>;
        }

        return (
            <div className="profiler-results w-100">
                <Table striped bordered hover size="sm">
                    <thead>
                    <tr className="header-groups">
                        <th colSpan={3}>event</th>
                        <th colSpan={4}>data</th>
                        <th colSpan={4}>scans</th>
                        <th/>
                    </tr>
                    <tr className="header-names">
                        <th>index</th>
                        <th>stamp</th>
                        <th>elapsed</th>

                        <th>loads</th>
                        <th>size</th>
                        <th>latency</th>
                        <th>rate</th>

                        <th>success</th>
                        <th>failure</th>
                        <th>latency</th>
                        <th>rate</th>

                        <th>message</th>
                    </tr>
                    </thead>
                    <tbody className="profiler-events">
                    {events.map(event => <ProfilerEvent key={event.index} event={event}
                                                        showMessage={showMessage.has(event.index)}
                                                        onToggle={this.toggleMessage}/>
                    )}
                    </tbody>
                </Table>
            </div>
        );
    }

}

ProfilerResults.propTypes = {
    events: PropTypes.array,
};

export default ProfilerResults
