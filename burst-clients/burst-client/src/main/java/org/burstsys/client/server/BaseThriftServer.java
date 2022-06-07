/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.client.server;

import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServlet;

/**
 * ThriftServer maps a handler to a servlet that can be mounted in an HTTP server.
 */
public abstract class BaseThriftServer {
    protected TServlet servlet;

    protected BaseThriftServer(TProcessor processor) {
        servlet = new TServlet(processor, new TBinaryProtocol.Factory());
    }

    /**
     * Get the servlet that needs to be installed in a HTTP server
     * @return a servlet that responds to thrift requests
     */
    public TServlet getServlet() {
        return servlet;
    }

}
