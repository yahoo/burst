/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.http.service

import java.util.concurrent.atomic.{AtomicInteger, AtomicLong, LongAdder}

import org.burstsys.fabric.wave.metadata.model.over.FabricOver
import org.burstsys.vitals.logging._

package object profiler extends VitalsLogger {
  /**
   * A unit of work for the profiler
   *
   * @param source            the query to profile
   * @param desiredQueryCount the number of queries to run
   * @param concurrentQueries how many queries can run concurrently
   * @param queriesPerLoad    how many queries should be executed before forcing a load of the dataset
   * @param over              the domain/view to load
   */
  final case class ProfilerRun(
                                source: String,
                                desiredQueryCount: Int,
                                concurrentQueries: Int,
                                queriesPerLoad: Int,
                                over: FabricOver
                              ) {
    /**
     * The number of queries that have been started
     */
    val queriesTally: AtomicInteger = new AtomicInteger(0)
    /**
     * The number of queries that completed successfully
     */
    val successesTally: AtomicInteger = new AtomicInteger(0)
    /**
     * The number of queries that failed
     */
    val failuresTally: AtomicInteger = new AtomicInteger(0)
    /**
     * The generation clock for the last load
     */
    val lastGenerationClock: AtomicLong = new AtomicLong(0)
    /**
     * The number of scans that were run
     */
    val scanCountTally: LongAdder = new LongAdder()
    /**
     * The total amount of wall clock time that elapsed while the queries were running
     */
    val scanTimeTally: LongAdder = new LongAdder()
    /**
     * The total amount of time took loading data
     */
    val loadTimeTally: LongAdder = new LongAdder()
    /**
     * The total number of loads that occurred
     */
    val loadCountTally: AtomicInteger = new AtomicInteger(0)
    /**
     * The total number of bytes loaded
     */
    val loadSizeTally: LongAdder = new LongAdder()

  }

}
