/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.flex

import java.util
import java.util.concurrent.ConcurrentHashMap

import org.burstsys.tesla.TeslaTypes.{TeslaMemoryPtr, TeslaMemorySize}
import org.burstsys.tesla.part.TeslaPartBuilder
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.instrument.{prettyByteSizeString, prettyFixedNumber}
import org.burstsys.vitals.uid._
import org.jctools.queues.MpmcArrayQueue

import scala.collection.mutable.ArrayBuffer
import scala.language.postfixOps

/**
 * this is a singleton that supports flex sizing management of a particular type of collector within a JVM
 *
 * <p/>'''NOTE 1:''' indexSlots might best be moved to off heap page aligned and page sized array (unproven)
 *
 * <p/>'''NOTE 2:''' this is intended to be subtyped by a package object
 *
 * @tparam Builder   the custom config object passed during instantiations
 * @tparam Collector the underlying collector object
 * @tparam Proxy     the resizeable wrapper object that mediates to the collector
 **/
abstract
class TeslaFlexCoupler[Builder <: TeslaPartBuilder, Collector <: TeslaFlexCollector[Builder, Collector], Proxy <: TeslaFlexProxy[Builder, Collector]]
  extends AnyRef {

  def collectorName: String

  /**
   * max slots for a give collector. This is forced to be a power of two.
   *
   * @return
   */
  def powersOf2SlotCount: Int

  // power of two aligned slot count
  private lazy val slotCount: Int = math.pow(2, powersOf2SlotCount).toInt

  // a way to schedule usage of the slots as dictionaries come and go
  final lazy val slotQueue: util.Queue[TeslaFlexSlotIndex] = new MpmcArrayQueue[TeslaFlexSlotIndex](slotCount) {

    // initialize by placing all slot indexes into queue
    indexSlots.indices foreach {
      s =>
        indexSlots(s) = emptySlotValue
        this.add(s)
    }
  }

  /**
   * an indexed array of off heap memory pointers for underlying fixed size dictionaries
   */
  private
  lazy val indexSlots = new Array[TeslaMemoryPtr](slotCount)

  //////////////////////////////////////////////////////////////////////////////////////////////
  // subtype API
  //////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * given a memory pointer looked up in the slot table, return a Collector
   * <br/>'''NOTE:''' the assumption here is that this call does not create new objects i.e. the collector
   * is a value class wrapping a memory ptr.
   *
   * @param ptr
   * @return
   */
  def instantiateCollector(ptr: TeslaMemoryPtr): Collector

  /**
   * create a proxy at a given slot index
   * <br/>'''NOTE:''' the assumption here is that this call does not create new objects i.e. the proxy
   * is a value class wrapping a memory ptr.
   * <hr/>
   * TODO THIS DOES NOT SEEM TO BE HOLDING TRUE -- THIS MUST BE FIXED!!! DIVE INTO THE BYTE CODE AND FIGURE
   * TODO OUT HOW TO STOP OBJECT INSTANTIATION
   *<hr/>
   * @param index
   * @return
   */
  def instantiateProxy(index: TeslaFlexSlotIndex): Proxy

  /**
   * allocate a new internal collector of a given size
   *
   * @param size
   * @return
   */
  def allocateInternalCollector(builder: Builder, size: TeslaMemorySize): Collector

  /**
   * allocate as internal collector
   *
   * @param collector
   */
  def releaseInternalCollector(collector: Collector): Unit

  //////////////////////////////////////////////////////////////////////////////////////////////
  // Implementation
  //////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * lookup the underlying fixed size dictionary and return
   *
   * @param index
   * @return
   */
  final
  def lookupInternalCollector(index: TeslaFlexSlotIndex): Collector = {
    if (index == emptySlotIndex)
      throw VitalsException(s"lookupInternalCollector null index")
    assert(collectorName != null)
    if (index >= slotCount)
      throw VitalsException(s"lookupInternalCollector $collectorName $index > $slotCount")
    val ptr = indexSlots(index)
    if (ptr == emptySlotValue)
      throw VitalsException(s"lookupInternalCollector $collectorName $index is empty")
    instantiateCollector(ptr)
  }

  /**
   * replace the existing too small internal fixed size offheap collector with a new upsized fixed size offheap collector
   *
   * @param index
   * @param items
   */
  final
  def upsize(index: TeslaFlexSlotIndex, items: Int, builder: Builder): Unit = {
    val oldInternal = lookupInternalCollector(index)
    val oldSize = oldInternal.currentMemorySize
    try {
      val newSize = (oldInternal.currentMemorySize * builder.multiplier).toInt
      val newInternal = allocateInternalCollector(builder, newSize)
      val oldPtr = oldInternal.basePtr
      val newPtr = newInternal.basePtr
      log debug s"RESIZE '$collectorName' items=$items  oldPtr=$oldPtr oldSize=$oldSize newPtr=$newPtr newSize=$newSize"
      newInternal.importCollector(oldInternal, items, builder)
      indexSlots(index) = newInternal.blockPtr
    } finally {
      releaseInternalCollector(oldInternal)
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////
  // STANDARD API
  //////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * grab a flex collector proxy
   *
   * @param builder
   * @return
   */
  final
  def grabFlexCollectorProxy(builder: Builder, startSize: TeslaMemorySize): Proxy = {
    slotQueue poll match {
      case null =>
        throw VitalsException(s"$collectorName collector ran out of flex slots!")
      case index =>
        indexSlots(index) = allocateInternalCollector(builder, startSize).blockPtr
        instantiateProxy(index)
    }
  }

  /**
   * free a flex collector
   *
   */
  final
  def freeFlexCollectorProxy(proxy: Proxy): Unit = {
    val collector = lookupInternalCollector(proxy.index)
    releaseInternalCollector(collector)
    indexSlots(proxy.index) = emptySlotValue
    slotQueue add proxy.index
  }

}
