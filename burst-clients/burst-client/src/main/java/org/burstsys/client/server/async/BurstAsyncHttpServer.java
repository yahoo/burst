/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.client.server.async;

import org.burstsys.client.server.BaseThriftServer;
import org.burstsys.gen.thrift.api.client.BTBurstService;

public class BurstAsyncHttpServer extends BaseThriftServer {
    public BurstAsyncHttpServer(BTBurstService.AsyncIface responder) {
        super(new BTBurstService.AsyncProcessor<>(responder));
    }
}
