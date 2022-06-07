/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio;

import java.nio.ByteBuffer;

class JNullMap {
    static void setAllNull(ByteBuffer data, byte count, int nullMapLocation) {
        int words = mapLongCount(count);
        int i = 0;
        while (i < words) {
            data.putLong(nullMapLocation + i*JBrioRollPress.LONG_SIZE, 0);
            i += 1;
        }
    }

    static boolean fieldTestNull(ByteBuffer data, byte key, int nullMapLocation) {
        int offset = getWordOffset(key);
        int index = getIndexOffset(key);
        long bitmap = data.getLong(nullMapLocation + (offset * JBrioRollPress.LONG_SIZE));
        return ((bitmap >> index) & 0x1) != 0;
    }

    static void fieldSetNull(ByteBuffer data, byte key, int nullMapLocation) {
        int offset = getWordOffset(key);
        int index = getIndexOffset(key);
        int position = nullMapLocation + (offset * JBrioRollPress.LONG_SIZE);
        long bit = 1 << index;
        long oldValue = data.getLong(position);
        long newValue = oldValue | bit;
        data.putLong(position, newValue);
    }

    static void fieldClearNull(ByteBuffer data, byte key, int nullMapLocation) {
        int offset = getWordOffset(key);
        int index = getIndexOffset(key);
        int position = nullMapLocation + (offset * JBrioRollPress.LONG_SIZE);
        long bit = 1 << index;
        long oldValue = data.getLong(position);
        long newValue = oldValue & ~bit;
        data.putLong(position, newValue);
    }

    private final static int word1 = 64;
    private final static int word2 = word1 + 128;
    private final static int word3 = word2 + 192;

    private static int getWordOffset(byte idx) {
        int i = (int) idx & 0xFF;
        if (i < 0) {
            String msg = "getWordOffset(" + i + ")";
            throw new RuntimeException(msg);
        }
        if (i < word1) return 0;
        if (i < word2) return 1;
        if (i < word3) return 2;
        String msg = "getWordOffset(" + i + ")";
        throw new RuntimeException(msg);
    }

    private static int getIndexOffset(byte idx) {
        int i = (int) idx & 0xFF;
        if (i < 0) {
            String msg = "getIndexOffset(" + i + ")";
            throw new RuntimeException(msg);
        }
        if (i < word1) return i;
        if (i < word2) return i - word1;
        if (i < word3) return i - word2;
        String msg = "getIndexOffset(" + i + ")";
        throw new RuntimeException(msg);
    }

    /*
    private static boolean readBit(byte i, long[] map) {
        checkFieldKey(i);
        int offset = getWordOffset(i);
        long bitmap = map[offset];
        int index = getIndexOffset(i);
        int bit = 1 << index;
        return (bitmap & bit) != 0;
    }

    private static void setBit(byte i, long[] map) {
        checkFieldKey(i);
        int offset = getWordOffset(i);
        int index = getIndexOffset(i);
        int bit = 1 << index;
        map[offset] = map[offset] | bit;
    }

    private static void clearBit(byte i, long[] map) {
        checkFieldKey(i);
        int offset = getWordOffset(i);
        int index = getIndexOffset(i);
        int bit = 1 << index;
        map[offset] = map[offset] & ~bit;
    }
    */

    private static int mapLongCount(byte count) {
        checkFieldCount(count);
        return (count / (JBrioRollPress.LONG_SIZE * 8)) + 1;
    }

    private static void checkFieldCount(int i) {
        if (i < 1 || i > 127) {
            String msg = "checkFieldCount(" + i + ") out of range";
            throw new RuntimeException(msg);
        }
    }

    /*
    private static int mapByteCount(byte count) {
        checkFieldCount(count);
        return mapLongCount(count) * JBrioRollPress.LONG_SIZE;
    }

    private static void checkFieldKey(int i) {
        if (i < 0 || i > 127) {
            String msg = "checkFieldKey(" + i + ") out of range";
            throw new RuntimeException(msg);
        }
    }
    */
}
