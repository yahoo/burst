/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.agent.ops

import io.opentelemetry.api.common.Attributes
import org.burstsys.agent._
import org.burstsys.agent.api.server
import org.burstsys.agent.api.server.maxConcurrencyGate
import org.burstsys.agent.configuration.burstAgentApiTimeoutDuration
import org.burstsys.agent.model.execution.group.call._
import org.burstsys.agent.model.execution.group.over._
import org.burstsys.agent.model.execution.result._
import org.burstsys.agent.transform.AgentTransform
import org.burstsys.api._
import org.burstsys.fabric.wave.exception.FabricQueryProcessingException
import org.burstsys.fabric.wave.execution.model.execute.group.{FabricGroupUid, sanitizeGuid}
import org.burstsys.fabric.wave.execution.model.execute.parameters.FabricCall
import org.burstsys.fabric.wave.execution.model.result.FabricExecuteResult
import org.burstsys.fabric.wave.execution.model.result.status.{FabricFaultResultStatus, FabricInvalidResultStatus, FabricNotReadyResultStatus, FabricTimeoutResultStatus}
import org.burstsys.fabric.wave.metadata.model.over.FabricOver
import org.burstsys.tesla.thread.request._
import org.burstsys.vitals.errors.{VitalsException, messageFromException}

import java.util.concurrent.{ConcurrentHashMap, TimeUnit, TimeoutException}
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.{Failure, Success}

/**
 * Handle language-related Agent tasks, like language registration and query execution
 */
trait AgentExecuteOps extends AgentService {

  self: AgentServiceContext =>

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _languageMap = new ConcurrentHashMap[String, AgentLanguage]()

  private[this]
  var _languages: String = "{}"

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // api
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def registerLanguage(language: AgentLanguage): this.type = {

    for (prefix <- language.languagePrefixes) {
      if (_languageMap.contains(prefix) && _languageMap.get(prefix) != language)
        log warn s"Duplicate prefix in registerLanguage.  Cannot assign $language to $prefix because ${_languageMap.get(prefix)} already claimed it"
      else
        _languageMap.put(prefix.toLowerCase(), language)
    }
    _languages = _languageMap.keySet.asScala.mkString("{'", "', '", "'}")
    this
  }

  final override
  def execute(source: String, over: FabricOver, guid: FabricGroupUid, call: Option[FabricCall]): Future[FabricExecuteResult] = {
    if (modality.isClient) {
      return apiClient.groupExecute(Some(guid), source, over, call.map(fabricToAgentCall)) map thriftToFabricExecuteResult
    }

    val start = System.nanoTime
    lazy val durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime - start)
    val saniGuid = sanitizeGuid(guid)
    if (!saniGuid.startsWith(guid)) {
      log info s"AGENT_PIPELINE_GUID_INVALID guid='$guid' provided, using cleanGuid='$saniGuid' instead"
    }
    AgentRequestTrekMark.begin(saniGuid) { trek =>
      val cleanGuid = s"${saniGuid}_${trek.getTraceId}"
      val tag = s"guid='$guid' cleanGuid='$cleanGuid' $over startConcurrency=${maxConcurrencyGate.get}"
      log info s"AGENT_PIPELINE_BEGIN $tag"

      TeslaRequestFuture { // execute the request if we have a free wave
        onAgentRequestBegin(cleanGuid, source, over, call)

        val capacityAvailable = maxConcurrencyGate.getAndIncrement() < server.maxConcurrency
        if (!capacityAvailable) {
          maxConcurrencyGate.decrementAndGet() // we incremented the count when we tested our current capacity
          FabricExecuteResult(FabricNotReadyResultStatus, "Too many requests")
        } else {
          val wave = delegateLanguage(cleanGuid, source, over, call) andThen {
            case _ => maxConcurrencyGate.decrementAndGet
          }
          Await.result(wave, burstAgentApiTimeoutDuration)
        }
      } recover { // make sure that any unhandled exception is turned into a FabricExecuteResult
        case t: TimeoutException =>
          log.warn(s"AGENT_PIPELINE_TIMEOUT $tag timeout=$burstAgentApiTimeoutDuration", t)
          FabricExecuteResult(FabricTimeoutResultStatus, s"Timeout exceeded. ${messageFromException(t)}")

        case t: FabricQueryProcessingException =>
          val cause = if (t.getCause != null) t.getCause else t
          FabricExecuteResult(FabricInvalidResultStatus, s"Failed to parse ${t.language}", cause)

        case error =>
          val cause = if (error.getCause != null) error.getCause else error
          FabricExecuteResult(FabricFaultResultStatus, s"FAB_EXECUTE_FAIL($cleanGuid) $cause", cause)

      } chainWithFuture { // do any transforms on the result (this is the future that gets returned)
        queryTransform(transform.Identity, _)
      } andThen { // log things and notify the execute tender that this request has finished (this is a side effect on the future that gets returned)
        case Success(result) if result.succeeded =>
          log info s"AGENT_EXECUTE_DONE status=${result.resultStatus} $tag endConcurrency=${maxConcurrencyGate.get} duration=${durationMs}ms success=true"
          onAgentRequestSucceed(cleanGuid)
          trek.span.setAttribute("result.resultSetCount", result.resultGroup.map(_.resultSets.size).getOrElse(0))
          AgentRequestTrekMark.end(trek)

        case Success(result) =>
          log warn s"AGENT_EXECUTE_DONE status=${result.resultStatus} $tag endConcurrency=${maxConcurrencyGate.get} duration=${durationMs}ms message='${result.resultMessage.split("\n")(0)}' failure=true"
          onAgentRequestFail(cleanGuid, result.resultStatus, result.resultMessage)
          trek.span.addEvent(result.resultStatus.name, Attributes.empty)
          AgentRequestTrekMark.fail(trek)

        case Failure(ex) =>
          log warn(s"AGENT_EXECUTE_DONE status=UNHANDLED $tag endConcurrency=${maxConcurrencyGate.get} duration=${durationMs}ms failure=true", ex)
          onAgentRequestFail(cleanGuid, FabricFaultResultStatus, ex.getLocalizedMessage)
          trek.span.recordException(ex)
          AgentRequestTrekMark.fail(trek)
      }
    }
  }

  final override
  def delegateLanguage(guid: FabricGroupUid, source: String, over: FabricOver, call: Option[FabricCall] = None): Future[FabricExecuteResult] = {
    TeslaRequestFuture {
      val processor = source.trim.takeWhile(p => p.isLetterOrDigit).toLowerCase
      val tag = s"AgentPipeline.delegateLanguage(guid='$guid' language='$processor' $over)"
      _languageMap.get(processor) match {
        case null =>
          throw VitalsException(s"AGENT_EXECUTE_PROCESSOR_NOT_FOUND processor='$processor' supported processors are ${_languages} $tag")
        case language =>
          log info tag
          onAgentRequestBegin(guid, source, over, call)
          language
      }
    } chainWithFuture { language =>
      language.executeGroupAsWave(guid, source, over, call)
    }
  }

  final override
  def queryTransform(transform: AgentTransform, results: FabricExecuteResult): Future[FabricExecuteResult] = TeslaRequestFuture {
    transform(results)
  }

}
