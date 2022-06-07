/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class JZapDictionary {
    private int basePtr = 0;
    private ByteBuffer data;
    private static final int NullSlotOffset = 0;
    private static final int SizeOfBucket = JBrioRollPress.INT_SIZE;
    // private static final int KeyBits = 16;
    private static final int BucketBits = 6;
    private static final short BucketCount = (1 << 6); // 2^6
    private static final int BucketMask = BucketCount - 1;
    // private static final int BucketMask = 0x3f;
    private static final int OrdinalMask = 0xFFC0;
    private static final int MaxOrdinal = (1 << (16 - BucketBits)); // 2^16 - BucketBits

    JZapDictionary(int size) {
        data = ByteBuffer.allocateDirect(size);
        data.order(ByteOrder.LITTLE_ENDIAN);
    }

    int xferData(ByteBuffer data, int cursor) {
        ByteBuffer b = data.duplicate();
        ByteBuffer d = this.data.duplicate();
        d.position(getNextSlotOffset());
        d.flip();
        b.position(cursor);
        b.put(d);
        return b.position() - cursor;
    }

    /**
     * call when first created, only once
     */
    void initialize(int id) {
        putPoolId(id);
        reset();
    }

    /**
     * call each time you want to reuse the dictionary
     */
    void reset() {
        putWords(0);
        putNextSlotOffset(NullSlotOffset);
        initializeBuckets();
    }

    private JZapDictionary initializeBuckets() {
        int i = 0;
        while (i < BucketCount) {
            putBucketOffsetValue(i, NullSlotOffset);
            i += 1;
        }
        return this;
    }


    /**
     * lookup string and return key. Create new slot and add it if missing
     * and return that key.
     */
    short keyLookupWithAdd(String string) {
        if (string == null) {
            String msg = "string is null";
            throw new RuntimeException(msg);
        }
        short ordinal = 0; // head slot in bucket list
        byte[] bytes = string.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        /**
         * get the appropriate bucket based on a hash function
         */
        short bucket = getBucketIndex(string);

        // get the contents of that bucket
        int bucketListHeadOffset = getBucketOffsetValue(bucket);

        /**
         * Now that we know what is in the bucket
         * handle case of empty bucket
         */
        if (bucketListHeadOffset == NullSlotOffset) {
            // allocate a new slot
            int slotOffset = instantiateSlot();

            // update the bucket to point to this as the head of the bucket list
            putBucketOffsetValue(bucket, slotOffset);

            // put the string into it
            putStringData(slotOffset, bytes);

            // update the cursor for new slots
            putNextSlotOffset(slotOffset + slotSize(slotOffset));

            // create the key
            short zk = 0;
            zk = setBucket(zk, bucket);
            zk = setOrdinal(zk, ordinal);
            putKey(slotOffset, zk);
            return zk;
        }

        /**
         * we don't have an empty bucket so time to look at the bucket list
         */
        ordinal = 1;

        // keep track of last one seen (start with head of bucket list)
        int priorSlotOffset = bucketListHeadOffset;
        int currentSlotOffset = bucketListHeadOffset;

        /**
         * handle case of at least one in bucket list - loop through value slots until we find a match or
         * hit the end
         */
        while (currentSlotOffset != NullSlotOffset) {
            // check for a match
            if (stringMatches(currentSlotOffset, bytes))
                return getKey(currentSlotOffset);

            // store the prior end of the list for later linkage
            priorSlotOffset = currentSlotOffset;

            // move down the bucket list
            currentSlotOffset = getLink(currentSlotOffset);

            ordinal += 1; // move down the list
            if (ordinal >= MaxOrdinal) {
                throw new RuntimeException("max ordinal exceeded");
            }
        }

        /**
         * we hit the end of the bucket list without a match. Create a new slot and link
         * it in to the end of the bucket list
         */

        // get a new slot
        int newSlotOffset = instantiateSlot();

        putLink(priorSlotOffset, newSlotOffset);

        // put the string into it
        putStringData(newSlotOffset, bytes);

        // update the cursor for new slots
        putNextSlotOffset(newSlotOffset + slotSize(newSlotOffset));

        // create the key
        short zk = 0;

        // initialize the key
        zk = setBucket(zk, bucket);
        zk = setOrdinal(zk, ordinal);

        // store the key
        putKey(newSlotOffset, zk);

        // return the key
        return zk;
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // memory needed/used for this dictionary
    //////////////////////////////////////////////////////////////////////////////////////////
    public int memorySize() {
        return getNextSlotOffset();
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // mutating routines
    //////////////////////////////////////////////////////////////////////////////////////////

    private int instantiateSlot() {
        // increment the word count
        putWords(getWords() + 1);

        // check to see if we have allocated any slots yet
        int slotOffset = getNextSlotOffset();
        if (slotOffset == NullSlotOffset) {
            // if not, use the first one
            slotOffset = firstSlotOffset;
            putNextSlotOffset(slotOffset);
        }

        // return this slot value class
        initializeSlot(slotOffset);
        return slotOffset;
    }


    //////////////////////////////////////////////////////////////////////////////////////////
    // first integer in the memory block is the count of words
    //////////////////////////////////////////////////////////////////////////////////////////
    private int getWords() {
        return data.getInt(basePtr);
    }

    private void putWords(int words) {
        data.putInt(basePtr, words);
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // next integer in the memory block is the pool id
    //////////////////////////////////////////////////////////////////////////////////////////
    private final static int poolIdOffset = JBrioRollPress.INT_SIZE;

    /*
    private int getPoolId() {
        return data.getInt(basePtr + poolIdOffset);
    }
    */

    private void putPoolId(int id) {
        data.putInt(basePtr + poolIdOffset, id);
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // next integer in the memory block is the offset to the next slot (starts at zero)
    //////////////////////////////////////////////////////////////////////////////////////////
    private static final int nextSlotOffsetOffset = poolIdOffset + JBrioRollPress.INT_SIZE;

    private int getNextSlotOffset() {
        return data.getInt(basePtr + nextSlotOffsetOffset);
    }

    private void putNextSlotOffset(int offset) {
        data.putInt(basePtr + nextSlotOffsetOffset, offset);
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // next comes an array of buckets in the memory block
    //////////////////////////////////////////////////////////////////////////////////////////
    private static final int bucketsStartOffset = nextSlotOffsetOffset + JBrioRollPress.INT_SIZE;

    private short getBucketIndex(String s) {
        return (short) (Math.abs(s.hashCode()) % BucketCount);
    }

    private int getBucketOffsetValue(int index) {
        return data.getInt(basePtr + bucketsStartOffset + (SizeOfBucket * index));
    }

    private void putBucketOffsetValue(int index, int offset) {
        data.putInt(basePtr + bucketsStartOffset + (SizeOfBucket * index), offset);
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // then comes an sequence of string storage slots in the memory block
    //////////////////////////////////////////////////////////////////////////////////////////
    private static final int firstSlotOffset = bucketsStartOffset + (SizeOfBucket * BucketCount);

    //
    // Dictionary slot helper routines
    //
    private long initializeSlot(int ptr) {
        putLink(ptr, 0);
        putStringSize(ptr, (short) 0);
        return ptr;
    }

    private short getKey(int ptr) {
        return data.getShort(basePtr + ptr);
    }

    private void putKey(int ptr, short k) {
        data.putShort(basePtr + ptr, k);
    }

    private int linkStartOffset(int ptr) {
        return ptr + JBrioRollPress.SHORT_SIZE;
    }

    private int getLink(int ptr) {
        return data.getInt(basePtr + linkStartOffset(ptr));
    }

    private void putLink(int ptr, int size) {
        data.putInt(basePtr + linkStartOffset(ptr), size);
    }

    private int stringSizeStartOffset(int ptr) {
        return linkStartOffset(ptr) + JBrioRollPress.INT_SIZE;
    }

    private short getStringSize(int ptr) {
        return data.getShort(basePtr + stringSizeStartOffset(ptr));
    }

    private void putStringSize(int ptr, short size) {
        data.putShort(basePtr + stringSizeStartOffset(ptr), size);
    }

    private int stringDataStartOffset(int ptr) {
        return stringSizeStartOffset(ptr) + JBrioRollPress.SHORT_SIZE;
    }

    private void putStringData(int ptr, byte[] bytes) {
        putStringSize(ptr, (short) bytes.length);
        int i = 0;
        while (i < bytes.length) {
            data.put(basePtr + stringDataStartOffset(ptr) + i, bytes[i]);
            i += 1;
        }
        putLink(ptr, NullSlotOffset);
    }

    private int slotSize(int ptr) {
        return (stringDataStartOffset(ptr) + getStringSize(ptr)) - ptr;
    }


    private boolean stringMatches(int ptr, byte[] bytes) {
        if (bytes == null) {
            throw new RuntimeException("null string");
        }
        int sSize = getStringSize(ptr);
        if (bytes.length == 0 && sSize == 0)
            return true;
        if (bytes.length != sSize)
            return false;
        int i = 0;
        int off = basePtr + stringDataStartOffset(ptr);
        while (i < sSize) {
            if (bytes[i] != data.get(off + i))
                return false;
            i += 1;
        }
        return true;
    }

    //
    // Dictionary key helper routines
    //
    static private short setBucket(short key, short b) {
        if (b > BucketCount - 1 || b < 0)
            throw new RuntimeException("bucket size larger than " + BucketCount + " or less than zero");
        // merge the bucket into the lower bits
        return (short) ((key & OrdinalMask) | b);
    }

    static private short getBucket(short data) {
        return (short) (data & BucketMask);
    }

    static private short setOrdinal(short key, short value) {
        if (value > MaxOrdinal - 1 || value < 0)
            throw new RuntimeException("ordinal size larger than " + MaxOrdinal + " or less than zero");
        // merge the ordinal into the upper bits
        return (short) (value << BucketBits | getBucket(key));
    }

    // static private short getOrdinal(short data) { return (short) (data >>> BucketBits); }
}

