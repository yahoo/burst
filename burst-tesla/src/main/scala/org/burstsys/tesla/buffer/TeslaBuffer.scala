/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.buffer

import org.burstsys.tesla.TeslaTypes.{TeslaMemoryPtr, TeslaMemorySize}


/**
  * The front end to all manipulations of __memory buffers__. These are backed by a chunk of off heap memory
  * from the TeslaBlock library. We support a set of read [[TeslaBufferReader]], and write [[TeslaBufferWriter]] accessors,
  * and some bulk operations.
  */
trait TeslaBuffer extends Any {

  /**
    * bounds checking
    *
    * @param ptr
    * @return
    */
  def checkPtr(ptr: TeslaMemoryPtr): TeslaMemoryPtr

  /**
    * location of the start of the contained data in the buffer
    *
    * @return
    */
  def dataPtr: TeslaMemoryPtr

  /**
    * The capacity of this buffer (bytes available to be used)
    *
    * @return
    */
  def maxAvailableMemory: TeslaMemorySize

  /**
    * the location of the end of the used part of the buffer (bytes actually used)
    * This does not include header info so its not the actual size
    *
    * @return
    */
  def currentUsedMemory: TeslaMemorySize


}
