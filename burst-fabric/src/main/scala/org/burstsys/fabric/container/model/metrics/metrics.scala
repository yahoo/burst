/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.model

import java.util.concurrent.atomic.{AtomicInteger, AtomicLong, DoubleAdder, LongAdder}

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import org.burstsys.vitals.logging._

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * The execution & data model for the metrics plane in the fabric.
 * These work for Kryo and JSON
 */

package object metrics extends VitalsLogger {

  /**
   * given a set of random samples, return a unit rate per time quantum (default is 5 minutes)
   *
   * @param timeQuantum
   */
  private[fabric] final case
  class FabricRateMeter(timeQuantum: Duration = 5 minutes) {

    def sample(amount: Int): this.type = {
      this
    }
  }

  private[fabric] final case
  class FabricMetricTuple(var value: Double = 0, var time: Long = -1) extends KryoSerializable {
    override
    def read(kryo: Kryo, input: Input): Unit = {
      time = input.readLong
      value = input.readDouble
    }

    override
    def write(kryo: Kryo, output: Output): Unit = {
      output writeLong time
      output writeDouble value
    }
  }

  private[fabric] final case
  class FabricLastHourMetric(var history: Array[FabricMetricTuple] = Array.empty) extends KryoSerializable {
    var average: FabricMetricTuple = calcAverage

    override def read(kryo: Kryo, input: Input): Unit = {
      history = kryo.readClassAndObject(input).asInstanceOf[Array[FabricMetricTuple]]
      average = calcAverage
    }

    override def write(kryo: Kryo, output: Output): Unit = kryo.writeClassAndObject(output, history)

    private def calcAverage = {
      if (history.isEmpty) {
        FabricMetricTuple()
      } else
        FabricMetricTuple(history.toList.foldLeft(0.0)(_ + _.value) / history.length, history.last.time)
    }
  }

  /**
   * thread safe metrics collector
   * <p/>
   * '''TODO''' this is throwing 'Tuple3(new DoubleAdder, new LongAdder, new AtomicLong)' like crazy MUST BE REWRITTEN
   *
   * @param buckets     how many time quantum buckets do we manage (default is 12)
   * @param timeQuantum how long is a time quantum bucket (default is 5 minutes)
   *
   */
  private[fabric] final case
  class FabricLastHourMetricCollector(buckets: Int = 12, timeQuantum: Duration = 5 minutes) {

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // private state
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    // fixed size circular buffer of collections of samples within a time quantum bucket
    private[this]
    val quantumBuckets = new Array[(DoubleAdder, LongAdder, AtomicLong)](buckets)

    private[this]
    var beginIndex = new AtomicInteger(0)

    private[this]
    var endIndex = new AtomicInteger(0)

    private[this]
    var size: Int = 0

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // API
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * initialize state
     *
     * @return
     */
    def initialize: this.type = {
      quantumBuckets.indices.foreach(quantumBuckets(_) = (new DoubleAdder, new LongAdder, new AtomicLong))
      this
    }

    /**
     * export the readings to an external format
     *
     * @return
     */
    def exportMetric: FabricLastHourMetric = {
      val collection = new ArrayBuffer[FabricMetricTuple]
      var i = 0
      var index = beginIndex.get
      while (i < size) {
        val quantumBucket = quantumBuckets(index)
        // average value over the time quantum with time marker (time, sum_of_all_values / count_of_values)
        collection += FabricMetricTuple(
          quantumBucket._1.doubleValue() / quantumBucket._2.doubleValue(), quantumBucket._3.get()
        )
        // calc index, wrap around if necessary
        index += 1
        if (index > (buckets - 1)) {
          index = 0
        }
        i += 1
      }
      FabricLastHourMetric(collection.toArray)
    }

    /**
     * Record a new value
     *
     * @param value
     * @param nowTime generally this should be 'now' but for unit tests we provide for faking...
     * @return
     */
    def sample(value: Double, nowTime: Long = System.currentTimeMillis): this.type = {
      // TODO this is throwing Tuple3(new DoubleAdder, new LongAdder, new AtomicLong) like crazy must be rewritten
      return this
      synchronized {
        // this may be our first sample evah
        if (size == 0) {
          size = 1
          beginIndex.set(0)
          endIndex.set(0)
          val currentBucket = quantumBuckets(endIndex.get)
          currentBucket._1 add value
          currentBucket._2.increment()
          currentBucket._3 set nowTime
          return this
        }
        /**
         * ok its not going to be that easy... start to roll buckets forward (we may have a gap of samples greater
         * than timeQuantum)
         */
        var continue = true
        while (continue) {
          val currentBucket = quantumBuckets(endIndex.get)
          // check to see if we are in the right bucket
          if (nowTime < currentBucket._3.get + timeQuantum.toMillis) {
            currentBucket._1 add value
            currentBucket._2.increment()
            continue = false
          } else {
            // move bucket forward one
            endIndex.getAndIncrement
            // we may need to wrap our bucket circular buffer forward
            if (endIndex.get > (buckets - 1)) {
              endIndex.set(0)
            }
            if (size == buckets) {
              // move bucket forward one
              beginIndex.getAndIncrement
              // we may need to wrap our bucket circular buffer forward
              if (beginIndex.get > (buckets - 1)) {
                beginIndex.set(0)
              }
            }
            // initialize this new or renewed bucket
            val nextBucket = quantumBuckets(endIndex.get)
            nextBucket._1.reset()
            nextBucket._2.reset()
            nextBucket._3.set(0)

            nextBucket._1 add value
            nextBucket._2.increment()
            nextBucket._3 set currentBucket._3.get + timeQuantum.toMillis
            continue = false

            // if not move forward one bucket and time quantum
            if (size != buckets) size += 1 // can't get over max size
          }
        }
      }
      this
    }

  }

}
