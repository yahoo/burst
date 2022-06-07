/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.client.util;

import org.burstsys.gen.thrift.api.client.query.BTCell;
import org.burstsys.gen.thrift.api.client.query.BTResult;
import org.burstsys.gen.thrift.api.client.query.BTResultSet;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CsvBuilder {
    private CsvBuilder() {
    }

    public static String allResults(BTResult result) {
        StringBuilder b = new StringBuilder();

        for (Map.Entry<String, BTResultSet> entry : result.getResultSets().entrySet()) {
            b.append("\nQuery: ").append(entry.getKey()).append("\n");

            BTResultSet resultSet = entry.getValue();
            b.append(fromResultSet(resultSet));
        }

        return b.toString();
    }

    public static String fromResultSet(BTResultSet resultSet) {
        StringBuilder b = new StringBuilder();
        List<String> columns = resultSet.getColumnNames();
        b.append(
                columns.stream()
                       .map(c -> c.replace(",", "\\,"))
                       .collect(Collectors.joining(","))
        ).append("\n");
        for (List<BTCell> row : resultSet.getRows()) {
            b.append(
                    row.stream()
                       .map(cell -> DatumValue.extractVal(cell.dType, cell.datum).toString())
                       .collect(Collectors.joining(","))
            ).append("\n");
        }
        return b.toString();
    }
}
