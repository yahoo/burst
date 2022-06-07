import {Item} from "./helpers";
import {elapsedTimeNs} from "../utility/burst-conversions";
import React from "react";

export const ExecutionMetrics = ({metrics}) => (
    <table className="summary-table">
        <tbody>
        <tr>
            <Item label="Query count" value={metrics.queryCount}/>
            <Item label="Successful" value={metrics.succeeded}/>
            <Item label="Total Rows" value={metrics.rowCount}/>
        </tr>
        <tr>
            <Item label="Scan time (clock)" value={elapsedTimeNs(metrics.scanTime)}/>
            <Item label="Scan work (total)" value={elapsedTimeNs(metrics.scanWork)}/>
        </tr>
        <tr>
            <Item label="Limited" value={metrics.limited}/>
            <Item label="Overflowed" value={metrics.overflowed}/>
        </tr>
        <tr>
            <Item label="Compile time" value={elapsedTimeNs(metrics.compileTime)}/>
            <Item label="Cache hits" value={metrics.cacheHits}/>
        </tr>
        </tbody>
    </table>
)
