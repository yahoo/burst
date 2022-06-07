/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.types

import org.burstsys.brio.types.BrioCourse.BrioCourseId
import org.burstsys.brio.types.BrioPrimitives._
import org.burstsys.vitals.errors.VitalsException

import scala.language.implicitConversions

/**
  *
  */
trait BrioCourse[T <: BrioCourse[T]] extends Any {
  def data: BrioPrimitive

  def maxOrdinal: Int

  def maxId: BrioCourseId

  def bits: Long

  def mask: Long

  final
  def isEmpty: Boolean = data == 0L

  def mergeStep(ordinal: Int, id: BrioCourseId): T

  @inline final
  def mergeData(ordinal: BrioCourseId, id: BrioCourseId): Long = {
    validateStep(ordinal, id)
    val idValue: Long = id & mask
    val shiftBits: Long = ordinal * bits
    val shiftedValue: Long = idValue << shiftBits
    data | shiftedValue
  }

  final
  def validateStep(ordinal: Int, id: BrioCourseId): Unit = {
    if (ordinal < 0)
      throw VitalsException(s" BrioCourse negative ordinal")
    if (id < 0)
      throw VitalsException(s" BrioCourse negative id")
    if (ordinal > maxOrdinal)
      throw VitalsException(s" BrioCourse ordinal '$ordinal' greater than $maxOrdinal")
    if (id > maxId)
      throw VitalsException(s" BrioCourse id '$id' greater than $maxId")
  }

  final override
  def toString: String = {
    val b = new StringBuilder
    var i = 0
    while (i <= maxOrdinal) {
      val d: Long = (data >> (i * bits)) & mask
      if (d != 0)
        b ++= s"$d-"
      i += 1
    }
    b.toString().stripSuffix("-")
  }

}

object BrioCourse {

  type BrioCourseId = Int

  final val BrioRouteNoCourse: BrioCourseId = -1

  implicit def longToRouteCourse32(l: Long): BrioCourse32 = BrioCourse32(l)

  implicit def longToRouteCourse16(l: Long): BrioCourse16 = BrioCourse16(l)

  implicit def longToRouteCourse8(l: Long): BrioCourse8 = BrioCourse8(l)

  implicit def longToRouteCourse4(l: Long): BrioCourse4 = BrioCourse4(l)

  /**
    * Course 4 has 4 steps of 16 bits each. ids 1->65,535 with id=0 meaning no step
    *
    * @param data
    */
  final case
  class BrioCourse4(data: BrioPrimitive = 0L) extends AnyVal with BrioCourse[BrioCourse4] {

    @inline override
    def mergeStep(ordinal: Int, id: BrioCourseId): BrioCourse4 =
      BrioCourse4(mergeData(ordinal, id))

    @inline override
    def bits: Long = 16

    @inline override
    def mask: Long = 0xFFFF

    @inline override
    def maxOrdinal: BrioCourseId = 3

    @inline override
    def maxId: BrioCourseId = 65535

  }

  /**
    * Course 8 has 8 steps of 8 bits each. ids 1->255 with id=0 meaning no step
    *
    * @param data
    */
  final case
  class BrioCourse8(data: BrioPrimitive = 0L) extends AnyVal with BrioCourse[BrioCourse8] {

    @inline override
    def mergeStep(ordinal: Int, id: BrioCourseId): BrioCourse8 =
      BrioCourse8(mergeData(ordinal, id))

    @inline override
    def bits: Long = 8

    @inline override
    def mask: Long = 0xFF

    @inline override
    def maxOrdinal: BrioCourseId = 7

    @inline override
    def maxId: BrioCourseId = 255

  }

  /**
    * Course 16 has 16 steps of 4 bits each. ids 1->15 with id=0 meaning no step
    *
    * @param data
    */
  final case
  class BrioCourse16(data: BrioPrimitive = 0L) extends AnyVal with BrioCourse[BrioCourse16] {

    @inline override
    def mergeStep(ordinal: Int, id: BrioCourseId): BrioCourse16 =
      BrioCourse16(mergeData(ordinal, id))

    @inline override
    def bits: Long = 4

    @inline override
    def mask: Long = 0xF

    @inline override
    def maxOrdinal: BrioCourseId = 15

    @inline override
    def maxId: BrioCourseId = 15

  }

  /**
    * Course 32 has 32 steps of two bits each. ids 1->3 with id=0 meaning no step
    *
    * @param data
    */
  final case
  class BrioCourse32(data: BrioPrimitive = 0L) extends AnyVal with BrioCourse[BrioCourse32] {

    @inline override
    def mergeStep(ordinal: Int, id: BrioCourseId): BrioCourse32 =
      BrioCourse32(mergeData(ordinal, id))

    @inline override
    def bits: Long = 2

    @inline override
    def mask: Long = 0x3

    @inline override
    def maxOrdinal: BrioCourseId = 31

    @inline override
    def maxId: BrioCourseId = 3

  }

}

/** The Brio Course builder is used to follow a sequence of steps and support
  * finding courses to construct.
  * ==Different Types of Courses==
  * {{{
  *   Type 1. Courses that have a start id but no end id. These start when a start id is seen and
  *   end when max ordinal is reached (or when the scan stops?)
  *   Type 2. Courses that have an end id but no start id. These start right away and end when the end is reached
  *   (or when the scan stops?)
  *   Type 3. Courses that have both a start id and an end id. These start when a start id is seen and end when the
  *   end id is seen
  * }}}
  * ==Semantic Rules==
  * Where A is a start point and C is an end point in a 6 step sequence.
  * {{{
  *   1. We keep track of multiple start points to handle 'overlapping' sequences like AABBC
  *   e.g. AABBC would produce AABBC and ABBC as output.
  *   2. We keep track of multiple end points for multiple sequences that can fit into a single sequence
  *   e.g. ABCADC would produce ABCADC, ABC, and ADC.
  *   3. How about when we have both say as in AABCBC. We would create AABC, ABC, AABC, AABCBC, ABCBC.
  *   2. Sequences start where we see one of a set of `start` ids
  *   3. Sequences end where we reach one of a set of `end` ids, or if no `end` ids are specified, when a sequence
  *   reaches `maxOrdinal`
  * }}}
  */
final case
class BrioCourseBuilder(maxOrdinal: Int, startIds: Array[BrioCourseId], endIds: Array[BrioCourseId]) {

  /**
    * a circular buffer for course ids
    */
  val circularBuffer: Array[BrioCourseId] = new Array[BrioCourseId](64)

  /**
    * begin pt in the circular buffer (wraps around)
    */
  var beginCursor: Int = 0

  /**
    * end pt in the circular buffer (wraps around)
    */
  var endCursor: Int = 0

  /**
    * bit map for begin markers in the circular buffer
    */
  var beginMarkers: Long = 0L

  /**
    * bit map for end markers in the circular buffer
    */
  var endMarkers: Long = 0L

  /**
    * Add a step to buffer, possibly returning a course.
    *
    * @param id the courseId to record
    * @param isBegin
    * @param isEnd
    * @return return true if there are courses that can be produced.
    */
  def record(id: BrioCourseId, isBegin: Boolean = false, isEnd: Boolean = false): Boolean = {
    ???
  }

  /**
    *
    * @return
    */
  def produce: Long = {
    ???
  }
}
