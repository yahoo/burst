/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.test.exception

import java.io.{PrintWriter, StringWriter}
import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.serializers.JavaSerializer
import org.burstsys.fabric.exception.{FabricException, FabricGenericException}
import org.burstsys.fabric.test.FabricSpecLog
import org.burstsys.vitals.errors._
import org.burstsys.vitals.kryo.{acquireKryo, releaseKryo}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, Suite}

import scala.language.postfixOps

//@Ignore
class FabricExceptionSpec extends AnyFlatSpec with Suite with Matchers with BeforeAndAfterAll with FabricSpecLog {

  it should "stitch local/remote exception stacks" in {

    // make believe there was an exception on a worker
    val workerSideException = FabricGenericException("", new RuntimeException().fillInStackTrace())

    // that we serialized back to the supervisor
    var supervisorSideException: FabricException = null

    // do the serialization using java serialization in the kryo framework
    val k = acquireKryo
    try {
      val output = new Output(50000)
      k.writeObject(output, workerSideException, new JavaSerializer)
      val encoded = output.toBytes

      val input = new Input(encoded)

      supervisorSideException = k.readObject(input, classOf[FabricException], new JavaSerializer)

      try {

        // stitch together worker and supervisor stack traces before throwing
        throw supervisorSideException.stitch

      } catch safely {
        case fe: FabricException =>
          val writer = new StringWriter()
          fe.printStackTrace(new PrintWriter(writer))
          log info writer.toString
      }

    } finally releaseKryo(k)


  }

}
