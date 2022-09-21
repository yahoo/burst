/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.torquemada.test

import org.apache.logging.log4j.Logger
import org.burstsys.agent.AgentLanguage
import org.burstsys.agent.AgentService
import org.burstsys.brio.types.BrioTypes._
import org.burstsys.catalog.CatalogService
import org.burstsys.catalog.CatalogService.CatalogUnitTestClientConfig
import org.burstsys.catalog.CatalogService.CatalogUnitTestServerConfig
import org.burstsys.fabric.data.model.generation.FabricGeneration
import org.burstsys.fabric.data.model.generation.key.FabricGenerationKey
import org.burstsys.fabric.data.model.ops._
import org.burstsys.fabric.data.model.slice.metadata.FabricSliceMetadata
import org.burstsys.fabric.execution.model.execute.group.FabricGroupUid
import org.burstsys.fabric.execution.model.execute.parameters.FabricCall
import org.burstsys.fabric.execution.model.result.FabricExecuteResult
import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricAggregationCell
import org.burstsys.fabric.execution.model.result.row.FabricResultCell
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.fabric.execution.model.result.set.FabricResultSet
import org.burstsys.fabric.metadata.model.over.FabricOver
import org.burstsys.tesla.thread.request.TeslaRequestFuture
import org.burstsys.vitals.VitalsService.VitalsStandaloneServer
import org.burstsys.vitals.logging.VitalsLog
import org.burstsys.vitals.uid._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.util.concurrent.atomic.AtomicLong
import scala.concurrent.Future

abstract class TorquemadaHelper extends AnyFlatSpec
  with TorquemadaSpecLog with Matchers with BeforeAndAfterAll with AgentLanguage with FabricCacheOps {

  override def languagePrefixes: Array[String] = Array("select")

  val agentClient: AgentService = AgentService()
  val agentService: AgentService = AgentService(VitalsStandaloneServer)

  var catalogClient: CatalogService = CatalogService(CatalogUnitTestClientConfig)
  var catalogServer: CatalogService = CatalogService(CatalogUnitTestServerConfig)

  override protected
  def beforeAll(): Unit = {
    catalogServer.start
    catalogClient.start
    agentService.start
    agentService.registerLanguage(this)
    agentService.registerCache(this)
    agentClient.start
  }

  override protected
  def afterAll(): Unit = {
    agentService.stop
    agentClient.stop
    catalogClient.stop
    catalogServer.stop
  }

  override def log: Logger = VitalsLog.getJavaLogger(classOf[TorquemadaHelper])

  val columnNames: Array[String] = Array("Users", "Sessions", "Events", "count")
  val columnTypes: Array[BrioTypeKey] = Array(BrioLongKey, BrioLongKey, BrioLongKey, BrioLongKey)
  val columnTypeNames: Array[BrioTypeName] = Array(BrioLongName, BrioLongName, BrioLongName, BrioLongName)

  var evictCount = new AtomicLong(0L)
  var flushCount = new AtomicLong(0L)
  var queryCount = new AtomicLong(0L)

  def resetTestCounters(): Unit = {
    evictCount.set(0L)
    flushCount.set(0L)
    queryCount.set(0L)
  }

  override def executeGroupAsWave(groupUid: FabricGroupUid, source: String, over: FabricOver, call: Option[FabricCall]): Future[FabricExecuteResult] = {
    TeslaRequestFuture {
      queryCount.incrementAndGet()

      val mockRows = Array(
        FabricResultRow(
          cells = Array(
            FabricResultCell.asLong(isNull = false, isNan = false, value = 100, cellType = FabricAggregationCell),
            FabricResultCell.asLong(isNull = false, isNan = false, value = 200, cellType = FabricAggregationCell),
            FabricResultCell.asLong(isNull = false, isNan = false, value = 300, cellType = FabricAggregationCell),
            FabricResultCell.asLong(isNull = false, isNan = false, value = 1, cellType = FabricAggregationCell)
          )
        )
      )

      val resultGroup = FabricResultGroup(resultSets = Map(0 -> FabricResultSet(rowSet = mockRows)))
      FabricExecuteResult(resultGroup)
    }
  }

  final override
  def cacheGenerationOp(groupUid: VitalsUid,
                     operation: FabricCacheManageOp,
                     generationKey: FabricGenerationKey,
                     parameters: Option[Seq[FabricCacheOpParameter]]
                    ): Future[Seq[FabricGeneration]] = {
    TeslaRequestFuture {
      operation match {
        case FabricCacheEvict => evictCount.incrementAndGet()
        case FabricCacheFlush => flushCount.incrementAndGet()
        case _ =>
      }
      Seq.empty
    }
  }

  final override
  def cacheSliceOp(guid: VitalsUid, generationKey: FabricGenerationKey): Future[Seq[FabricSliceMetadata]] = ???
}
