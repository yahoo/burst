/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.offheap;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class TeslaDirectBufferFactory {
    public static final Constructor<?> DIRECT_BUFFER_CONSTRUCTOR;

    static {
        final ByteBuffer direct;
        direct = ByteBuffer.allocateDirect(1);
        Constructor<?> directBufferConstructor;
        long address = -1;
        try {
            final Object maybeDirectBufferConstructor =
                    AccessController.doPrivileged(new PrivilegedAction<Object>() {
                        @Override
                        public Object run() {
                            try {
                                final Constructor<?> constructor =
                                        direct.getClass().getDeclaredConstructor(long.class, int.class);
                                constructor.setAccessible(true);
                                return constructor;
                            } catch (NoSuchMethodException e) {
                                return e;
                            } catch (SecurityException e) {
                                return e;
                            }
                        }
                    });

            directBufferConstructor = (Constructor<?>) maybeDirectBufferConstructor;
        } catch (Exception e) {
            directBufferConstructor = null;

        }
        DIRECT_BUFFER_CONSTRUCTOR = directBufferConstructor;
    }

    static public ByteBuffer directBuffer(long address, int size) {
        try {
            return (ByteBuffer) DIRECT_BUFFER_CONSTRUCTOR.newInstance(address, size);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException("failed");
        }
    }
}
