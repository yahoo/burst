/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.execution.master.wave

import org.burstsys.fabric.execution.master.FabricMasterExecutionContext
import org.burstsys.fabric.execution.model.gather.FabricGather
import org.burstsys.fabric.execution.model.pipeline.{FabricPipelineEvent, publishPipelineEvent}
import org.burstsys.vitals.net.VitalsHostName
import org.burstsys.vitals.uid._

/**
 * handle updating all listeners as waves start, progress, and complete
 */
trait FabricWaveTalker extends AnyRef with FabricWaveListener {

  self: FabricMasterExecutionContext =>

  private[this] def publish(event: FabricWaveEvent): Unit = publishPipelineEvent(event)

  final override
  def onWaveBegin(seqNum: FabricWaveSeqNum, guid: VitalsUid): Unit = {
    publish(WaveBegan(guid, seqNum))
  }

  final override
  def onParticleBegin(seqNum: FabricWaveSeqNum, guid: VitalsUid, ruid: VitalsUid, host: String): Unit = {
    publish(ParticleDispatched(guid, seqNum, ruid, host))
  }

  final override
  def onParticleSucceed(seqNum: FabricWaveSeqNum, guid: VitalsUid, ruid: VitalsUid): Unit = {
    publish(ParticleSucceeded(guid, seqNum, ruid))
  }

  final override
  def onParticleFail(seqNum: FabricWaveSeqNum, guid: VitalsUid, ruid: VitalsUid, msg: String): Unit = {
    publish(ParticleFailed(guid, seqNum, ruid, msg))
  }

  final override
  def onParticleRetry(seqNum: FabricWaveSeqNum, guid: VitalsUid, ruid: VitalsUid, msg: String): Unit = {
    publish(ParticleRetried(guid, seqNum, ruid, msg))
  }

  final override
  def onParticleTardy(seqNum: FabricWaveSeqNum, guid: VitalsUid, ruid: VitalsUid, msg: String): Unit = {
    publish(ParticleTardy(guid, seqNum, ruid, msg))
  }

  final override
  def onParticleProgress(seqNum: FabricWaveSeqNum, guid: VitalsUid, ruid: VitalsUid, msg: String): Unit = {
    publish(ParticleProgress(guid, seqNum, ruid, msg))
  }

  final override
  def onParticleCancelled(seqNum: FabricWaveSeqNum, guid: VitalsUid, ruid: VitalsUid, msg: String): Unit = {
    publish(ParticleCancelled(guid, seqNum, ruid, msg))
  }

  final override
  def onWaveFail(seqNum: FabricWaveSeqNum, guid: VitalsUid, msg: String): Unit = {
    publish(WaveFailed(guid, seqNum, msg))
  }

  final override
  def onWaveTimeout(seqNum: FabricWaveSeqNum, guid: VitalsUid, msg: String): Unit = {
    publish(WaveTimeout(guid, seqNum, msg))
  }

  final override
  def onWaveCancel(seqNum: FabricWaveSeqNum, guid: VitalsUid, msg: String): Unit = {
    publish(WaveCanceled(guid, seqNum, msg))
  }

  final override
  def onWaveSucceed(seqNum: FabricWaveSeqNum, guid: VitalsUid, results: FabricGather): Unit = {
    publish(WaveSucceeded(guid, seqNum, results))
  }
}

trait FabricWaveEvent extends FabricPipelineEvent {
  /**
   * The sequence number is a monotonically increasing pseudo-identifier for a wave
   */
  def seqNum: FabricWaveSeqNum
}

trait FabricParticleEvent extends FabricWaveEvent {
  /**
   * The guid for this particle's execution
   */
  def ruid: VitalsUid
}

final case class WaveBegan(guid: VitalsUid, seqNum: FabricWaveSeqNum) extends FabricWaveEvent

final case class ParticleDispatched(guid: VitalsUid, seqNum: FabricWaveSeqNum, ruid: VitalsUid, host: String) extends FabricParticleEvent

final case class ParticleSucceeded(guid: VitalsUid, seqNum: FabricWaveSeqNum, ruid: VitalsUid) extends FabricParticleEvent

final case class ParticleFailed(guid: VitalsUid, seqNum: FabricWaveSeqNum, ruid: VitalsUid, message: String) extends FabricParticleEvent

final case class ParticleCancelled(guid: VitalsUid, seqNum: FabricWaveSeqNum, ruid: VitalsUid, message: String) extends FabricParticleEvent

final case class ParticleRetried(guid: VitalsUid, seqNum: FabricWaveSeqNum, ruid: VitalsUid, message: String) extends FabricParticleEvent

final case class ParticleTardy(guid: VitalsUid, seqNum: FabricWaveSeqNum, ruid: VitalsUid, message: String) extends FabricParticleEvent

final case class ParticleProgress(guid: VitalsUid, seqNum: FabricWaveSeqNum, ruid: VitalsUid, message: String) extends FabricParticleEvent

final case class WaveFailed(guid: VitalsUid, seqNum: FabricWaveSeqNum, message: String) extends FabricWaveEvent

final case class WaveTimeout(guid: VitalsUid, seqNum: FabricWaveSeqNum, message: String) extends FabricWaveEvent

final case class WaveCanceled(guid: VitalsUid, seqNum: FabricWaveSeqNum, message: String) extends FabricWaveEvent

final case class WaveSucceeded(guid: VitalsUid, seqNum: FabricWaveSeqNum, results: FabricGather) extends FabricWaveEvent


