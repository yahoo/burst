/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys

import java.util.concurrent.TimeUnit

import org.burstsys.tesla.thread.request.TeslaRequestFuture
import org.burstsys.vitals.logging._

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

package object api extends VitalsLogger {

  import com.twitter.{util => twitter}

  implicit def scalaToTwitterTry[T](t: Try[T]): twitter.Try[T] = t match {
    case Success(r) => twitter.Return(r)
    case Failure(ex) => twitter.Throw(ex)
  }

  implicit def twitterToScalaTry[T](t: twitter.Try[T]): Try[T] = t match {
    case twitter.Return(r) => Success(r)
    case twitter.Throw(ex) => Failure(ex)
  }

  object ApiTwitterRequestFuture {

    import tesla.thread.request.teslaRequestExecutor

    def apply[T](body: => T): twitter.Future[T] = {
      scalaToTwitterFuture(TeslaRequestFuture {
        body
      })
    }
  }

  implicit def twitterFutureToScalaFuture[T](twitterF: twitter.Future[T]): Future[T] = {
    val promise = Promise[T]()
    twitterF.respond(promise complete _)
    promise.future
  }

  implicit def scalaToTwitterFuture[T](f: Future[T])(implicit ec: ExecutionContext): twitter.Future[T] = {
    val promise = twitter.Promise[T]()
    f.onComplete(promise update _)
    promise
  }

  implicit def scalaDurationToTwitterDuration(sd: scala.concurrent.duration.Duration): twitter.Duration = {
    twitter.Duration(sd.toNanos, TimeUnit.NANOSECONDS)
  }

}
