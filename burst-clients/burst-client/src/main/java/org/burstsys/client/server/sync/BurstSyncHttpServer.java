/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.client.server.sync;

import org.burstsys.client.server.BaseThriftServer;
import org.burstsys.gen.thrift.api.client.BTBurstService;

public class BurstSyncHttpServer extends BaseThriftServer {
    public BurstSyncHttpServer(BTBurstService.Iface responder) {
        super(new BTBurstService.Processor<>(responder));
    }
}
