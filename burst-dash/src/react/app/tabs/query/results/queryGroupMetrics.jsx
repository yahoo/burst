import React from "react";
import PropTypes from "prop-types";
import {byteCount, commaNumber, dateTime, elapsedTime, elapsedTimeNs, ratio} from "../../../utility/burst-conversions";
import {SingleRowTable} from "./helpers";
import {LabeledItem} from "../../../utility/helpers";

class QueryGroupMetrics extends React.Component {
    render() {
        const {resultGroup} = this.props;
        const {groupKey: key, groupMetrics} = resultGroup;
        const {executionMetrics: execution, generationMetrics: generation} = groupMetrics;
        return (
            <div className="group-metrics">
                <SingleRowTable>
                    <LabeledItem name="name" value={key.groupName}/>
                    <LabeledItem name="uid" value={key.groupUid}/>
                    <LabeledItem name="domain" value={groupMetrics.generationKey.domainKey}/>
                    <LabeledItem name="view" value={groupMetrics.generationKey.viewKey}/>
                    <LabeledItem name="genClk" value={dateTime(groupMetrics.generationKey.generationClock)} condensed/>
                </SingleRowTable>
                <SingleRowTable>
                    <LabeledItem name="Load Invalid" value={generation.loadInvalid.toString()}/>
                    <LabeledItem name="Bytes" value={byteCount(generation.byteCount)}/>
                    <LabeledItem name="Slices" value={commaNumber(generation.sliceCount)}/>
                    <LabeledItem name="Regions" value={commaNumber(generation.regionCount)}/>
                    <LabeledItem name="Suggested Sample Rate" value={ratio(generation.suggestedSampleRate)}/>
                    <LabeledItem name="Suggested Slices" value={commaNumber(generation.suggestedSliceCount)}/>
                </SingleRowTable>
                <SingleRowTable>
                    <LabeledItem name="warm" value={(
                        <>
                            {elapsedTime(generation.warmLoadTook)}
                            <span>&nbsp;@&nbsp;</span>
                            {dateTime(generation.warmLoadAt)}
                        </>
                    )}/>
                    <LabeledItem name="cold" value={(
                        <>
                            {elapsedTime(generation.coldLoadTook)}
                            <span>&nbsp;@&nbsp;</span>
                            {dateTime(generation.coldLoadAt)}
                        </>
                    )}/>
                </SingleRowTable>
                <SingleRowTable>
                    <LabeledItem name="Items" condensed />
                    <LabeledItem name="loaded" value={commaNumber(generation.itemCount)}/>
                    <LabeledItem name="rejected" value={commaNumber(generation.rejectedItemCount)}/>
                    <LabeledItem name="potential" value={commaNumber(generation.potentialItemCount)}/>
                    <LabeledItem name="itemSize" value={byteCount(generation.itemSize)}/>
                    <LabeledItem name="variation" value={ratio(generation.itemVariation)}/>
                </SingleRowTable>
                <SingleRowTable>
                    <LabeledItem name="scanTime" value={elapsedTimeNs(execution.scanTime)}/>
                    <LabeledItem name="scan" value={elapsedTimeNs(execution.scanWork)}/>
                    <LabeledItem name="itemWk" value={elapsedTimeNs(execution.scanWork / generation.itemCount)}/>
                    <LabeledItem name="compile" value={elapsedTimeNs(execution.compileTime)}/>
                    <LabeledItem name="hits" value={commaNumber(execution.cacheHits)}/>
                    <LabeledItem name="queries" value={commaNumber(execution.queryCount)}/>
                    <LabeledItem name="rows" value={commaNumber(execution.rowCount)}/>
                    <LabeledItem name="succeeds" value={commaNumber(execution.succeeded)}/>
                    <LabeledItem name="limits" value={commaNumber(execution.limited)}/>
                    <LabeledItem name="overflows" value={commaNumber(execution.overflowed)} condensed/>
                </SingleRowTable>
            </div>
        );
    }
}

QueryGroupMetrics.propTypes = {
    resultGroup: PropTypes.object
};

export default QueryGroupMetrics
