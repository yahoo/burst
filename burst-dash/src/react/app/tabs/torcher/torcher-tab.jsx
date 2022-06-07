import './torcher.scss';

import React from "react";
import {connect} from "react-redux";
import PropTypes from "prop-types";
import {Button, Col, Form, Row} from "react-bootstrap";
import {Controlled as CodeMirror} from "react-codemirror2";

import TorcherEvents from "./torcher-events"
import {Pane} from "../../components/pane";
import {codeMirrorQueryOptions as options} from "../../utility/code-mirror";
import {startTorcher, stopTorcher, updateSource} from "../../store/reducers/torcher";
import TorcherStats from "./torcher-stats";

class TorcherTab extends React.Component {
    state = {
        value: ''
    };

    onSelectFile = (e) => {
        const reader = new FileReader();
        reader.onload = () => {
            this.updateSource(reader.result);
        };
        reader.readAsText(e.target.files[0]);
    };

    onChange = (editor, data, value) => {
        this.updateSource(value);
    };

    updateSource = (source) => {
        this.props.updateSource(source)
    };

    toggleTorcher = () => {
        const {source, status: {running}} = this.props;
        if (running) {
            this.props.stopTorcher();
        } else {
            this.props.startTorcher(source);
        }
    };

    render() {
        const {summary, events, source, status} = this.props;
        const {running} = status;
        return (
            <Pane id="torcher" noFixedHeight>
                <Pane.Fixed className={summary && 'with-summary'} minHeight={100}>
                    <TorcherStats summary={summary} status={status}/>
                </Pane.Fixed>
                <Pane.Fixed>
                    <Form className="w-100 row row-cols-auto">
                        <Form.Group as={Row} className="align-items-center">
                            <Form.Label column>Torcher spec:</Form.Label>
                            <Col xs={6}>
                                <Form.Control type='file' accept='.json' onChange={this.onSelectFile}/>
                            </Col>
                            <Col>
                                <Button size="sm" variant={running ? 'outline-danger' : 'outline-success'}
                                        onClick={this.toggleTorcher}>
                                    <span
                                        className={`fa fa-${running ? 'stop' : 'play'}`}/>&nbsp;&nbsp; {running ? 'Stop' : 'Start'}
                                </Button>
                            </Col>
                        </Form.Group>
                    </Form>
                </Pane.Fixed>
                <Pane.Flex className="position-relative">
                    <CodeMirror value={source} className="h-100"
                                options={{...options, mode: "text/javascript"}} onBeforeChange={this.onChange}/>
                </Pane.Flex>
                <Pane.Flex>
                    <TorcherEvents events={events} showDebug={false}/>
                </Pane.Flex>
            </Pane>
        );
    }
}

TorcherTab.contextTypes = {
    store: PropTypes.object,
};

const mapStateToProps = ({torcherTab}) => {
    const {source, status, events, summary} = torcherTab;
    return {source, status, events, summary};
};

const mapDispatchToProps = (dispatch) => ({
    updateSource: source => dispatch(updateSource(source)),
    startTorcher: source => dispatch(startTorcher(source)),
    stopTorcher: () => dispatch(stopTorcher()),
});

TorcherTab = connect(mapStateToProps, mapDispatchToProps)(TorcherTab);

export default TorcherTab
