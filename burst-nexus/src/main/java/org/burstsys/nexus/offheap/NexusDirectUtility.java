/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.nexus.offheap;

import io.netty.buffer.ByteBuf;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Utilities to help with unsafe direct memory
 */
public class NexusDirectUtility {

    /////////////////////////////////////////////////////////////////////////////////////////////////
    // Netty PooledUnsafeDirectByteBuf
    /////////////////////////////////////////////////////////////////////////////////////////////////

    private static Field _pooledUnsafeDirectByteBufMemoryAddressField = null;

    private static Class<?> _pooledUnsafeDirectByteBufClass = null;

    private static Class<?> pooledUnsafeDirectByteBufClass() {
        if (_pooledUnsafeDirectByteBufClass == null) {
            try {
                _pooledUnsafeDirectByteBufClass = Class.forName("io.netty.buffer.PooledUnsafeDirectByteBuf");
            } catch (Exception e) {
                throw new RuntimeException("Cannot access memory pointer field for buffer", e);
            }
            try {
                _pooledUnsafeDirectByteBufMemoryAddressField = _pooledUnsafeDirectByteBufClass.getDeclaredField("memoryAddress");
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("Cannot access memory pointer field for ByteBuf: " + _pooledUnsafeDirectByteBufClass, e);
            }
            // Allow modification on the field
            _pooledUnsafeDirectByteBufMemoryAddressField.setAccessible(true);
        }
        return _pooledUnsafeDirectByteBufClass;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    // Netty PooledSlicedByteBuf
    /////////////////////////////////////////////////////////////////////////////////////////////////

    private static Class<?> _pooledSlicedByteBuf = null;

    private static Method _pooledSlicedByteBufMemoryAddressMethod = null;

    private static Class<?> pooledSlicedByteBuf() {
        if (_pooledSlicedByteBuf == null)
            try {
                _pooledSlicedByteBuf = Class.forName("io.netty.buffer.WrappedByteBuf");
            } catch (Exception e) {
                throw new RuntimeException("Cannot access memory pointer field for buffer", e);
            }
        try {
            _pooledSlicedByteBufMemoryAddressMethod = _pooledSlicedByteBuf.getDeclaredMethod("memoryAddress");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Cannot access memory pointer field for ByteBuf: " + _pooledUnsafeDirectByteBufClass, e);
        }
        // Allow modification on the field
        _pooledSlicedByteBufMemoryAddressMethod.setAccessible(true);

        return _pooledSlicedByteBuf;
    }

    /**
     * This is a very unsafe way to get to the memory address field in a Netty ByteBuf assuming it was allocated
     * as a UnsafeDirectByteBuf in some manner. You better know what you're doing an hope the underlying implementation
     * doesn't change.
     *
     * @param buffer NETTY ByteBuf
     * @return long of the contents of memoryAddress
     */
    private static Long getByteBufMemoryAddress(ByteBuf buffer) {
        buffer.memoryAddress();
        Class<? extends ByteBuf> thisClazz = buffer.getClass();

        // if we have a straight direct buf we can just grab base memory ptr
        if (thisClazz == pooledUnsafeDirectByteBufClass()) {
            try {
                Object vo = _pooledUnsafeDirectByteBufMemoryAddressField.get(buffer);
                if (vo.getClass().equals(java.lang.Long.class))
                    return (Long) vo;
                else
                    throw new RuntimeException("Unexpected field value type, chCannot access memory pointer field for buffer: " + buffer);
            } catch (Exception e) {
                throw new RuntimeException("Cannot access memory pointer field for buffer: " + buffer, e);
            }

            // if we have a sliced byte buf we have to offset into underlying direct buf
        } else if (thisClazz == pooledSlicedByteBuf()) {

            try {
                return (long) _pooledSlicedByteBufMemoryAddressMethod.invoke(buffer);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Cannot access memory pointer field for buffer: " + buffer, e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException("Cannot access memory pointer field for buffer: " + buffer, e);
            }
        } else {
            // something else
            throw new RuntimeException("unhandled netty buffer type: " + thisClazz.getName());
        }
    }
}
