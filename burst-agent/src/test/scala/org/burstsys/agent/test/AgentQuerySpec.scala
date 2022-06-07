/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.test

import org.burstsys.agent.api.BurstQueryDataType
import org.burstsys.agent.model.execution.group.datum._
import org.burstsys.agent.model.execution.result.cell._
import org.burstsys.fabric.execution.model.execute.group.{FabricGroupKey, FabricGroupUid}
import org.burstsys.fabric.execution.model.execute.parameters.FabricCall
import org.burstsys.fabric.execution.model.result.FabricExecuteResult
import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.{FabricAggregationCell, FabricResultCell, FabricResultRow}
import org.burstsys.fabric.execution.model.result.set.FabricResultSet
import org.burstsys.fabric.execution.model.result.status.FabricSuccessResultStatus
import org.burstsys.fabric.metadata.model.over
import org.burstsys.fabric.metadata.model.over.FabricOver
import org.burstsys.tesla.thread.request.TeslaRequestFuture

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

/**
 *
 */
final
class AgentQuerySpec extends AgentQuerySpecSupport {

  val columnNames: Array[String] = Array("foo", "bar")
  val columnTypes: Array[BurstQueryDataType] = Array(BurstQueryDataType.ShortType, BurstQueryDataType.DoubleType)

  it should "execute mock processor" in {
    val result = Await.result(agentService.execute(source = "mock kjdhsg", over = over.FabricOver(), guid = "foo"), 10 minutes)
    validate(result)
  }

  private val evictDomain = -1L
  private val evictView = -1L
  private val flushDomain = -1L
  private val flushView = -1L
  private val searchDomain = -1L
  private val searchView = -1L
  private val searchGenClk = -1L

  ignore should "execute commands" in {
    //    agentClient.cacheEvict(FabricGenerationKey(66, 77))
    evictDomain should equal(66)
    evictView should equal(77)

    //    agentClient.cacheFlush(FabricGenerationKey(88, 99))
    flushDomain should equal(88)
    flushView should equal(99)

    //    agentClient.cacheSearch(FabricGenerationKey(31, 32, 33))
    searchDomain should equal(31)
    searchView should equal(32)
    searchGenClk should equal(33)
  }

  /**
   */
  override
  def executeGroupAsWave(groupUid: FabricGroupUid, source: String, over: FabricOver, call: Option[FabricCall]): Future[FabricExecuteResult] = {
    TeslaRequestFuture {
      val mockRows: Array[FabricResultRow] =
        Seq(
          Seq(
            asShort(isNull = false, isNan = false, 0x55, FabricAggregationCell),
            asDouble(isNull = false, isNan = false, 4.5, FabricAggregationCell),
            asBoolean(isNull = false, value = true, FabricAggregationCell),
          )
        ).map {
          r =>
            FabricResultRow(
              cells = r.map {
                c => c: FabricResultCell
              }.toArray
            )
        }.toArray


      val mockResultSet = FabricResultSet(
        resultName = "mock-result-set",
        columnNames = columnNames,
        columnTypeKeys = columnTypes.map(datatypeOf),
        rowSet = mockRows
      )
      FabricExecuteResult(
        resultGroup = Some(
          FabricResultGroup(
            groupKey = FabricGroupKey(groupName = "mock", groupUid = groupUid),
            resultSets = Map(0 -> mockResultSet),
            rowCount = 1
          )
        )
      )
    }
  }

  private def validate(er: FabricExecuteResult): Unit = {
    val r = er.resultGroup.get
    er.resultStatus should equal(FabricSuccessResultStatus)
    // r.groupKey.groupUid should equal("foo")
    val rs = r.resultSets(0)
    rs.columnNames should equal(columnNames)
    rs.columnTypeKeys.map(datatypeFor).sortBy(_.name) should equal(columnTypes.sortBy(_.name))
    rs.metrics.limited should equal(false)
    rs.rowSet.head.cells(0).bType should equal(datatypeOf(BurstQueryDataType.ShortType))
    rs.rowSet.head.cells(0).isNull should equal(false)
    rs.rowSet.head.cells(0).isNan should equal(false)
    rs.rowSet.head.cells(1).bType should equal(datatypeOf(BurstQueryDataType.DoubleType))
    rs.rowSet.head.cells(1).isNull should equal(false)
    rs.rowSet.head.cells(1).isNan should equal(false)
    rs.rowSet.head.cells(2).bType should equal(datatypeOf(BurstQueryDataType.BooleanType))
    rs.rowSet.head.cells(2).isNull should equal(false)
  }


}
