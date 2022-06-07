/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.paths.segments;

import org.burstsys.motif.common.DataType;
import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.eql.segments.Segment;
import org.burstsys.motif.paths.Path;

public class SegmentMembersFieldPath extends SegmentMembersPath {
    public enum Field {
        ID(DataType.LONG);

        private final DataType type;
        Field(DataType type) {
            this.type = type;
        }

        public DataType getDType() {
            return type;
        }
    }

    private final SegmentMembersFieldPath.Field field;

    private final SegmentMembersPath path;

    public SegmentMembersFieldPath(Segment t, Field field) {
       super(t);
       this.field = field;
       this.path = new SegmentMembersPath(t);
    }


    public SegmentMembersFieldPath.Field getField() { return field; }

    @Override
    public String getPathAsString() {
        return path.getPathAsString() + '.' + field;
    }

    @Override
    public Path getEnclosingStructure() {
        return path;
    }

    @Override
    public Path getParentStructure() {
        return path.getParentStructure();
    }

    static public SegmentMembersFieldPath formPath(Segment segment, String field) {
        try {
            SegmentMembersFieldPath.Field f = SegmentMembersFieldPath.Field.valueOf(field.toUpperCase());
            return new SegmentMembersFieldPath(segment, f);
        } catch (Exception e) {
            throw new ParseException("Invalid field '" + field + "' in " + segment.getName());
        }
    }
}
