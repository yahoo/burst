/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.wave.data.model.slice.region

import org.burstsys.brio.blob.BrioBlob.BrioRegionIterator
import org.burstsys.brio.blob.{BrioBlob, BrioBlockAnyVal, BrioStaticBlobAnyVal}
import org.burstsys.fabric.wave.data.model.slice.region.reader.{FabricRegionReader, FabricRegionReaderContext}
import org.burstsys.tesla.TeslaTypes.{TeslaMemoryOffset, TeslaMemorySize}
import org.burstsys.vitals.errors.VitalsException

/**
 * access to the data in a region
 */
trait FabricRegionAccessor extends FabricRegionReader {

  self: FabricRegionReaderContext =>

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def iterator: BrioRegionIterator = {
    lazy val tag = s"FabricRegionAccessor.iterator($parameters)"
    new BrioRegionIterator {

      private var currentBlock = BrioBlockAnyVal(readMemoryPtr + SizeOfRegionHeader)

      // don't read past the EOF
      val eof: Long = readFileSize

      // we start right after the header for the region file.
      var currentFileLocation: TeslaMemoryOffset = SizeOfRegionHeader

      final override def hasNext: Boolean = currentFileLocation < eof

      final override def next(): BrioBlob = {

        val blob = BrioStaticBlobAnyVal(currentBlock.itemStartPtr)

        // make sure we don't go over EOF
        currentFileLocation += currentBlock.totalBlockSize

        // pick up next block - if we are EOF we will ignore it
        currentBlock = currentBlock.nextBlock

        blob
      }
    }
  }

}
