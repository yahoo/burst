/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.http.service.thrift

import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroupMetrics
import org.burstsys.gen.thrift.api.client.query.BTExecutionMetrics
import org.burstsys.gen.thrift.api.client.query.BTGenerationMetrics

object metrics {

  def toThrift(groupMetrics: FabricResultGroupMetrics): (BTGenerationMetrics, BTExecutionMetrics) = {
    val gen = groupMetrics.generationMetrics
    val exec = groupMetrics.executionMetrics
    (
      new BTGenerationMetrics(
        gen.byteCount, gen.itemCount, gen.sliceCount, gen.regionCount,
        gen.coldLoadAt, gen.coldLoadTook, gen.warmLoadAt, gen.warmLoadTook, gen.warmLoadCount,
        gen.sizeSkew, gen.timeSkew, gen.itemSize, gen.itemVariation,
        gen.loadInvalid, gen.earliestLoadAt, gen.rejectedItemCount, gen.potentialItemCount,
        gen.suggestedSampleRate, gen.suggestedSliceCount, gen.expectedItemCount
      ),
      new BTExecutionMetrics(
        exec.scanTime, exec.scanWork, exec.queryCount, exec.rowCount,
        exec.succeeded, exec.limited, exec.overflowed,
        exec.compileTime, exec.cacheHits
      )
    )
  }

}
