/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.runtime

import org.burstsys.felt.kryo.FeltKryoSerializable
import org.burstsys.felt.model.visits.decl.FeltStaticVisitDecl
import org.burstsys.tesla.TeslaTypes.TeslaMemorySize
import org.burstsys.tesla.pool.TeslaPooledResource

/**
 * a collector is a type defined in FELT primitives that is used at scan runtime to collect information that make up the
 * semantics of an analysis scan.
 * <hr/>
 * ==Collector Aspects==
 * <ol>
 * <li>'''METADATA''': a set of related ''immutable'' types that make up the semantics of a specific collector and its purpose in a specific analysis scan.
 * Things likes ''schema'', ''builders'', ''declarations''...
 * <li>'''DATA''': a set of related ''mutable'' types related to the collection of data during a scan. Data ends up somewhere in
 * the result semantics of the analysis.
 * <li>'''RUNTIME''': a set of related ''mutable'' runtime support types related to a single scan.  This includes things that
 * control tree positioning, or exection handling etc
 * </li>
 * </ol>
 * <hr/>
 *
 * ==Collectors Semantics==
 * <ol>
 * <li> There are two ways of mutating (writing/updated) state in a Felt scan 1) Collectors 2) Mutables. They are similar
 * in some ways but only collectors can be 'visited' i.e. the results of the writes to a collector can be merged or joined
 * into another collector. </li>
 * </ol>
 * <li> '''READING:''' generally speaking there is only one way of reading a collector and that is during a '''visit''' of its collected
 * information. This is an important distinguishing characteristic from a [[org.burstsys.felt.model.mutables.FeltMutable]]</li>
 * <li> '''WRITE:''' generally speaking collectors are written into during any [[FeltStaticVisitDecl]] action</li>
 * </ol>
 * <hr/>
 */
trait FeltCollector extends Any with TeslaPooledResource with FeltKryoSerializable {

  /**
   * return the number of rows in the cube
   *
   * @return
   */
  def itemCount: Int

  /**
   * set the number of rows in the cube
   *
   * @param count
   */
  def itemCount_=(count: Int): Unit

  def size(): TeslaMemorySize = 0

  /**
   * true is a fixed row limit was exceeded
   *
   * @return
   */
  def itemLimited: Boolean

  def itemLimited_=(s: Boolean): Unit

  /**
   * empty out the collector
   */
  def clear(): Unit

  /**
   *
   * @return
   */
  def isEmpty: Boolean

  /**
   *
   * @return
   */
  final def notEmpty: Boolean = !isEmpty

  def validate(): Boolean = true

}
