/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.client.client.model.results;

import org.burstsys.gen.thrift.api.client.query.BTViewGeneration;

public class BViewGeneration {
    public final String domainUdk;
    public final String viewUdk;
    public final long generationClock;

    public BViewGeneration(BTViewGeneration fromThrift) {
        domainUdk = fromThrift.domainUdk;
        viewUdk = fromThrift.viewUdk;
        generationClock = fromThrift.generationClock;
    }
}
