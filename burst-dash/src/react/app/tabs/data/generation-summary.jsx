import React from "react";
import {Col, Container, Row} from "react-bootstrap";
import numeral from "numeral";
import {byteCount} from "../../utility/burst-conversions";

const SummaryItem = ({name, value, width = 4}) => (
    <Col xs={width}>{`${name}: ${value}`}</Col>
);

const sum = (metrics, prop) => metrics.reduce((total, entry) => total + entry[prop], 0);
const avg = (metrics, prop) => metrics.reduce((total, entry) => total + entry[prop], 0) / metrics.length;
const max = (metrics, prop) => metrics.reduce((val, entry) => entry[prop] > val ? entry[prop] : val, Number.MIN_VALUE);
const min = (metrics, prop) => metrics.reduce((val, entry) => entry[prop] < val ? entry[prop] : val, Number.MAX_VALUE);

/**
 * Display summary data about the generations in the cell
 */
class GenerationSummary extends React.Component {
    render() {
        const {generations} = this.props;
        if (!generations.length) {
            return (
                <Container className="burst-border generation-summary">
                    <h4>Data Generation Summary</h4>
                    <p>No data loaded</p>
                </Container>
            )
        }

        const metrics = generations.map(g => g.metrics);
        const statuses = generations.map(g => g.state).reduce((totals, state) => {
            if (!totals[state]) totals[state] = 0;
            totals[state] += 1;
            return totals;
        }, {});
        return (
            <Container className="burst-border generation-summary">
                <h4>Data Generation Summary <small>({generations.length} total)</small></h4>
                <Row>
                    <SummaryItem name="Generations InMemory" value={numeral(statuses['InMemory']).format('0,0')} />
                    <SummaryItem name="Generations OnDisk" value={numeral(statuses['OnDisk']).format('0,0')} />
                    <SummaryItem name="Generations NoData" value={numeral(statuses['NoData']).format('0,0')} />
                </Row>
                <Row>
                    <SummaryItem name="Max bytes" value={byteCount(max(metrics, 'byteCount'))} />
                    <SummaryItem name="Min bytes" value={byteCount(min(metrics, 'byteCount'))} />
                    <SummaryItem name="Total bytes" value={byteCount(sum(metrics, 'byteCount'))} />
                </Row>
                <Row>
                    <SummaryItem name="Max timeSkew" value={numeral(max(metrics, 'timeSkew')).format('0,0')} />
                    <SummaryItem name="Min timeSkew" value={numeral(min(metrics, 'timeSkew')).format('0,0')} />
                    <SummaryItem name="Average timeSkew" value={numeral(avg(metrics, 'timeSkew')).format('0,0')} />
                </Row>
            </Container>
        );
    }
}

export default GenerationSummary
