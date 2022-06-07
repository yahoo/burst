/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.client.client.model.results;

import org.burstsys.gen.thrift.api.client.query.BTExecutionMetrics;

public class BExecutionMetrics {
    public final long scanTimeNs;
    public final long scanWorkNs;
    public final long queryCount;
    public final long rowCount;
    public final long succeeded;
    public final long limited;
    public final long overflowed;
    public final long compileTimeNs;
    public final long cacheHits;

    public BExecutionMetrics(BTExecutionMetrics fromThrift) {
        scanTimeNs = fromThrift.scanTimeNs;
        scanWorkNs = fromThrift.scanWorkNs;
        queryCount = fromThrift.queryCount;
        rowCount = fromThrift.rowCount;
        succeeded = fromThrift.succeeded;
        limited = fromThrift.limited;
        overflowed = fromThrift.overflowed;
        compileTimeNs = fromThrift.compileTimeNs;
        cacheHits = fromThrift.cacheHits;
    }
}
