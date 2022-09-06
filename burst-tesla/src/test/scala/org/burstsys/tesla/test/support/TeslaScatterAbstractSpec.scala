/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.tesla.test.support

import java.util.concurrent.atomic.AtomicInteger

import org.burstsys.tesla.configuration.scatterTenderIntervalProperty
import org.burstsys.tesla.scatter.slot.{TeslaScatterSlotBegin, TeslaScatterSlotCancel, TeslaScatterSlotFail, TeslaScatterSlotProgress, TeslaScatterSlotSucceed, TeslaScatterSlotTardy}
import org.burstsys.tesla.scatter.{TeslaScatter, TeslaScatterBegin, TeslaScatterCancel, TeslaScatterFail, TeslaScatterRequestContext, TeslaScatterSucceed, TeslaScatterTimeout, pool}
import org.burstsys.tesla.thread.request.{TeslaRequestCoupler, TeslaRequestFuture}
import org.burstsys.vitals.background.VitalsBackgroundFunction
import org.burstsys.vitals.errors.{VitalsException, _}
import org.burstsys.vitals.net.{VitalsHostName, getPublicHostName}
import org.burstsys.vitals.uid.{VitalsUid, newBurstUid}
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Future, Promise}
import scala.language.postfixOps

trait TeslaScatterAbstractSpec extends TeslaAbstractSpec with BeforeAndAfterAll {
  private var _scatter: TeslaScatter = _

  final val scatterBeginCount = new AtomicInteger(0)
  final val scatterCancelCount = new AtomicInteger(0)
  final val scatterTimeoutCount = new AtomicInteger(0)
  final val scatterFailCount = new AtomicInteger(0)
  final val scatterSucceedCount = new AtomicInteger(0)

  final val slotBeginCount = new AtomicInteger(0)
  final val slotProgressCount = new AtomicInteger(0)
  final val slotTardyCount = new AtomicInteger(0)
  final val slotCancelCount = new AtomicInteger(0)
  final val slotFailCount = new AtomicInteger(0)
  final val slotSucceedCount = new AtomicInteger(0)


  override protected def beforeAll(): Unit = {
    scatterTenderIntervalProperty.set(5)
  }


  override protected def afterAll(): Unit = {
    scatterTenderIntervalProperty.useDefault()
  }

  override def beforeEach(): Unit = {
    scatterBeginCount.set(0)
    scatterCancelCount.set(0)
    scatterTimeoutCount.set(0)
    scatterFailCount.set(0)
    scatterSucceedCount.set(0)
    slotBeginCount.set(0)
    slotProgressCount.set(0)
    slotTardyCount.set(0)
    slotCancelCount.set(0)
    slotFailCount.set(0)
    slotSucceedCount.set(0)

    continuePolling = true
  }


  override protected def afterEach(): Unit = {
    pool.releaseScatter(_scatter)
    _scatter = null
  }

  def pollWaitDuration: Duration = 10 seconds

  def awaitTimeout: Duration = 10 seconds

  var continuePolling: Boolean = true

  def onSlotBegin(update: TeslaScatterSlotBegin): Unit = slotBeginCount.getAndIncrement()

  def onSlotProgress(update: TeslaScatterSlotProgress): Unit = slotProgressCount.getAndIncrement()

  def onSlotTardy(update: TeslaScatterSlotTardy): Unit = slotTardyCount.getAndIncrement()

  def onSlotSucceed(update: TeslaScatterSlotSucceed): Unit = slotSucceedCount.getAndIncrement()

  def onSlotCancelled(update: TeslaScatterSlotCancel): Unit = slotCancelCount.getAndIncrement()

  def onSlotFailed(update: TeslaScatterSlotFail): Unit = slotFailCount.getAndIncrement()

  def onScatterBegin(update: TeslaScatterBegin): Unit = scatterBeginCount.getAndIncrement()

  def onScatterSucceed(update: TeslaScatterSucceed): Unit = fail("Unhandled scatter succeed")

  def onScatterCancel(update: TeslaScatterCancel): Unit = fail("Unhandled scatter cancel")

  def onScatterTimeout(update: TeslaScatterTimeout): Unit = fail("Unhandled scatter timeout")

  def onScatterFail(update: TeslaScatterFail): Unit = fail("Unhandled scatter fail")

  def finish(counter: AtomicInteger): Unit = {
    counter.incrementAndGet
    continuePolling = false
  }

  private def processUpdates(scatter: TeslaScatter): Unit = {
    while (continuePolling) {
      scatter.nextUpdate(pollWaitDuration) match {
        case begin: TeslaScatterSlotBegin => onSlotBegin(begin)

        case progress: TeslaScatterSlotProgress => onSlotProgress(progress)

        case tardy: TeslaScatterSlotTardy => onSlotTardy(tardy)

        case succeed: TeslaScatterSlotSucceed => onSlotSucceed(succeed)

        case fail: TeslaScatterSlotFail => onSlotFailed(fail)

        case cancel: TeslaScatterSlotCancel => onSlotCancelled(cancel)

        case begin: TeslaScatterBegin => onScatterBegin(begin)

        case succeed: TeslaScatterSucceed => onScatterSucceed(succeed)

        case cancel: TeslaScatterCancel => onScatterCancel(cancel)

        case timeout: TeslaScatterTimeout => onScatterTimeout(timeout)

        case fail: TeslaScatterFail => onScatterFail(fail)
      }
    }
  }

  final def scatter: TeslaScatter = if (_scatter != null) _scatter else fail("No scatter available")

  final def runScatter(configure: TeslaScatter => Unit): Unit = {
    try {
      TeslaRequestCoupler({
        _scatter = pool.grabScatter(newBurstUid)
        scatter.failures.length should equal(0)
        scatter.successes.length should equal(0)
        scatter.zombies.length should equal(0)

        assert(_scatter.successes.isEmpty, "Did not close properly")
        assert(_scatter.failures.isEmpty, "Did not close properly")

        configure(_scatter)
        _scatter.execute()
        processUpdates(_scatter)
      }, awaitTimeout)
    } catch safely {
      case t: Throwable =>
        continuePolling = false
        log error s"$t"
        throw t
    }
  }

  final def assertSlotCounts(begin: Int = 0, progress: Int = 0, succeed: Int = 0, fail: Int = 0, cancel: Int = 0, tardy: Int = 0): Unit = {
    slotBeginCount.get should equal(begin)
    // timing is a tricky thing, so if we're expecting to see progress messages we'll be lenient about how many we get
    if (progress > 0) {
      val count = slotProgressCount.get
      assert(progress - 2 <= count && count <= progress + 2, "slot progress, approximately")
    } else {
      assert(slotProgressCount.get == progress, "slot progress, exactly")
    }
    if (tardy > 0) {
      val count = slotTardyCount.get
      assert(tardy - 2 <= count && count <= tardy + 2, "slot tardy count, approximately")
    } else {
      assert(slotTardyCount.get == tardy, "slot tardy count, exactly")
    }
    assert(slotSucceedCount.get == succeed, "slot succeed count")
    assert(slotFailCount.get == fail, "slot fail count")
    assert(slotCancelCount.get == cancel, "slot cancel count")
  }

  final def assertScatterCounts(begin: Int = 0, succeed: Int = 0, fail: Int = 0, timeout: Int = 0, cancel: Int = 0): Unit = {
    assert(scatterBeginCount.get == begin, "scatter begin count")
    assert(scatterSucceedCount.get == succeed, "scatter succeed count")
    assert(scatterFailCount.get == fail, "scatter fail count")
    assert(scatterTimeoutCount.get == timeout, "scatter timeout count")
    assert(scatterCancelCount.get == cancel, "scatter cancel count")
  }

  case class MockRequest(
                                updates: Boolean = false,
                                updateInterval: Duration = 1 second,
                                tardyInterval: Duration = 10 seconds,
                                succeeds: Boolean = false,
                                succeedAfter: Duration = 5 seconds,
                                fails: Boolean = false,
                                failAfter: Duration = 5 seconds,
                                after: Option[MockRequest => Unit] = None,
                                destinationHostName: VitalsHostName = getPublicHostName
                              ) extends TeslaScatterRequestContext[Unit] {

    var updater: Option[VitalsBackgroundFunction] = None

    private def shutdown(): Unit = {
      updater.foreach(_.stopIfNotAlreadyStopped)
    }

    def ruid: VitalsUid = newBurstUid

    def fail(msg: String): Unit = {
      slot.slotFailed(VitalsException(msg))
    }

    override def tardyAfter: Duration = tardyInterval

    override def execute: Future[_] = {
      val promise = Promise[Unit]()
      slot.slotBegin()

      if (updates) {
        updater = Some(new VitalsBackgroundFunction(s"request-${slot.slotId}-progress", 0 seconds, updateInterval, {
          slot.slotProgress(s"slot#${slot.slotId} progress")
        }).start)
      }

      if (succeeds) {
        promise.success((): Unit)
        TeslaRequestFuture {
          Thread.sleep(succeedAfter.toMillis)
          slot.slotSuccess()
          after.foreach(_.apply(this))
        }
      }

      if (fails) {
        val msg = "You told me to..."
        promise.failure(VitalsException(msg).fillInStackTrace())
        TeslaRequestFuture {
          Thread.sleep(failAfter.toMillis)
          this.fail(msg)
          after.foreach(_.apply(this))
        }
      }
      promise.future
    }

    override def cancel(): Unit = {
      super.cancel()
      shutdown()
    }

    override def close(): Unit = {
      shutdown()
    }

  }

}
