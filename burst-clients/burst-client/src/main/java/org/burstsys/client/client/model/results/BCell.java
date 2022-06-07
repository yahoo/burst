/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.client.client.model.results;

import org.burstsys.gen.thrift.api.client.query.BTCell;
import org.burstsys.gen.thrift.api.client.query.BTCellType;

public class BCell {
    public enum CellType {
        DIMENSION, AGGREGATION;

        public static CellType fromThrift(BTCellType thrift) {
            switch (thrift) {
                case Dimension:
                    return DIMENSION;
                case Aggregation:
                    return AGGREGATION;
                default:
                    throw new IllegalStateException("Unknown thrift cell type: " + thrift);
            }
        }
    }

    public final CellType cellType;
    public final BDatum.DataType dataType;
    public final BDatum datum;
    public final boolean isNull;
    public final boolean isNaN;

    public BCell(BTCell thrift) {
        cellType = CellType.fromThrift(thrift.cType);
        dataType = BDatum.DataType.fromThrift(thrift.dType);
        datum = BDatum.fromThriftCell(thrift, dataType);
        isNull = thrift.isNull;
        isNaN = thrift.isNaN;
    }
}
