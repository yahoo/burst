/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.http

import com.fasterxml.jackson.databind.{DeserializationFeature, JsonNode, ObjectMapper}
import org.burstsys.vitals
import org.burstsys.vitals.background.VitalsBackgroundFunctions
import org.burstsys.vitals.background.VitalsBackgroundFunctions.BackgroundFunction
import org.burstsys.vitals.errors.{VitalsException, _}
import org.burstsys.vitals.logging._
import org.glassfish.grizzly.websockets.{DataFrame, WebSocket, WebSocketApplication, WebSocketEngine}

import java.io.StringWriter
import java.util.concurrent.ConcurrentHashMap
import scala.annotation.nowarn
import scala.concurrent.duration._
import scala.language.postfixOps

trait FabricWebSocketGroup {

  /**
   * The URL this websocket listens on, relative to the websocket application root
   */
  def url: String

  /**
   * The full URL this websocket listens on
   */
  def fullUrl: String

  /**
   * Encode the message as json and broadcast it to all connected websockets
   *
   * @param message the object to encode and send
   */
  def broadcastJson(message: Any): Unit

  /**
   * Broadcast the json object to all connected websockets
   *
   * @param node the json to send
   */
  def broadcastNode(node: JsonNode): Unit

  /**
   * Close any open websocket connections
   */
  def close(): Unit

}

trait FabricWebSocket extends Equals {

  def ping(): Unit

  def sendJson(message: Any): Unit

  def sendNode(node: JsonNode): Unit

  def sendRaw(text: String): Unit

  def close(): Unit
}

object FabricWebSocket {
  def apply(underlying: WebSocket): FabricWebSocket =
    WebSocketContext(underlying)

  private val mapper: ObjectMapper = {
    vitals.json.buildJsonMapper
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  }

  def encode(data: Any): String = {
    try {
      val writer = new StringWriter()
      mapper.writeValue(writer, data)
      writer.toString
    } catch safely {
      case t: Throwable => throw VitalsException(t)
    }
  }

  def decode(json: String): Any = {
    mapper.readValue(json, classOf[Any])
  }
}

private final case
class WebSocketContext(socket: WebSocket) extends FabricWebSocket {

  override def hashCode(): Int = socket.hashCode()

  override def canEqual(that: Any): Boolean = that.isInstanceOf[WebSocketContext] || that.isInstanceOf[WebSocket]

  override def equals(that: Any): Boolean = that match {
    case burstWebSocket: WebSocketContext => burstWebSocket.socket == socket
    case webSocket: WebSocket => webSocket == socket
    case _ => false
  }

  override def ping(): Unit = socket.sendPing(Array.emptyByteArray)

  override def sendJson(message: Any): Unit = {
    val payload = FabricWebSocket.encode(message)
    sendRaw(payload)
  }

  override def sendNode(node: JsonNode): Unit = {
    sendRaw(node.toString)
  }

  override def sendRaw(text: String): Unit = {
    try {
      if (socket.isConnected)
        socket.send(text)
    } catch safely {
      case t: Throwable =>
        log error burstLocMsg(s"WEBSOCKET_SEND_ERROR length=${text.length} text=${text.substring(0, 100)} ${if (text.length > 100) "..." else ""}", t)
        throw VitalsException(t)
    }
  }

  override def close(): Unit = socket.close()
}

private final
case class WebSocketKeepAlive(websocket: FabricWebSocket) extends BackgroundFunction {
  // Send a ping to the underlying websocket
  override def apply(): Unit = websocket.ping()

  // ensure that this function == the websocket so that it can be unregistered later
  override def hashCode(): Int = websocket.hashCode()

  override def canEqual(that: Any): Boolean = that match {
    case fabWS: WebSocketKeepAlive => websocket.canEqual(fabWS.websocket)
    case _ => false
  }

  override def equals(that: Any): Boolean = that match {
    case fabWS: WebSocketKeepAlive => websocket.equals(fabWS.websocket)
    case _ => false
  }
}


object FabricWebSocketGroup {
  def apply(url: String, listener: FabricWebSocketListener): FabricWebSocketGroup =
    WebSocketGroupContext(url, listener)
}

private final case
class WebSocketGroupContext(url: String, listener: FabricWebSocketListener)
  extends WebSocketApplication with FabricWebSocketGroup {

  log info s"WEBSOCKET_OPEN $fullUrl"
  WebSocketEngine.getEngine.register("/ws", url, this)

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // private state
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  val _sockets = ConcurrentHashMap.newKeySet[FabricWebSocket]

  private[this]
  val _keepalive = new VitalsBackgroundFunctions(s"keepalive[$url]", 1 minute, 1 minute)
  _keepalive.start

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  override def fullUrl: String = s"/ws$url"

  override def broadcastJson(message: Any): Unit = {
    val raw = FabricWebSocket.encode(message)
    broadcastText(raw)
  }

  override def broadcastNode(node: JsonNode): Unit = {
    val raw = node.toString
    broadcastText(raw)
  }

  override def close(): Unit = {
    _keepalive.stop
    val itr = _sockets.iterator()
    while (itr.hasNext) {
      val socket = itr.next
      try {
        socket.close()
      } catch safely {
        case e => log warn(s"Problem in socket $socket", e)
      }
    }
    WebSocketEngine.getEngine.unregister(this)
  }

  /**
   * send a message on each socket. Each socket should be shielded from problems talking to other sockets
   */
  private def broadcastText(raw: String): Unit = {
    val itr = _sockets.iterator()
    while (itr.hasNext) {
      val socket = itr.next
      try {
        socket.sendRaw(raw)
      } catch safely {
        case e => log warn(s"Problem in socket $socket", e)
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // lifecycle
  //////////////////////////////////////////////////////////////////////////////////////////////////////

  override def onConnect(socket: WebSocket): Unit = {
    super.onConnect(socket)
    val websocket = FabricWebSocket(socket)
    _sockets.add(websocket)
    _keepalive += WebSocketKeepAlive(websocket)
    log info s"WEB_SOCKET_CONNECTED path=$fullUrl connections=${_sockets.size()}"
    try {
      listener.onWebSocketOpen(this, websocket)
    } catch safely {
      case t: Throwable => throw VitalsException(t)
    }
  }

  override def onClose(socket: WebSocket, frame: DataFrame): Unit = {
    super.onClose(socket, frame)
    val websocket = FabricWebSocket(socket)
    _sockets.remove(websocket)
    _keepalive -= WebSocketKeepAlive(websocket)
    log info s"WEB_SOCKET_CLOSED path=$fullUrl connections=${_sockets.size()}"
    try {
      listener.onWebSocketClose(this, websocket)
    } catch safely {
      case t: Throwable => throw VitalsException(t)
    }
  }

  override def onPing(socket: WebSocket, bytes: Array[Byte]): Unit = socket.sendPong(Array.emptyByteArray)

  override def onMessage(socket: WebSocket, text: String): Unit = {
    log info burstStdMsg(s"web socket $fullUrl received a message: $text")
    super.onMessage(socket, text)
    val websocket = FabricWebSocket(socket)
    try {
      val json = FabricWebSocket.decode(text)
      listener.onWebSocketReceive(this, websocket, json)
      @nowarn("msg=the type test for.*?has type parameters eliminated by erasure")
      val _ = json match {
        case map: Map[String, Any] => // cast removed by erasure, but json implies that if this _is_ a map it must be [String, Any]
          map.get("action") match {
            case Some(action: String) =>
              listener.onWebSocketAction(this, websocket, action, map)
            case other =>
              log info s"It looks like a websocket tried to send an action, but found '$other' payload '$text'"
          }
      }
    } catch safely {
      case t: Throwable => throw VitalsException(t)
    }
  }

}
