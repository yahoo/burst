import expect from 'expect.js';
import * as Conversions from '../../../app/utility/burst-conversions';

describe('BurstConversions', function () {
    it('#byteCount', function () {
        expect(Conversions.byteCount(0)).to.equal('empty');
        expect(Conversions.byteCount(1000)).to.equal('1.0KB');
        expect(Conversions.byteCount(1000 * 1000)).to.equal('1.0MB')
    });

    it('#dateTime', function () {
        expect(Conversions.dateTime(null)).to.equal('');
        expect(Conversions.dateTime('')).to.equal('');
        expect(Conversions.dateTime(-1)).to.equal('');
        expect(Conversions.dateTime(0)).to.equal('');
        expect(Conversions.dateTime(1)).to.equal('70/01/01 00:00:00 UTC')

    });

    it('#commaNumber', function () {
        expect(Conversions.commaNumber(0)).to.equal('0');
        expect(Conversions.commaNumber(1000)).to.equal('1,000');
    });

    it('#ratio', function () {
        expect(Conversions.ratio(0)).to.equal('0');
        expect(Conversions.ratio(1000.2345)).to.equal('1,000.235');
    });

    it('#floatFormat', function () {
        expect(Conversions.floatFormat(0)).to.equal('0.0');
        expect(Conversions.floatFormat(1000.2345)).to.equal('1,000.235')
    });

    it('#elaspedTime', function () {
        expect(Conversions.elapsedTime(-1)).to.equal('');
        expect(Conversions.elapsedTime(1.1)).to.equal('1.10 ms');
    });

    it('#elapsedTimeNs', function () {
        expect(Conversions.elapsedTimeNs(-1)).to.equal('');
        expect(Conversions.elapsedTimeNs(1)).to.equal('1 ns');
    });

    it('#prettyByteRate', function () {
        const nsPerSec = 1000 * 1000;
        expect(Conversions.prettyByteRate(-1, nsPerSec)).to.equal('???');
        expect(Conversions.prettyByteRate(0, nsPerSec)).to.equal('0.00 B/s');
        expect(Conversions.prettyByteRate(999, nsPerSec)).to.equal('999.00 B/s');
        expect(Conversions.prettyByteRate(1000, nsPerSec)).to.equal('1.00 KB/s');
        expect(Conversions.prettyByteRate(1000 * 1000, nsPerSec)).to.equal('1.00 MB/s');
        expect(Conversions.prettyByteRate(1000 * 1000 * 9.9, nsPerSec)).to.equal('9.90 MB/s');
        expect(Conversions.prettyByteRate(1000 * 1000 * 1000, nsPerSec)).to.equal('1.00 GB/s');
        expect(Conversions.prettyByteRate(1000 * 1000 * 1000 * 1000, nsPerSec)).to.equal('1.00 TB/s');
    });

    it('#prettyTimeFromNanos', function () {
        expect(Conversions.prettyTimeFromNanos(0)).to.equal('0 ns');

        expect(Conversions.prettyTimeFromNanos(1000)).to.equal('1000 ns');
        expect(Conversions.prettyTimeFromNanos(1001)).to.equal('1.00 us');

        expect(Conversions.prettyTimeFromNanos(1000 * 1000)).to.equal('1000.00 us');
        expect(Conversions.prettyTimeFromNanos(1000 * 1001)).to.equal('1.00 ms');

        expect(Conversions.prettyTimeFromNanos(1000 * 1000 * 1000)).to.equal('1000.00 ms');
        expect(Conversions.prettyTimeFromNanos(1000 * 1000 * 1001)).to.equal('1.00 s');

        expect(Conversions.prettyTimeFromNanos(60 * 1000 * 1000 * 1000)).to.equal('60.00 s');
        expect(Conversions.prettyTimeFromNanos(60 * 1000 * 1000 * 1001)).to.equal('1.00 min');

        expect(Conversions.prettyTimeFromNanos(60 * 1000 * 1000 * 1000)).to.equal('60.00 s');
        expect(Conversions.prettyTimeFromNanos(60 * 1000 * 1000 * 1001)).to.equal('1.00 min');

        expect(Conversions.prettyTimeFromNanos(60 * 60 * 1000 * 1000 * 1000)).to.equal('60.00 min');
        expect(Conversions.prettyTimeFromNanos(60 * 60 * 1000 * 1000 * 1001)).to.equal('1.00 hr');

        expect(Conversions.prettyTimeFromNanos(24 * 60 * 60 * 1000 * 1000 * 1000)).to.equal('24.00 hr');
        expect(Conversions.prettyTimeFromNanos(24 * 60 * 60 * 1000 * 1000 * 1001)).to.equal('1.00 d');
    });

    it('#prettySizeFromBytes', function () {
        expect(Conversions.prettySizeFromBytes(-1)).to.equal('???');
        expect(Conversions.prettySizeFromBytes(1)).to.equal('1.00 B');
        expect(Conversions.prettySizeFromBytes(1000)).to.equal('1.00 KB');
        expect(Conversions.prettySizeFromBytes(1000 * 1000)).to.equal('1.00 MB');
        expect(Conversions.prettySizeFromBytes(1000 * 1000 * 1000)).to.equal('1.00 GB');
        expect(Conversions.prettySizeFromBytes(1000 * 1000 * 1000 * 1000)).to.equal('1.00 TB');
    });
});
