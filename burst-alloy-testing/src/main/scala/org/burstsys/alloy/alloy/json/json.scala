/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy.alloy

import org.burstsys.alloy.alloy.store.worker.bufferSize
import org.burstsys.alloy.alloy.store.worker.blobSize
import org.burstsys.alloy.alloy.store.worker.log
import org.burstsys.brio.blob.BrioBlobEncoder
import org.burstsys.brio.json.JsonPressSource
import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.press.BrioPressSink
import org.burstsys.brio.press.BrioPresser
import org.burstsys.tesla.buffer.mutable.TeslaMutableBuffer
import org.burstsys.vitals.errors.safely
import org.burstsys.brio
import org.burstsys.tesla

import java.io.FileInputStream
import java.io.InputStream
import java.nio.file.Paths
import java.util.zip.GZIPInputStream

package object json {

  final
  def loadFromJson(fName: String, schemaName: String, rootVersion: Int): Iterator[TeslaMutableBuffer] = {
    assert(fName != null)
    val jsonSource: InputStream = {
      val fileName = Paths.get(fName)
      if (fileName.getFileName.toString.endsWith(".gz"))
        new GZIPInputStream(new FileInputStream(fileName.toFile))
      else
        new FileInputStream(fileName.toFile)
    }
    val schema = BrioSchema(schemaName)

    /* wrap the json interator with one that presses to a buffer */
    new Iterator[TeslaMutableBuffer]() {
      private val jsonIterator = brio.json.getJsonSource(schema, jsonSource)
      private var itemCount = 0

      override def hasNext: Boolean = jsonIterator.hasNext

      override def next(): TeslaMutableBuffer = {
        itemCount += 1
        val item = jsonIterator.next()
        val pressBuffer = tesla.buffer.factory.grabBuffer(bufferSize)
        val blobBuffer = tesla.buffer.factory.grabBuffer(blobSize)
        val dictionary = brio.dictionary.factory.grabMutableDictionary()
        val sink = BrioPressSink(pressBuffer, dictionary)

        val presser = BrioPresser(schema, sink, JsonPressSource(schema, item))
        try {
          presser.press
          BrioBlobEncoder.encodeV2Blob(sink.buffer, rootVersion, sink.dictionary, blobBuffer)
          blobBuffer
        } catch safely {
          case e: Exception =>
            log error(s"failed to press item $itemCount", e)
            tesla.buffer.factory.releaseBuffer(blobBuffer)
            null
        } finally {
          tesla.buffer.factory.releaseBuffer(pressBuffer)
          brio.dictionary.factory.releaseMutableDictionary(dictionary)
        }
      }
    }
  }

}
