/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.execute

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.fabric.exception.FabricQueryProcessingException
import org.burstsys.fabric.execution.supervisor.group.FabricGroupExecuteContext
import org.burstsys.fabric.execution.model.execute.group.{FabricGroupKey, FabricGroupUid}
import org.burstsys.fabric.execution.model.execute.invoke.FabricInvocation
import org.burstsys.fabric.execution.model.execute.parameters.FabricCall
import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.metadata.model.over.FabricOver
import org.burstsys.hydra.parser.parse
import org.burstsys.hydra.runtime.HydraScanner
import org.burstsys.hydra.trek.{HydraSupervisorParse, HydraSupervisorSchemaLookup}
import org.burstsys.hydra.{HydraService, HydraServiceContext}
import org.burstsys.tesla.thread.request._
import org.burstsys.vitals.errors.VitalsException

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future, Promise}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

trait HydraWaveExecutor extends FabricGroupExecuteContext with HydraService {

  self: HydraServiceContext =>

  ///////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   */
  final override
  def executeHydraAsWave(groupUid: FabricGroupUid, hydraSource: String, over: FabricOver, call: Option[FabricCall] = None): Future[FabricResultGroup] = {
    val tag = s"HydraWaveExecutor.executeHydraAsWave(guid=$groupUid)"
    val promise = Promise[FabricResultGroup]()
    //  parse the hydra source so we can validate the syntax/semantics and generate its normalized form.
    var datasource: FabricDatasource = null
    var schema: BrioSchema = null
    Try {
      HydraSupervisorSchemaLookup.begin(groupUid)
      datasource = container.metadata.lookup.datasource(over = over, validate = true)
      schema = BrioSchema(datasource.view.schemaName)
      HydraSupervisorSchemaLookup.end(groupUid)

      HydraSupervisorParse.begin(groupUid)
      val analysis = parse(source = hydraSource, schema = schema)
      HydraSupervisorParse.end(groupUid)
      analysis
    } match {
      case Failure(t) => promise.failure(FabricQueryProcessingException("HYDRA", s"HYDRA_PARSE_FAIL $t $tag", t))
      case Success(analysis) =>
        val normalizedSource = analysis.normalizedSource
        val groupKey = FabricGroupKey(groupName = analysis.analysisName, groupUid = groupUid)
        val modelSchema = analysis.global.brioSchema.name
        if (!schema.aliasedTo(modelSchema)) {
          promise.failure(VitalsException(s"HYDRA_SCHEMA_MISMATCH $schema does not match model schema $modelSchema $tag ").fillInStackTrace())
        } else {
          Try {
            val invocation = FabricInvocation(call.getOrElse(FabricCall()), normalizedSource, over.locale.timeZoneKey)
            val activePlanes = analysis.frames.length
            val scanner = HydraScanner().initialize(group = groupKey, datasource = datasource, activePlanes = activePlanes, invocation = invocation)
            log info s"Created scanner for guid=$groupUid"
            scanner
          } match {
            case Failure(t) => promise.failure(VitalsException(s"HYDRA_INIT_FAIL $t $tag", t).fillInStackTrace())
            case Success(scanner) =>
              doWaveExecute(name = "HydraExecute", datasource, groupKey, over, scanner, call, executionStart = System.nanoTime()) onComplete {
                case Failure(t) =>
                  promise.failure(VitalsException(s"HYDRA_EXECUTE_FAIL $t $tag", t).fillInStackTrace())
                case Success(r) =>
                  promise.success(r)
              }
          }
        }
    }
    promise.future
  }

}
