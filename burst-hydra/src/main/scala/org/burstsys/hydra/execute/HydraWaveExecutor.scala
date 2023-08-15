/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.execute

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.fabric.wave.exception.FabricQueryProcessingException
import org.burstsys.fabric.wave.execution.model.execute.group.{FabricGroupKey, FabricGroupUid}
import org.burstsys.fabric.wave.execution.model.execute.invoke.FabricInvocation
import org.burstsys.fabric.wave.execution.model.execute.parameters.FabricCall
import org.burstsys.fabric.wave.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.wave.execution.supervisor.group.FabricGroupExecuteContext
import org.burstsys.fabric.wave.metadata.model.datasource.FabricDatasource
import org.burstsys.fabric.wave.metadata.model.over.FabricOver
import org.burstsys.felt.model.analysis.decl.FeltAnalysisDecl
import org.burstsys.hydra.parser.parse
import org.burstsys.hydra.runtime.HydraScanner
import org.burstsys.hydra.trek.HydraSupervisorParse
import org.burstsys.hydra.{HydraService, HydraServiceContext}
import org.burstsys.tesla.thread.request._
import org.burstsys.vitals.errors.{VitalsException, safely}

import scala.concurrent.{Future, Promise}
import scala.language.postfixOps

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
    TeslaRequestFuture {
      var datasource: FabricDatasource = null
      var schema: BrioSchema = null
      val analysis = try {
        datasource = container.metadata.lookup.datasource(over, validate = true)
        schema = BrioSchema(datasource.view.schemaName)

        val stage = HydraSupervisorParse.beginSync(groupUid)
        try {
          val parsed = parse(source = hydraSource, schema = schema)
          HydraSupervisorParse.end(stage)
          parsed
        } catch safely {
          case t =>
            HydraSupervisorParse.fail(stage, t)
            throw t
        } finally stage.closeScope()
      } catch safely {
        case t: Throwable =>
          throw FabricQueryProcessingException("HYDRA", s"HYDRA_PARSE_FAIL $t $tag", t)
      }

      val normalizedSource = analysis.normalizedSource
      val groupKey = FabricGroupKey(groupName = analysis.analysisName, groupUid)
      val modelSchema = analysis.global.brioSchema.name
      if (!schema.aliasedTo(modelSchema)) {
        throw VitalsException(s"HYDRA_SCHEMA_MISMATCH $schema does not match model schema $modelSchema $tag ").fillInStackTrace()
      }

      val scanner = try {
        val invocation = FabricInvocation(call.getOrElse(FabricCall()), normalizedSource, over.locale.timeZoneKey)
        val activePlanes = analysis.frames.length
        HydraScanner().initialize(groupKey, datasource, activePlanes, invocation)
      } catch safely {
        case t: Throwable =>
          throw VitalsException(s"HYDRA_INIT_FAIL $t $tag", t).fillInStackTrace()
      }
      log info s"Created scanner for guid=$groupUid"
      (datasource, groupKey, scanner)
    } chainWithFuture { bits =>
      val (datasource, groupKey, scanner) = bits
      doWaveExecute(name = "HydraExecute", datasource, groupKey, over, scanner, call, executionStart = System.nanoTime())
    }
  }

}
