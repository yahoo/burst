import React from "react";
import {Form, Table} from "react-bootstrap";

class TorcherEvents extends React.Component {
    state = {
        showDebug: false,
        eventCount: 50
    };

    toggleDebug = (e) => {
        const showDebug = e.target.checked;
        this.setState({showDebug});
    };

    setEventCount = (e) => {
        const eventCount = e.target.value || 1;
        this.setState({eventCount});
    }

    render() {
        const {events} = this.props;
        const {eventCount} = this.state;
        const showDebug = this.props.showDebug || this.state.showDebug;
        const toShow = events.filter(e => e.level !== "DEBUG" || showDebug).slice(0, eventCount);

        if (toShow.length === 0) {
            return (
                <div id="torcherResultMainPanel" className="w-100 burst-border">
                    <div className="burst-empty-message">No Torcher Output</div>
                </div>
            )
        }

        return (
            <div id="torcherResultMainPanel" className="w-100 burst-border overflow-auto p-3">
                <Form inline className="pb-3">
                    <Form.Group className="pr-2">
                        <Form.Check id="show-debug" label="Show debug messages" checked={showDebug}
                                    onChange={this.toggleDebug}/>
                    </Form.Group>
                    <Form.Group>
                        <Form.Label className="pr-2">Show</Form.Label>
                        <Form.Control value={eventCount} size="sm" type="number" onChange={this.setEventCount}/>
                    </Form.Group>
                </Form>
                <Table size="sm" striped bordered>
                    <tbody>
                    {toShow.map((e, i) => (
                            <tr key={i}>
                                <td className="status">{e.level}</td>
                                <td>
                                    <pre>{e.data}</pre>
                                </td>
                            </tr>
                        )
                    )}
                    </tbody>
                </Table>
            </div>
        );
    }
}

export default TorcherEvents
