/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.client.client.model.results;

import org.burstsys.gen.thrift.api.client.query.BTResult;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BResult {

    public final String guid;
    public final String message;
    public final BViewGeneration generation;
    public final BGenerationMetrics generationMetrics;
    public final BExecutionMetrics executionMetrics;
    public final Map<String, BResultSet> resultSets;

    public BResult(BTResult thrift) {
        guid = thrift.guid;
        message = thrift.message;
        generation = new BViewGeneration(thrift.generation);
        generationMetrics = new BGenerationMetrics(thrift.generationMetrics);
        executionMetrics = new BExecutionMetrics(thrift.executionMetrics);
        resultSets = Collections.unmodifiableMap(
                thrift.resultSets.values().stream()
                        .map(BResultSet::new)
                        .collect(Collectors.toMap(BResultSet::name, Function.identity()))
        );
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BResult{");
        sb.append("guid='").append(guid).append('\'');
        sb.append(", message='").append(message).append('\'');
        sb.append(", resultSets=").append(resultSets.size());
        sb.append('}');
        return sb.toString();
    }
}
