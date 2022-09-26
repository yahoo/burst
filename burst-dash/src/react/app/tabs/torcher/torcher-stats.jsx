import React, {Component} from 'react';
import {commaNumber, prettySizeFromBytes, prettyTimeFromNanos} from "../../utility/burst-conversions";
import {Table} from "react-bootstrap";
import {LabeledItem} from "../../utility/helpers";

const NS_TO_MS = 1000000;
const SEC = 1000;
const MIN = SEC * 60;
const HOUR = MIN * 60;
const fmt = (n) => n.toFixed(0).padStart(2, '0');

class TorcherStats extends Component {

    componentDidMount() {
        this.interval = setInterval(() => this.props.status.running && this.forceUpdate(), 500);
    }

    componentWillUnmount() {
        clearTimeout(this.interval);
    }

    renderSummaryStats(summary) {
        return (
            <>
                <tr>
                    <LabeledItem name="Bytes" value={prettySizeFromBytes(summary.byteCount)} condensed/>
                    <LabeledItem name="Objects" value={commaNumber(summary.objectCount)} condensed/>
                    <LabeledItem name="Items" value={commaNumber(summary.itemCount)} condensed/>
                    <LabeledItem name="Limited" value={summary.limitCount} condensed/>
                    <LabeledItem name="Overflowed" value={summary.overflowCount} condensed/>
                </tr>
                <tr>
                    <LabeledItem name="Mean minute Rate" value={summary.observedColdLoadMeanRate.toFixed(3)} condensed/>
                    <LabeledItem name="5 minute Rate" value={summary.observedColdLoad5Rate.toFixed(3)} condensed/>
                    <LabeledItem name="15 minute Rate" value={summary.observedColdLoad15Rate.toFixed(3)} condensed/>
                </tr>
                <tr>
                    <LabeledItem name={<>Reported Cold Load<sup>99th</sup></>}
                                 value={prettyTimeFromNanos(summary.reportedColdLoad99Duration.toFixed(3))} condensed/>
                    <LabeledItem name={<>Reported Cold Load<sup>Max</sup></>}
                                 value={prettyTimeFromNanos(summary.reportedColdLoadMaxDuration.toFixed(3))} condensed/>
                </tr>
                <tr>
                    <LabeledItem name={<>Observed Cold Load<sup>99th</sup></>}
                                 value={prettyTimeFromNanos(summary.observedColdLoad99Duration.toFixed(3))} condensed/>
                    <LabeledItem name={<>Observed Cold Load<sup>Max</sup></>}
                                 value={prettyTimeFromNanos(summary.observedColdLoadMaxDuration.toFixed(3))} condensed/>
                </tr>
                <tr>
                    <LabeledItem name="First Query Failures" value={summary.firstQueryFailuresCount} condensed/>
                    <LabeledItem name="First Query NoData Failures" value={summary.firstQueryNoDataFailuresCount}
                                 condensed/>
                    <LabeledItem name="All Query Failures" value={summary.queryFailuresCount} condensed/>
                </tr>
            </>
        )
    }

    render() {
        const {summary, status} = this.props;

        const {running, schema, counter, concurrency, startTimeMs = 0, endTimeMs = 0, currentDatasetIndex, duration, datasetCount} = status;
        const runtime = running ? Date.now() - startTimeMs : endTimeMs - startTimeMs;
        const hours = runtime > HOUR ? Math.floor(runtime / HOUR) : 0;
        const minutes = runtime > MIN ? Math.floor((runtime % HOUR) / MIN) : 0;
        const seconds = Math.floor((runtime / SEC) % 60);
        const runClock = `${fmt(hours)}:${fmt(minutes)}:${fmt(seconds)}`;

        const dlLink = (
            <a href="/api/supervisor/torcher/getColdLoadStatisticsTableCSV" download="cold-load-statistics.csv">
                Cold Loads CSV
            </a>
        );
        return (
            <Table size="sm" variant="borderless" style={{backgroundColor: 'rgb(248, 248, 248)'}}>
                <tbody>
                <tr>
                    <LabeledItem name="Torcher" value={running ? "Running" : "Stopped"} condensed/>
                </tr>
                <tr>
                    <LabeledItem name="Schema Name" value={schema || '?'} condensed/>
                    <LabeledItem name="Parallelism" value={concurrency > -1 ? concurrency : '?'} condensed/>
                    <LabeledItem name="Duration" value={duration || '?'} condensed/>
                    <LabeledItem name="RunningTime" value={runClock || '?'} condensed/>
                </tr>
                <tr>
                    <LabeledItem name="Prepared Datasets" value={datasetCount > -1 ? datasetCount : '?'} condensed/>
                    <LabeledItem name="Dataset Index" value={currentDatasetIndex > -1 ? currentDatasetIndex : '?'}
                                 condensed/>
                    <LabeledItem name="Dataset Count" value={counter > -1 ? counter : '?'} condensed/>
                </tr>
                {summary && this.renderSummaryStats(summary)}
                {summary && (
                    <tr>
                        <LabeledItem name="Statistics Downloads" value={dlLink} condensed/>
                    </tr>
                )}
                </tbody>
            </Table>
        )
    }
}

export default TorcherStats;
