import React from 'react';
import {Item} from "./helpers";
import {byteCount, dateTime, elapsedTime} from "../utility/burst-conversions";

export const GenerationMetrics = ({metrics}) => (
    <table className="w-100 summary-table">
        <tbody>
        <tr>
            <Item label="Total size" value={byteCount(metrics.byteCount)}/>
            <Item label="Items" value={metrics.itemCount}/>
            <Item label="Slices" value={metrics.sliceCount}/>
            <Item label="Regions" value={metrics.regionCount}/>
        </tr>
        <tr>
            <Item label="First load" value={dateTime(metrics.earliestLoadAt)}/>
            <Item label="Cold load" value={`${elapsedTime(metrics.coldLoadTook)} @ ${dateTime(metrics.coldLoadAt)}`}/>
            <Item label="Warm load" value={`${elapsedTime(metrics.warmLoadTook)} @ ${dateTime(metrics.warmLoadAt)}`}/>
            <Item label="Warm loads" value={metrics.warmLoadCount}/>
        </tr>
        <tr>
            <Item label="Size skew" value={metrics.sizeSkew}/>
            <Item label="Time skew" value={metrics.timeSkew}/>
            <Item label="Item size" value={byteCount(metrics.itemSize)}/>
            <Item label="Item variation" value={metrics.itemVariation}/>
        </tr>
        <tr>
            <Item label="Rejected items" value={metrics.rejectedItemCount}/>
            <Item label="Potential items" value={metrics.potentialItemCount}/>
            <Item label="SSR" labelTitle="Suggested Sample Rate" value={metrics.suggestedSampleRate}/>
            <Item label="Suggested slices" value={metrics.suggestedSliceCount}/>
        </tr>
        </tbody>
    </table>
)
