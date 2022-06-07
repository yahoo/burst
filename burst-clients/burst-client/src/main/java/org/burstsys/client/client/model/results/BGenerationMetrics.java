/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.client.client.model.results;

import org.burstsys.gen.thrift.api.client.query.BTGenerationMetrics;

public class BGenerationMetrics {
    public final long byteCount;
    public final long itemCount;
    public final long sliceCount;
    public final long regionCount;

    public final long coldLoadAt;
    public final long coldLoadTookMs;
    public final long warmLoadAt;
    public final long warmLoadTookMs;
    public final long warmLoadCount;

    public final double sizeSkew;
    public final double timeSkew;
    public final double itemSize;
    public final double itemVariation;
    public final boolean loadInvalid;

    public final long earliestLoadAt;
    public final long rejectedItemCount;
    public final long potentialItemCount;
    public final double suggestedSampleRate;
    public final long suggestedSliceCount;

    public BGenerationMetrics(BTGenerationMetrics fromThrift) {
        byteCount = fromThrift.byteCount;
        itemCount = fromThrift.itemCount;
        sliceCount = fromThrift.sliceCount;
        regionCount = fromThrift.regionCount;
        coldLoadAt = fromThrift.coldLoadAt;
        coldLoadTookMs = fromThrift.coldLoadTookMs;
        warmLoadAt = fromThrift.warmLoadAt;
        warmLoadTookMs = fromThrift.warmLoadTookMs;
        warmLoadCount = fromThrift.warmLoadCount;
        sizeSkew = fromThrift.sizeSkew;
        timeSkew = fromThrift.timeSkew;
        itemSize = fromThrift.itemSize;
        itemVariation = fromThrift.itemVariation;
        loadInvalid = fromThrift.loadInvalid;
        earliestLoadAt = fromThrift.earliestLoadAt;
        rejectedItemCount = fromThrift.rejectedItemCount;
        potentialItemCount = fromThrift.potentialItemCount;
        suggestedSampleRate = fromThrift.suggestedSampleRate;
        suggestedSliceCount = fromThrift.suggestedSliceCount;
    }
}
