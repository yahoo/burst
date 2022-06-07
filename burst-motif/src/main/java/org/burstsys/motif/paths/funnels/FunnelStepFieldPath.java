/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.motif.paths.funnels;

import org.burstsys.motif.common.DataType;
import org.burstsys.motif.common.ParseException;
import org.burstsys.motif.motif.tree.eql.funnels.Funnel;
import org.burstsys.motif.paths.Path;

public class FunnelStepFieldPath extends FunnelStepsPath {
    // paths.steps.id, paths.steps.time
    public enum Field {
        ORDINAL(DataType.LONG),
        ISFIRST(DataType.BOOLEAN),
        ISLAST(DataType.BOOLEAN),
        ISCOMPLETE(DataType.BOOLEAN),
        ID(DataType.LONG),
        TIME(DataType.LONG);

        private final DataType type;
        Field(DataType type) {
            this.type = type;
        }

        public DataType getDType() {
            return type;
        }
    }

    private final Field field;

    private final FunnelStepsPath step;

    protected FunnelStepFieldPath(Funnel f, Field field) {
       super(f);
       this.field = field;
       this.step = new FunnelStepsPath(f);
    }

    @Override
    public String getPathAsString() {
        return step.toString() + "." + field;
    }

    @Override
    public Path getEnclosingStructure() {
        return step;
    }

    @Override
    public Path getParentStructure() {
        return step.getParentStructure();
    }

    public Field getField() { return field; }

    static public FunnelPathBase formPath(Funnel funnel, String field) {
        try {
            FunnelStepFieldPath.Field f = FunnelStepFieldPath.Field.valueOf(field.toUpperCase());
            return new FunnelStepFieldPath(funnel, f);
        } catch (Exception e) {
            throw new ParseException("Invalid field '" + field + "' in " + funnel.getName() + ".paths.steps");
        }
    }
}
