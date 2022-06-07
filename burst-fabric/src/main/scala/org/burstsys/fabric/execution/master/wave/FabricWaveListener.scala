/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.execution.master.wave

import org.burstsys.fabric.execution.model.gather.FabricGather
import org.burstsys.vitals.net.VitalsHostName
import org.burstsys.vitals.uid._

/**
 * provide access to ongoing coarse and fine grained wave execution events.
 * these are processed __serially__ on a single [[FabricWaveTalker]] background processor thread
 */
trait FabricWaveListener extends Any {

  /**
   * a wave has begun
   */
  def onWaveBegin(seqNum: FabricWaveSeqNum, guid: VitalsUid): Unit = {}

  /**
   * a particle in a wave has begun
   */
  def onParticleBegin(seqNum: FabricWaveSeqNum, guid: VitalsUid, ruid: VitalsUid, host: String): Unit = {}

  /**
   * a particle in a wave has succeeded
   */
  def onParticleSucceed(seqNum: FabricWaveSeqNum, guid: VitalsUid, ruid: VitalsUid): Unit = {}

  /**
   * a particle in a wave is late
   */
  def onParticleTardy(seqNum: FabricWaveSeqNum, guid: VitalsUid, ruid: VitalsUid, msg: String): Unit = {}

  /**
   * a particle in a wave got a progress message
   */
  def onParticleProgress(seqNum: FabricWaveSeqNum, guid: VitalsUid, ruid: VitalsUid, msg: String): Unit = {}

  /**
   * a particle in a wave has been cancelled
   */
  def onParticleCancelled(seqNum: FabricWaveSeqNum, guid: VitalsUid, ruid: VitalsUid, msg: String): Unit = {}

  /**
   * a particle in a wave has failed
   */
  def onParticleFail(seqNum: FabricWaveSeqNum, guid: VitalsUid, ruid: VitalsUid, msg: String): Unit = {}

  /**
   * a particle in a wave has been retried - probably on a different worker...
   */
  def onParticleRetry(seqNum: FabricWaveSeqNum, guid: VitalsUid, ruid: VitalsUid, msg: String): Unit = {}

  /**
   * a wave has timed out
   */
  def onWaveTimeout(seqNum: FabricWaveSeqNum, guid: VitalsUid, msg: String): Unit = {}

  /**
   * a wave has failed
   */
  def onWaveFail(seqNum: FabricWaveSeqNum, guid: VitalsUid, msg: String): Unit = {}

  /**
   * a wave has succeeded
   */
  def onWaveSucceed(seqNum: FabricWaveSeqNum, guid: VitalsUid, results: FabricGather): Unit = {}

  /**
   * a wave has been cancelled
   */
  def onWaveCancel(seqNum: FabricWaveSeqNum, guid: VitalsUid, msg: String): Unit = {}

}
