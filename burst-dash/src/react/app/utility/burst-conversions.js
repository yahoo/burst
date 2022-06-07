import numeral from "numeral";
import moment from "moment/moment";

const nsPerDay  = 1e9 * 60 * 60 * 24;
const nsPerHour = 1e9 * 60 * 60;
const nsPerMin  = 1e9 * 60;
const nsPerSec  = 1e9;
const nsPerMs   = 1e6;
const nsPerUs   = 1e3;

export function byteCount(bytes) {
    if (bytes === 0) return "empty";
    else return numeral(bytes).format('0.0b');
}

export const yymmdd_hhmmssFormat = 'YY/MM/DD HH:mm:ss UTC';
export const hhmmss_mmmddFormat = 'HH:mm:ss UTC, MMM DD';
export const hhmmssFormat = 'HH:mm:ss UTC';

const longDisplayFmt = Intl.DateTimeFormat(navigator.language,
    { hour: 'numeric', minute: 'numeric', second: 'numeric', day: 'numeric', month: 'short', year: 'numeric', timeZoneName: 'short' });
const timeDisplayFmt = Intl.DateTimeFormat(navigator.language,
    { hour: 'numeric', minute: 'numeric', second: 'numeric', timeZoneName: 'short' });


export function dateTime(ms, format = yymmdd_hhmmssFormat) {
    if (!ms || ms < 1) {
        return "";
    } else if (format === yymmdd_hhmmssFormat) {
        return longDisplayFmt.format(new Date(ms))
    } else if (format === hhmmssFormat) {
        return timeDisplayFmt.format(new Date(ms))
    } else {
        return moment.utc(ms).format(format);
    }
}

export function commaNumber(num) {
    if (num === 0) return "0";
    else return numeral(num).format('0,0')
}

export function ratio(num) {
    if (num === 0) return "0";
    else return numeral(num).format('0,0.000')
}

export function floatFormat(num) {
    if (num === 0) return "0.0";
    else return numeral(num).format('0,0.000')
}

export function elapsedTime(ms) {
    return elapsedTimeNs(ms * 1e6);
}

export function elapsedTimeNs(ns) {
    if (ns <= 0 || isNaN(ns)) return "";
    else return prettyTimeFromNanos(ns);
}

export function prettyByteRate(bytes, ns) {
    const rate = bytes/(ns/1e6);
    if (isNaN(rate) || rate < 0) return "???";
    if (rate < 1e3) return (rate).toFixed(2) + ' B/s';
    if (rate < 1e6) return (rate / 1e3).toFixed(2) + ' KB/s';
    if (rate < 1e9) return (rate / 1e6).toFixed(2) + ' MB/s';
    if (rate < 1e12) return (rate / 1e9).toFixed(2) + ' GB/s';
    return (rate / 1e12).toFixed(2) + ' TB/s';
}

export function prettyTimeFromNanos(nanos) {
    if (nanos > nsPerDay) return (nanos / nsPerDay).toFixed(2) + ' d';
    if (nanos > nsPerHour) return (nanos / nsPerHour).toFixed(2) + ' hr';
    if (nanos > nsPerMin) return (nanos / nsPerMin).toFixed(2) + ' min';
    if (nanos > nsPerSec) return (nanos / nsPerSec).toFixed(2) + ' s';
    if (nanos > nsPerMs) return (nanos / nsPerMs).toFixed(2) + ' ms';
    if (nanos > nsPerUs) return (nanos / nsPerUs).toFixed(2) + ' us';
    return nanos + ' ns';
}

export function prettySizeFromBytes(bytes) {
    if (bytes < 0) return "???";
    if (bytes < 1e3) return (bytes).toFixed(2) + ' B';
    if (bytes < 1e6) return (bytes / 1e3).toFixed(2) + ' KB';
    if (bytes < 1e9) return (bytes / 1e6).toFixed(2) + ' MB';
    if (bytes < 1e12) return (bytes / 1e9).toFixed(2) + ' GB';
    return (bytes / 1e12).toFixed(2) + ' TB';
}
