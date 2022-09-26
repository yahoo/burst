/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy.store

import org.burstsys.alloy.store.mini.supervisor.MiniStoreSupervisor
import org.burstsys.alloy.store.mini.worker.MiniStoreWorker
import org.burstsys.brio.blob.BrioBlobEncoder
import org.burstsys.brio.press.{BrioPressSink, BrioPresser}
import org.burstsys.fabric.data.model.slice.data.FabricSliceData
import org.burstsys.fabric.data.model.store.FabricStoreProvider
import org.burstsys.fabric.metadata.model.domain.FabricDomain
import org.burstsys.fabric.metadata.model.view.FabricView
import org.burstsys.fabric.metadata.model.{FabricDomainKey, FabricViewKey}
import org.burstsys.tesla.buffer.mutable.TeslaMutableBuffer
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.instrument.prettyTimeFromNanos
import org.burstsys.vitals.logging._
import org.burstsys.{brio, tesla}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.language.implicitConversions

package object mini extends VitalsLogger {

  final val MiniStoreName = "mini"
  /**
   * mini store plugin provider
   */
  final case class MiniStoreProvider() extends FabricStoreProvider[MiniStoreSupervisor, MiniStoreWorker] {

    val storeName: String = mini.MiniStoreName

    val supervisorClass: Class[MiniStoreSupervisor] = classOf[MiniStoreSupervisor]

    val workerClass: Class[MiniStoreWorker] = classOf[MiniStoreWorker]

  }

  val sliceSet = new ArrayBuffer[FabricSliceData]

  val domainMap = new mutable.HashMap[FabricDomainKey, FabricDomain]

  val viewMap = new mutable.HashMap[FabricViewKey, MiniView]

  final
  def pressedViewCache(view: FabricView): Array[TeslaMutableBuffer] = {
    viewMap.get(view.viewKey) match {
      case None =>
        throw VitalsException(s"view '$view' not found")
      case Some(d) =>
        val start = System.nanoTime
        log info s"PRESS_VIEW '$view'"
        val blobs = for (item <- d.items) yield {
          val pressBuffer = tesla.buffer.factory.grabBuffer(1e6.toInt)
          val blobBuffer = tesla.buffer.factory.grabBuffer(1e6.toInt)
          val dictionary = brio.dictionary.factory.grabMutableDictionary()
          val sink = BrioPressSink(pressBuffer, dictionary)
          val presser = BrioPresser(d.schema, sink, d.presser(item))
          try {
            presser.press
            BrioBlobEncoder.encodeV2Blob(sink.buffer, d.rootVersion, sink.dictionary, blobBuffer)
            blobBuffer
          } finally {
            tesla.buffer.factory.releaseBuffer(pressBuffer)
            brio.dictionary.factory.releaseMutableDictionary(dictionary)
          }
        }
        log info burstStdMsg(s"PRESS_VIEW '$view' took ${prettyTimeFromNanos(System.nanoTime - start)}")
        blobs
    }
  }

}
