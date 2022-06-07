/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.paths.funnels;

import org.burstsys.motif.common.DataType;
import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.eql.funnels.Funnel;
import org.burstsys.motif.paths.Path;

public class FunnelPathFieldPath extends FunnelPathsPath {
    public enum Field {
        ORDINAL(DataType.LONG),
        STARTTIME(DataType.LONG),
        ENDTIME(DataType.LONG),
        ISCOMPLETE(DataType.BOOLEAN),
        ISFIRST(DataType.BOOLEAN),
        ISLAST(DataType.BOOLEAN);

        private final DataType type;
        Field(DataType type) {
            this.type = type;
        }

        public DataType getDType() {
            return type;
        }
    }

    private final Field field;

    private final FunnelPathBase path;

    protected FunnelPathFieldPath(Funnel f, Field field) {
       super(f);
       this.field = field;
       // TODO hydra does not have path iteration so we must evaluate down at the step level
       // this.path = new FunnelPathsPath(f);
       this.path = new FunnelStepsPath(f);
    }

    @Override
    public String getPathAsString() {
        return path.toString() + "." + field;
    }

    @Override
    public Path getEnclosingStructure() {
        return path;
    }

    @Override
    public Path getParentStructure() {
        return path.getParentStructure();
    }

    public Field getField() { return field; }

    static public FunnelPathBase formPath(Funnel funnel, String field) {
        try {
            Field f = Field.valueOf(field.toUpperCase());
            return new FunnelPathFieldPath(funnel, f);
        } catch (Exception e) {
            throw new ParseException("Invalid field '" + field + "' in " + funnel.getName() + ".paths");
        }
    }
}
