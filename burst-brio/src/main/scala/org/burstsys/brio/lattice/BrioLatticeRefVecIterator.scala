/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.brio.lattice

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.types.BrioTypes.{SizeOfCount, SizeOfOffset}
import org.burstsys.tesla.TeslaTypes._
import org.burstsys.tesla.buffer.TeslaBufferReader

/**
 * =Overview=
 * A lattice type that supports plural references to a collection of lattice references. Since this is a Scala Value
 * class and all the state is contained in a single off heap memory pointer, we need to pass the appropriate
 * [[org.burstsys.brio.model.schema.encoding.BrioSchematic]] metadata for whatever type is being looked at directly to the methods.
 * This metadata is available as part
 * of the traversal defined in  [[BrioSchema]]
 * =Memory Layout=
 * The in-memory structure that the 'ptr' points to is of the form:
 * {{{
 * Vector Header:
 *  ----------------------------
 * | Reference Vector Size      | SHORT
 *  ----------------------------
 *
 * And for each vector member:
 *  ----------------------------
 * | Reference Member Byte Size | SHORT
 *  ----------------------------
 * | Reference Member Contents  | ARRAY[BYTE]
 *  ----------------------------
 * }}}
 *
 */
final case
class BrioLatticeRefVecIterator(startOffset: TeslaMemoryOffset = TeslaNullOffset) extends AnyVal {

  /**
   * how many members
   *
   * @param reader
   * @return
   */
  @inline 
  def length(reader: TeslaBufferReader): Int = reader.readShort(startOffset)

  /**
   * initialize the vector index
   *
   * @param reader
   * @return
   */
  @inline 
  def start(reader: TeslaBufferReader): TeslaMemoryOffset = startOffset + SizeOfCount

  /**
   * Move past the current member
   *
   * @param offset
   * @param reader
   * @return
   */
  @inline 
  def advance(reader: TeslaBufferReader, offset: TeslaMemoryOffset): TeslaMemoryOffset = {
    var newPtr = offset
    val childByteSize = reader.readOffset(newPtr)
    newPtr += SizeOfOffset // skip over offset
    newPtr += childByteSize // skip over content
    newPtr
  }

  /**
   * grab the current member
   *
   * @param offset
   * @param reader
   * @return
   */
  @inline 
  def member(reader: TeslaBufferReader, offset: TeslaMemoryOffset): BrioLatticeReference = {
    BrioLatticeReference(offset + SizeOfOffset) // skip over offset
  }

}

