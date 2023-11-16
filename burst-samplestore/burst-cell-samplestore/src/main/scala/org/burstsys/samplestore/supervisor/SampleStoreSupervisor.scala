/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.samplestore.supervisor

import org.burstsys.api._
import org.burstsys.fabric.topology.model.node.worker.FabricWorkerNode
import org.burstsys.fabric.wave.container.supervisor.FabricWaveSupervisorContainer
import org.burstsys.fabric.wave.data.model.slice.FabricSlice
import org.burstsys.fabric.wave.data.model.store._
import org.burstsys.fabric.wave.data.supervisor.store._
import org.burstsys.fabric.wave.metadata.model.datasource.FabricDatasource
import org.burstsys.samplestore.SampleStoreName
import org.burstsys.samplestore.api.BurstSampleStoreApiRequestState._
import org.burstsys.samplestore.api._
import org.burstsys.samplestore.api.client.{SampleStoreApiClient, defaultHostName, defaultPort}
import org.burstsys.samplestore.api.configuration.{burstSampleStoreApiHostProperty, burstSampleStoreApiPortProperty, burstSampleStoreApiSslEnableProperty}
import org.burstsys.samplestore.model.{SampleStoreLocus, SampleStoreSlice, _}
import org.burstsys.samplestore.trek.SampleStoreGetViewGeneratorTrek
import org.burstsys.tesla.thread.request._
import org.burstsys.vitals.errors.VitalsException
import org.burstsys.vitals.healthcheck._
import org.burstsys.vitals.logging.burstStdMsg
import org.burstsys.vitals.net.{VitalsHostName, VitalsHostPort}
import org.burstsys.vitals.reporter.instrument.prettyTimeFromNanos
import org.burstsys.vitals.uid.VitalsUid

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import scala.annotation.unused
import scala.collection.mutable
import scala.concurrent.Future
import scala.language.implicitConversions

final case
class SampleStoreSupervisor(container: FabricWaveSupervisorContainer) extends FabricStoreSupervisor with VitalsHealthMonitoredService {

  override val storeName: FabricStoreName = SampleStoreName

  ///////////////////////////////////////////////////////////////////
  // STATE
  ///////////////////////////////////////////////////////////////////

  private val _clients = new ConcurrentHashMap[(VitalsHostName, VitalsHostPort), SampleStoreApiClient]()

  ///////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////

  private def apiClient(datasource: FabricDatasource): SampleStoreApiClient = {
    sampleStoreServerLock synchronized {
      val storeProperties = datasource.view.storeProperties
      (storeProperties.get(sampleStoreHostName), storeProperties.get(sampleStoreHostPort).map(_.toInt)) match {
        case (Some(hostName), None) =>
          log debug s"view overrides samplesource host $hostName"
          cacheClient(hostName, defaultPort)
        case (None, Some(hostPort)) =>
          log debug s"view overrides samplesource port $hostPort"
          cacheClient(defaultHostName, hostPort)
        case (Some(hostname), Some(hostPort)) =>
          log debug s"view overrides host and port $hostname:$hostPort"
          cacheClient(hostname, hostPort)
        case (None, None) =>
          cacheClient(defaultHostName, defaultPort)
      }
    }
  }

  private def cacheClient(hostName: VitalsHostName, hostPort: VitalsHostPort): SampleStoreApiClient = {
    val c = _clients.computeIfAbsent((hostName, hostPort), _ => SampleStoreApiClient(hostName, hostPort).start)
    if (c.created < System.currentTimeMillis() - 60000) {
      c.stop
      _clients.computeIfAbsent((hostName, hostPort), _ => SampleStoreApiClient(hostName, hostPort).start)
    } else {
      c
    }
  }

  ///////////////////////////////////////////////////////////////////
  // LIFECYCLE
  ///////////////////////////////////////////////////////////////////

  override
  def start: this.type = {
    synchronized {
      ensureNotRunning
      log info startingMessage
    }
    markRunning
    this
  }

  override
  def stop: this.type = {
    synchronized {
      ensureRunning
      log info stoppingMessage
      _clients.forEach((_, client) => client.stop)
    }
    markNotRunning
    this
  }

  ///////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////

  override def slices(guid: VitalsUid, workers: Array[FabricWorkerNode], datasource: FabricDatasource): Future[Array[FabricSlice]] = {
    val start = System.nanoTime

    SampleStoreGetViewGeneratorTrek.begin(guid) { st =>
      val tag = s"SampleStoreMaster.slices(guid=$guid, datasource=$datasource, traceId=${st.getTraceId})"
      twitterFutureToScalaFuture(apiClient(datasource).getViewGenerator(guid, datasource)) map { response =>
        response.context.state match {
          case BurstSampleStoreApiRequestSuccess if response.loci.isEmpty || response.loci.get.isEmpty =>
            throw VitalsException("Got no loci from sample store supervisor")

          case BurstSampleStoreApiRequestSuccess =>
            val loci = response.loci.get.map(SampleStoreDataLocus(_)).toArray
            SampleStoreGeneration(guid, response.generationHash, loci, datasource.view.schemaName, response.motifFilter)

          case BurstSampleStoreApiRequestTimeout |
               BurstSampleStoreApiRequestException |
               BurstSampleStoreApiRequestInvalid |
               BurstSampleStoreApiNotReady =>
            throw  VitalsException(s"Got ${response.context.state} from samplestore master")

          case r =>
            throw VitalsException(s"Got unrecognized $r from samplestore master")
        }
      } recover {
        case t =>
          val e =  VitalsException(s"Failed to get view generation guid=$guid datasource=$datasource", t)
          SampleStoreGetViewGeneratorTrek.fail(st, e)
          throw e
      } map { generator =>
        SampleStoreGetViewGeneratorTrek.end(st)
        val workerMap = new mutable.HashMap[FabricWorkerNode, mutable.ArrayBuffer[SampleStoreLocus]]
        val i = new AtomicInteger
        // spread the loci across the workers
        generator.loci foreach { locus =>
          val worker = workers(i.getAndIncrement() % workers.length)
          workerMap.getOrElseUpdate(worker, new mutable.ArrayBuffer[SampleStoreLocus]) += locus
        }
        val motifFilter = generator.motifFilter.getOrElse(throw VitalsException(s"$tag no motif filter!"))
        val sliceCount = workerMap.size
        log info s"SAMPLE_STORE_SLICE_GOT_WORKERS workers=${workerMap.size} $tag"

        // mapping the slices to workers using nodeId as sliceKey.
        // Hey, nobody said keys had to fall in (0..slices) and as far as I can tell it's only used to build the region file path
        val slices = workerMap.keys.map { worker =>
          SampleStoreSlice(
            guid, sliceKey = worker.nodeId.toInt, generator.generationHash, sliceCount,
            datasource, motifFilter, worker, workerMap(worker).toArray)
        }.toArray[FabricSlice]
        val elapsedNanos = System.nanoTime - start
        log info s"SAMPLE_STORE_SLICE_SUCCESS elapsedTime=$elapsedNanos (${prettyTimeFromNanos(elapsedNanos)}), slices=${slices.length} $tag"
        slices
      } recover {
        case t =>
          log error burstStdMsg(s"SAMPLE_STORE_SLICE_FAIL $tag", t)
          throw t
      }
    }
  }

  burstSampleStoreApiSslEnableProperty.listeners += watchHostProperty
  burstSampleStoreApiPortProperty.listeners += watchHostProperty
  burstSampleStoreApiHostProperty.listeners += watchHostProperty

  private def watchHostProperty(@unused v: Option[_]): Unit = {
    log info burstStdMsg(s"detected to sample store properties, restarting clients")
    sampleStoreServerLock synchronized {
      _clients.clear()
    }
  }
}
