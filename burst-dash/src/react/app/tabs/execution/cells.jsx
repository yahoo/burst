import React from 'react';
import {ProgressBar} from "react-bootstrap";
import cn from "classnames";

import {byteCount, elapsedTime} from "../../utility/burst-conversions";
import {makeFilterCell} from "../../components/table-cells";
import OverLink from "../../components/over-link";

const Separator = ({left, right}) => left > 0 && right > 0 ? ' / ' : null;
const Count = ({value, type, title}) => value > 0 && <span className={`text-${type}`} title={title}>{value}</span>
export const ProgressBarCell = ({value}) => {
    const width = 90
    if (!value) {
        return <div style={{width}}>{' '}</div>;
    }
    const {success, failure, cancelled, total} = value
    const summary = [[success, 'succeeded'], [failure, 'failed'], [cancelled, 'cancelled']].map(
        ([num, status]) => `${num} ${status}`
    ).join(',')
    const title = `${value.total} particles; ${summary}`
    if (value.running) {
        return (
            <div title={title} style={{height: '1.5rem', width}}>
                <ProgressBar key={value.key} max={value.total || 1} className="h-100">
                    <ProgressBar variant="success" key={1} now={value.success} max={value.total || 1}/>
                    <ProgressBar variant="warning" key={2} now={value.running} max={value.total || 1}/>
                    <ProgressBar variant="danger" key={3} now={value.failure} max={value.total || 1}/>
                </ProgressBar>
            </div>
        )
    }
    return <div title={summary} style={{width}}>
        <Count value={success} type="success" title={`${success} succeeded`}/>
        <Separator left={success} right={failure} />
        <Count value={failure} type="danger" title={`${failure} failed`}/>
        <Separator left={failure} right={cancelled} />
        <Count value={cancelled} type="warning" title={`${cancelled} cancelled`}/>
        {' / '}{total}
    </div>;
};

export const sortOver = (row1, row2, columnId) => {
    const [left, right] = getRowValues(row1, row2, columnId)
    return left.domainKey !== right.domainKey
        ? compareBasic(left.domainKey, right.domainKey)
        : compareBasic(left.viewKey, right.viewKey);
}
export const filterOver = (rows, columnIds, filterValue) => {
    return rows.filter(r => `${r.values?.over?.domainKey} ${r.values?.over?.viewKey}`.includes(filterValue))
}

export const DataOverCell = ({value: over}) => <OverLink over={over}/>

export const DataSizeCell = ({value, row}) => {
    if (typeof value !== "number") {
        return "";
    }
    const {wave} = row.original
    const {beginMillis, metrics: {generationMetrics}} = wave
    const coldLoad = generationMetrics.coldLoadAt > beginMillis
    return <span className={coldLoad ? "details" : ""} title={coldLoad ? "Cold load" : ""}>
        {byteCount(value)}
    </span>;
};

export const sortDuration = (row1, row2, columnId) => {
    const [left, right] = getRowValues(row1, row2, columnId)
    if (left[0] < 0) {
        return -1
    } else if (right[0] < 0) {
        return 1
    } else if (left.length === 1 || right.length === 1) {
        return compareBasic(left[0], right[0])
    } else if (roundToSec(left[0]) > 59 && roundToSec(right[0]) > 59) {
        // sort timeouts
        if (left[1] < 0) {
            return -1
        } else if (right[1] < 0) {
            return 1
        }
        return compareBasic(left[1], right[1])
    }
    return compareBasic(left[0], right[0])
}

export const DurationCell = ({value}) => {
    const length = value?.length ?? -1;
    if (length === 1) {
        return elapsedTime(value[0])
    } else if (length === 2 && value[0] > 0) {
        const [request, wave] = value
        return request > wave ? elapsedTime(request) : (
            <>
                <span>{elapsedTime(request)}</span>
                <br/> ({elapsedTime(wave)})
            </>
        );
    }
    return ""
};

export const SkewHeader = () => <span>Skew<sub>t(p)</sub></span>

export const SkewCell = ({value}) => (typeof value === "number") ? value.toFixed(2) : (value || "");

export const PARTICLE_STATES = [
    "UNKNOWN",
    "PENDING",
    "IN_PROGRESS",
    "LATE",
    "SUCCEEDED",
    "FAILED",
    "CANCELLED",
]
export const REQUEST_STATES = [
    "IN_PROGRESS",
    "SUCCESS",
    "TIMEOUT",
    "EXCEPTION",
    "NOT_READY",
    "INVALID",
];
export const WaveStatusFilter = makeFilterCell(REQUEST_STATES, 'WaveStatusFilter')

export const WaveStatusCell = ({value: {state, status = '', waveState, waveStatus = ''}}) => (
    <>
        <span title={status} className={cn('status', state)}>{state}</span>
        {waveState && state !== 'SUCCESS' && state !== waveState && (
            <>
                <br/>
                {' ('}
                <span title={waveStatus} className={cn('status', waveState)}>{waveState}</span>
                )
            </>
        )}
    </>
)
export const filterState = (rows, columnIds, filterValue) => {
    return rows.filter(r => r.values?.state?.state === filterValue)
}

export const ParticleStatusFilter = makeFilterCell(PARTICLE_STATES, 'ParticleStatusFilter')

export const ParticleStatusCell = ({value: {state, message}}) => (
    <span title={message} className={cn('status', state)}>{state}</span>
)

function roundToSec(ms) {
    return ms - (ms % 1000)
}

function compareBasic(left, right) {
    return left === right ? 0 : left > right ? 1 : -1;
}

function getRowValues(row1, row2, columnId) {
    return [row1.values[columnId], row2.values[columnId]]
}
