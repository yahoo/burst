/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.client.client.exception;

public class BurstRequestException extends RuntimeException {
    public BurstRequestException(String message) {
        super(message);
    }

    public BurstRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public BurstRequestException(Throwable cause) {
        super(cause);
    }
}
