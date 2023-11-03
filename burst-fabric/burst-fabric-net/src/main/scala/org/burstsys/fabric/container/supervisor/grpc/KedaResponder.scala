/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.supervisor.grpc

import io.grpc.stub.StreamObserver
import org.burstsys.fabric.container.supervisor.log
import org.burstsys.vitals.logging.burstLocMsg
import sh.keda.ExternalScalerGrpc
import sh.keda.GetMetricSpecResponse
import sh.keda.GetMetricsRequest
import sh.keda.GetMetricsResponse
import sh.keda.IsActiveResponse
import sh.keda.MetricSpec
import sh.keda.MetricValue
import sh.keda.ScaledObjectRef

trait BurstScalingService {
  def workersActive: Boolean

  def currentWorkerCount: Int

  def desiredWorkerCount: Int
}

/**
 * Implement the gRPC client for our external scaler. See the docs at https://keda.sh/docs/2.12/concepts/external-scalers/
 * The implmentation for Burst is simple, but not completely intuitve. This scaler has a single metric, workerScaleFactor,
 * which is the knob we can use to scale the number of workers. Because we're providing metrics to a kubernetes HPA we
 * cannot directly control the number of workers, only if the number of workers should be scaled up or down,
 * and by what magnitude the scaling should occur.
 */
class KedaResponder(scalingSource: BurstScalingService) extends ExternalScalerGrpc.ExternalScalerImplBase {

  /**
   * Called by KEDA initially and on a timer to determine if the scaler is active. When this function
   * returns true KEDA will use the metrics/metrics spec to scale the deployment.
   */
  override def isActive(request: ScaledObjectRef, responseObserver: StreamObserver[IsActiveResponse]): Unit = {
    val isActiveResponse = IsActiveResponse.newBuilder().setResult(scalingSource.workersActive).build()

    log info burstLocMsg(s"Responding to isActive request with $isActiveResponse")
    responseObserver.onNext(isActiveResponse)
    responseObserver.onCompleted()
  }

  /**
   * Not currently supported.
   *
   * Called by KEDA with the expectation that the scaler will hold the connection open and push updates.
   * (Used for external-push type scalers.)
   */
  override def streamIsActive(request: ScaledObjectRef, responseObserver: StreamObserver[IsActiveResponse]): Unit = super.streamIsActive(request, responseObserver)

  private val workerCount = "workerScaleFactor"

  /**
   */
  override def getMetricSpec(request: ScaledObjectRef, responseObserver: StreamObserver[GetMetricSpecResponse]): Unit = {
    val metricSpec = GetMetricSpecResponse.newBuilder()
      .addMetricSpecs(
        MetricSpec.newBuilder()
          .setMetricName(workerCount)
          .setTargetSize(scalingSource.currentWorkerCount).build())
      .build()

    log info burstLocMsg(s"Responding to getMetricSpec request with $metricSpec")
    responseObserver.onNext(metricSpec)
    responseObserver.onCompleted()
  }

  /**
   */
  override def getMetrics(request: GetMetricsRequest, responseObserver: StreamObserver[GetMetricsResponse]): Unit = {
    val metrics = GetMetricsResponse.newBuilder()
      .addMetricValues(MetricValue.newBuilder().setMetricName(workerCount).setMetricValue(scalingSource.desiredWorkerCount).build())
      .build()

    log info burstLocMsg(s"Responding to getMetrics request with $metrics")
    responseObserver.onNext(metrics)
    responseObserver.onCompleted()
  }
}

