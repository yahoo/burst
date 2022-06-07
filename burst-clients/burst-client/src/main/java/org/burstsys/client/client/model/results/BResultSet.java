/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.client.client.model.results;

import org.burstsys.client.client.BurstSyncClient;
import org.burstsys.gen.thrift.api.client.query.BTResultSet;
import org.burstsys.gen.thrift.api.client.query.BTResultSetMeta;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BResultSet {
    public final String name;
    public final Meta meta;
    public final List<String> columnNames;
    public final List<BDatum.DataType> columnTypes;
    public final List<List<BCell>> rows;

    public BResultSet(BTResultSet fromThrift) {
        name = fromThrift.name;
        meta = Meta.fromThrift(fromThrift.meta);
        columnNames = Collections.unmodifiableList(fromThrift.columnNames);
        columnTypes = BurstSyncClient.mappedList(fromThrift.columnTypes, BDatum.DataType::fromThrift);
        rows = BurstSyncClient.mappedList(fromThrift.rows, row -> BurstSyncClient.mappedList(row, BCell::new));
    }

    public String name() {
        return name;
    }

    public static class Meta {
        public static Meta fromThrift(BTResultSetMeta fromThrift) {
            return new Meta(fromThrift);
        }

        public final boolean succeeded;
        public final boolean limited;
        public final boolean overflowed;
        public final long rowCount;
        public final Map<String, String> properties;

        private Meta(BTResultSetMeta fromThrift) {
            succeeded = fromThrift.succeeded;
            limited = fromThrift.limited;
            overflowed = fromThrift.overflowed;
            rowCount = fromThrift.rowCount;
            properties = Collections.unmodifiableMap(new HashMap<>(fromThrift.properties));
        }
    }
}
