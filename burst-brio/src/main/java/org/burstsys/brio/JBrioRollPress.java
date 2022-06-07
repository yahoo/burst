/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Roll pressing follows a cursor based approach of writing an object based on the structure outlined
 * in the schema given when the press is started.
 */
public class JBrioRollPress {
    private JBrioSchema schema;
    private ByteBuffer data;
    private JZapDictionary dictionary;
    private JBrioScalarReference currentItem = null;

    private final static byte BOOLEAN_TYPE = (byte) 1;
    private final static byte BYTE_TYPE = (byte) 2;
    private final static byte SHORT_TYPE = (byte) 3;
    private final static byte INT_TYPE = (byte) 4;
    private final static byte LONG_TYPE = (byte) 5;
    private final static byte DOUBLE_TYPE = (byte) 6;
    private final static byte STRING_TYPE = (byte) 7;
    private final static byte COURSE_TYPE = (byte) 8;
    private final static byte FIRST_STRUCTURE_TYPE = (byte) 15;

    final static int SHORT_SIZE = 2;
    final static int INT_SIZE = 4;
    final static int LONG_SIZE = 8;
    private final static int STRING_KEY_SIZE = SHORT_SIZE;

    /* only handle string maps for now so hardcode the size */
    private final static int mapKeySize = STRING_KEY_SIZE;
    private final static int mapValuesSize = STRING_KEY_SIZE;

    public JBrioRollPress(JBrioSchema schema, int size) {
        this.schema = schema;
        this.data = ByteBuffer.allocateDirect(size);
        this.data.order(ByteOrder.LITTLE_ENDIAN);
        this.dictionary = new JZapDictionary(size);
        this.dictionary.initialize(0);
    }

    public void reset() {
        currentItem = null;
        data.clear();
        dictionary.reset();
    }

    /**
     * Start pressing an item in this schema.  This must be the first call of the press
     */
    public JBrioScalarReference beginItem() {
        if (currentItem != null)
            throw new RuntimeException("starting a new item while one is active");
        data.clear();
        dictionary.reset();
        currentItem = new JBrioScalarReference(schema.getRoot(), 0);
        return currentItem;
    }

    public ByteBuffer endItem(JBrioScalarReference reference) {
        if (currentItem != reference)
            throw new RuntimeException("passed item doesn't match active item");
        currentItem = null;
        // fix up the item as a blob with a dictionary
        data.position(reference.tailCursor);
        JBrioBlob.writeBlob(data, dictionary);
        ByteBuffer r = this.data.asReadOnlyBuffer();
        r.flip();
        return r;
    }

    private String typeToName(byte type) {
        switch (type) {
            case BOOLEAN_TYPE:
                return "BOOLEAN";
            case BYTE_TYPE:
                return "BYTE";
            case SHORT_TYPE:
                return "SHORT";
            case INT_TYPE:
                return "INT";
            case LONG_TYPE:
                return "LONG";
            case DOUBLE_TYPE:
                return "DOUBLE";
            case STRING_TYPE:
                return "STRING";
            case COURSE_TYPE:
                return "COURSE";
            default:
                if (type < FIRST_STRUCTURE_TYPE)
                    return "UNKNOWN TYPE";
                try {
                    return schema.getSchematic(type).getClass().getSimpleName();
                } catch (Exception e) {
                    return "UNKNOWN STRUCTURE TYPE";
                }
        }
    }

    abstract public class JBrioReference {
        protected JBrioSchematic schematic;
        protected int cursor;
        int tailCursor;

        private JBrioReference(JBrioSchematic s, int start) {
            schematic = s;
            cursor = start;
            tailCursor = start;
        }

        int size() {
            return tailCursor - cursor;

        }
    }

    public class JBrioScalarReference extends JBrioReference {
        private byte currentFieldIndex = 0;
        private JBrioReference currentReference = null;

        protected JBrioScalarReference(JBrioSchematic schematic, int start) {
            super(schematic, start);
            tailCursor = cursor + schematic.variableFieldsDataStart();

            // write out the version
            data.putInt(cursor, schematic.getVersionKey());
            // write out the initialized null table
            JNullMap.setAllNull(data, schematic.fixedFieldCount(), nullMapPosition());
            // write out the variable field offset table
            initializeOffsetTable(schematic);
        }

        private int nullMapPosition() {
            return cursor + schematic.nullsMapStart();
        }

        int fieldOffset(byte key) {
            if (schematic.isMap(key) || schematic.isVector(key) || !schematic.isValue(key)) {
                return cursor + schematic.variableFieldOffsetsStart() + schematic.variableFieldOffsetKeys(key) * INT_SIZE;
            } else {
                return cursor + schematic.fixedFieldOffsets(key);
            }
        }

        public int getFieldCount() {
            return schematic.getFieldCount();
        }

        public String getCurrentFieldName() {
            if (currentFieldIndex >= getFieldCount())
                return null;
            else
                return schematic.getField(currentFieldIndex);
        }

        private void checkNoReference() {
            if (currentFieldIndex >= getFieldCount())
                throw new RuntimeException("Only " + getFieldCount() + " fields are expected for " + schematic.getClass().getName());
            if (currentReference != null)
                throw new RuntimeException("unclosed reference outstanding");
        }

        private void checkReference(JBrioReference ref) {
            if (currentFieldIndex >= getFieldCount())
                throw new RuntimeException("Only " + getFieldCount() + " fields are expected for " + schematic.getClass().getName());
            if (currentReference == null)
                throw new RuntimeException("no reference outstanding");
            if (currentReference != ref)
                throw new RuntimeException("unclosed reference outstanding");
        }

        private void checkType(byte type) {
            if (schematic.valueTypeKey(currentFieldIndex) != type)
                throw new RuntimeException("expected a " + typeToName(schematic.valueTypeKey(currentFieldIndex)) +
                        " but got a " + typeToName(type) + " write");
        }

        public JBrioScalarReference startReference() {
            checkNoReference();
            if (schematic.isValue(currentFieldIndex) || schematic.isMap(currentFieldIndex) ||
                    schematic.isVector(currentFieldIndex))
                throw new RuntimeException("expecting " + typeToName(schematic.valueTypeKey(currentFieldIndex)) +
                        " not start of a scalar reference");
            byte tIndex = schematic.valueTypeKey(currentFieldIndex);
            JBrioScalarReference ref = new JBrioScalarReference(schematic.schematic(tIndex), tailCursor);
            currentReference = ref;
            return ref;
        }

        public void setNull() {
            checkNoReference();
            JNullMap.fieldSetNull(data, currentFieldIndex, nullMapPosition());
            currentReference = null;
            currentFieldIndex += 1;
        }

        public void endReference(JBrioScalarReference reference) {
            checkReference(reference);
            // write the cursor into the variable fields index
            data.putInt(fieldOffset(currentFieldIndex), reference.cursor - this.cursor);
            tailCursor = tailCursor + reference.size();
            currentReference = null;
            currentFieldIndex += 1;
        }

        public JBrioVectorReference startVectorReference() {
            checkNoReference();
            if (schematic.isValue(currentFieldIndex) || schematic.isMap(currentFieldIndex) ||
                    !schematic.isVector(currentFieldIndex))
                throw new RuntimeException("expecting " + typeToName(schematic.valueTypeKey(currentFieldIndex)) +
                        " not start of a reference vector");
            byte tIndex = schematic.valueTypeKey(currentFieldIndex);
            JBrioVectorReference ref = new JBrioVectorReference(schematic.schematic(tIndex), tailCursor);
            currentReference = ref;
            return ref;
        }

        public void endVectorReference(JBrioVectorReference reference) {
            checkReference(reference);
            data.putInt(fieldOffset(currentFieldIndex), reference.cursor - this.cursor);
            tailCursor = tailCursor + reference.size();
            currentReference = null;
            currentFieldIndex += 1;
        }

        public JBrioMapReference startMap(short size) {
            checkNoReference();
            JBrioMapReference ref = new JBrioMapReference(schematic, currentFieldIndex, tailCursor, size);
            currentReference = ref;
            return ref;
        }

        public void endMap(JBrioMapReference reference) {
            checkReference(reference);
            if (!reference.isDone())
                throw new RuntimeException("only " + reference.getCurrentIndex() + " of " + reference.getSize() +
                        " elements added to map");
            // sort the keys
            reference.sortKeys();
            // write the cursor into the variable fields index
            data.putInt(fieldOffset(currentFieldIndex), reference.cursor - this.cursor);
            tailCursor = tailCursor + reference.size();
            currentReference = null;
            currentFieldIndex += 1;
        }

        public void write(byte val) {
            checkNoReference();
            checkType(BYTE_TYPE);
            data.put(fieldOffset(currentFieldIndex), val);
            currentFieldIndex += 1;
        }

        public void write(boolean val) {
            checkNoReference();
            checkType(BOOLEAN_TYPE);
            data.put(fieldOffset(currentFieldIndex), val ? (byte) 1 : (byte) 0);
            currentFieldIndex += 1;
        }

        public void write(short val) {
            checkNoReference();
            checkType(SHORT_TYPE);
            data.putShort(fieldOffset(currentFieldIndex), val);
            currentFieldIndex += 1;
        }

        public void write(int val) {
            checkNoReference();
            checkType(INT_TYPE);
            data.putInt(fieldOffset(currentFieldIndex), val);
            currentFieldIndex += 1;
        }

        public void write(long val) {
            checkNoReference();
            checkType(LONG_TYPE);
            data.putLong(fieldOffset(currentFieldIndex), val);
            currentFieldIndex += 1;
        }

        public void write(double val) {
            checkNoReference();
            checkType(DOUBLE_TYPE);
            data.putDouble(fieldOffset(currentFieldIndex), val);
            currentFieldIndex += 1;
        }

        public void write(String val) {
            checkNoReference();
            checkType(STRING_TYPE);
            // add it to the dictionary if needed  and put the dictionary key here
            short key = dictionary.keyLookupWithAdd(val);
            data.putShort(fieldOffset(currentFieldIndex), key);
            currentFieldIndex += 1;
        }

        private void initializeOffsetTable(JBrioSchematic schematic) {
            // set offset table to defaults.
            int offsetTableOffset = cursor + schematic.variableFieldOffsetsStart();

            int i = 0;
            while (i < schematic.variableFieldCount()) {
                // initialize all offsets to zero
                data.putInt(offsetTableOffset, 0);
                offsetTableOffset += INT_SIZE;
                i += 1;
            }
        }
    }

    public class JBrioVectorReference extends JBrioReference {
        private JBrioScalarReference currentReference = null;

        JBrioVectorReference(JBrioSchematic s, int start) {
            super(s, start);
            /* initialize the vector size to zero */
            data.putShort(cursor, (short) 0);
            tailCursor = tailCursor + SHORT_SIZE;
        }

        private void updateVectorSize() {
            data.putShort(cursor, (short) (data.getShort(cursor) + 1));
        }

        public JBrioScalarReference startReference() {
            if (currentReference != null)
                throw new RuntimeException("unclosed scalar reference outstanding");
            // write the reference but leave space for the item size
            JBrioScalarReference ref = new JBrioScalarReference(schematic, tailCursor + INT_SIZE);
            currentReference = ref;
            return ref;
        }

        public void endReference(JBrioScalarReference reference) {
            // the reference being ended must match the type of the current field
            if (null == currentReference)
                throw new RuntimeException("no scalar reference outstanding");
            if (reference != currentReference)
                throw new RuntimeException("unclosed scalar reference outstanding");
            // ok write out the item size
            data.putInt(tailCursor, reference.size());
            tailCursor = tailCursor + reference.size() + INT_SIZE;
            currentReference = null;
            updateVectorSize();
        }
    }

    public class JBrioMapReference extends JBrioReference {
        private short currentIndex = 0;

        JBrioMapReference(JBrioSchematic s, byte idx, int start, short size) {
            super(s, start);

            // check the key types
            if (!schematic.isMap(idx))
                throw new RuntimeException("expecting " + typeToName(schematic.valueTypeKey(idx)) +
                        " not start of a map");
            if (schematic.valueTypeKey(idx) != STRING_TYPE)
                throw new RuntimeException("string values only for maps");
            if (schematic.mapTypeKey(idx) != STRING_TYPE)
                throw new RuntimeException("string keys only for maps");

            // initialize map size to zero
            data.putShort(cursor, size);

            // position the end of the map
            tailCursor = getValuesOffset() + getSize() * mapValuesSize;
        }

        private boolean isDone() {
            return currentIndex == getSize();
        }

        private short getSize() {
            return data.getShort(cursor);
        }

        private short getCurrentIndex() {
            return currentIndex;
        }

        private int getKeysOffset() {
            return cursor + SHORT_SIZE;
        }

        private int getValuesOffset() {
            return getKeysOffset() + getSize() * mapKeySize;
        }

        public void addMapEntry(String key, String val) {
            if (currentIndex >= getSize())
                throw new RuntimeException("adding " + (currentIndex+1) + " items to map of size " + getSize());
            // get key index
            short keyIdx = dictionary.keyLookupWithAdd(key);
            // get value index
            short valIdx = dictionary.keyLookupWithAdd(val);

            data.putShort(getKeysOffset() + currentIndex * mapKeySize, keyIdx);
            data.putShort(getValuesOffset() + currentIndex * mapValuesSize, valIdx);
            currentIndex += 1;
        }

        private void sortKeys() {
            sortKeysWithValues(0, getSize() - 1);
        }

        private void sortKeysWithValues(int lowerIndex, int higherIndex) {
            int i = lowerIndex;
            int j = higherIndex;

            int pivotKeyPoint = lowerIndex + ((higherIndex - lowerIndex) / 2);
            int pivotKeyValue = data.getShort(getKeysOffset() + (pivotKeyPoint * SHORT_SIZE));

            while (i <= j) {
                while (data.getShort(getKeysOffset() + (i * SHORT_SIZE)) < pivotKeyValue) {
                    i += 1;
                }
                while (data.getShort(getKeysOffset() + (j * SHORT_SIZE)) > pivotKeyValue) {
                    j -= 1;
                }
                if (i <= j) {
                    if (i != j) {
                        int leftCursorKeyOffset = getKeysOffset() + (i * SHORT_SIZE);
                        int rightCursorKeyOffset = getKeysOffset() + (j * SHORT_SIZE);
                        int leftCursorValueOffset = getValuesOffset() + (i * SHORT_SIZE);
                        int rightCursorValueOffset = getValuesOffset() + (j * SHORT_SIZE);
                        short rightKey = data.getShort(rightCursorKeyOffset);
                        short rightValue = data.getShort(rightCursorValueOffset);
                        data.putShort(rightCursorKeyOffset, data.getShort(leftCursorKeyOffset));
                        data.putShort(rightCursorValueOffset, data.getShort(leftCursorValueOffset));
                        data.putShort(leftCursorKeyOffset, rightKey);
                        data.putShort(leftCursorValueOffset, rightValue);
                    }
                    i += 1;
                    j -= 1;
                }
            }

            if (lowerIndex < j)
                sortKeysWithValues(lowerIndex, j);
            if (i < higherIndex)
                sortKeysWithValues(i, higherIndex);
        }
    }

    private static class JBrioBlob {
        private static final int BlobEncodingVersion = 2;

        /**
         * // BLOB FIELD 1:  ENCODING FORMAT VERSION
         * // BLOB FIELD 2:  ROOT OBJECT VERSION
         * // BLOB FIELD 3: DICTIONARY SIZE
         * // BLOB FIELD 4: DICTIONARY DATA
         * // BLOB FIELD 5: ROOT OBJECT SIZE
         * // BLOB FIELD 6: ROOT OBJECT DATA
         */
        static int writeBlob(ByteBuffer press, JZapDictionary dictionary) {
            // size of the blob header and the dictionary
            int shift = (4 * INT_SIZE) + dictionary.memorySize();
            int brioSize = press.position();
            int rootObjVersion = press.getInt(0);

            if (shift > press.remaining())
                throw new RuntimeException("not enough room for writing blob header and blob dictionary " +
                        shift + ">" + press.remaining());
            int pressCursor = 0;
            // move the current data over by the amount of the header and the size of the dictionary
            moveBytes(press, pressCursor, pressCursor + shift, brioSize);

            // encoding version
            press.putInt(pressCursor, BlobEncodingVersion);
            pressCursor += INT_SIZE;
            // root object version
            press.putInt(pressCursor, rootObjVersion);
            pressCursor += INT_SIZE;

            // dictionary size
            press.putInt(pressCursor, dictionary.memorySize());
            pressCursor += INT_SIZE;
            // transfer the dictionary
            pressCursor += dictionary.xferData(press, pressCursor);

            // size of the root data
            press.putInt(pressCursor, brioSize);
            pressCursor += INT_SIZE;

            // position the buffer position at the end
            press.position(pressCursor + brioSize);
            return press.position();
        }

        static private void moveBytes(ByteBuffer data, int fromOffset, int toOffset, int size) {
            if (fromOffset == toOffset || size <= 0)
                return;
            int inc = -1;
            int i = size - 1;
            if (fromOffset > toOffset) {
                inc = 1;
                i = 0;
            }
            while (i >= 0 && i < size) {
                data.put(toOffset + i, data.get(fromOffset + i));
                i += inc;
            }
        }
    }
}
