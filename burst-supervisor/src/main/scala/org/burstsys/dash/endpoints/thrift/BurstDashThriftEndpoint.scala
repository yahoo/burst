/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.endpoints.thrift

import jakarta.inject.Inject
import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.protocol.TProtocolFactory
import org.apache.thrift.transport.TIOStreamTransport
import org.burstsys.dash.application.BurstDashEndpointBase
import org.burstsys.dash.service.thrift.requestLog
import org.burstsys.gen.thrift.api.client.BTBurstService
import org.burstsys.gen.thrift.api.client.BTBurstService.Iface
import org.burstsys.vitals.errors.safely
import org.burstsys.vitals.uid.newBurstUid

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.lang.annotation.Annotation
import java.lang.reflect.Type
import jakarta.inject.Inject
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.MultivaluedMap
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.MessageBodyWriter
import jakarta.ws.rs.ext.Provider

object BurstDashThriftEndpoint {
  val clientProcessor = "clientThriftProcessor"
}

@Path("/thrift/client")
class BurstDashThriftEndpoint extends BurstDashEndpointBase {
  @Inject var processor: BTBurstService.Processor[Iface] = _
  @Inject var factory: TProtocolFactory = new TBinaryProtocol.Factory()

  @POST
  @Consumes(Array("application/x-thrift"))
  @Produces(Array("application/x-thrift"))
  def handlePost(message: InputStream): Response = {
    val ruid = newBurstUid
    requestLog.beginReqest(ruid)
    val response = new ByteArrayOutputStream(32 * 1028)
    val transport = new TIOStreamTransport(message, response)
    try {
      val protocol = factory.getProtocol(transport)
      processor.process(protocol, protocol)
      Response.ok(response).build()

    } catch safely {
      case e =>
        log.error(e)
        requestLog.requestEncounteredException(ruid, e)
        throw e

    } finally {
      try {
        transport.close()
      } catch safely {
        case e =>
          log.error(e)
          requestLog.requestEncounteredException(ruid, e)
      }
    }
  }
}

@Provider
@Produces(Array("application/x-thrift"))
class BurstThriftMessageBodyWriter extends MessageBodyWriter[Any] {
  override def isWriteable(
                            clazz: Class[_],
                            genericType: Type,
                            annotations: Array[Annotation],
                            mediaType: MediaType
                          ): Boolean =
    mediaType match {
      case media if media.getType == "application" && media.getSubtype == "x-thrift" =>
        clazz match {
          case os if classOf[ByteArrayOutputStream].isAssignableFrom(os) => true
          case bytes if classOf[Array[Byte]].isAssignableFrom(bytes) => true
          case _ => false
        }
      case _ => false
    }

  override def writeTo(
                        t: Any,
                        clazz: Class[_],
                        genericType: Type,
                        annotations: Array[Annotation],
                        mediaType: MediaType,
                        httpHeaders: MultivaluedMap[String, AnyRef],
                        entityStream: OutputStream
                      ): Unit = {
    t match {
      case baos: ByteArrayOutputStream => baos.writeTo(entityStream)
      case bytes: Array[Byte] => entityStream.write(bytes)
    }
  }
}
