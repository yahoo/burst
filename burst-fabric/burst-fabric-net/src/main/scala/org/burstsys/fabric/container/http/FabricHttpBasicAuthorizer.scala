/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.http
import org.apache.commons.lang3.StringUtils
import org.burstsys.vitals.errors.VitalsException

import java.nio.charset.StandardCharsets
import java.util.Base64
import scala.util.Failure
import scala.util.Success
import scala.util.Try

class FabricHttpBasicAuthorizer extends FabricAuthorizationProvider {

  private val BasicPrefix = "Basic "

  override def authorize(authHeader: String): Try[User] = {
    if (!authHeader.startsWith(BasicPrefix)) {
      return Failure(VitalsException("Not HTTP Basic auth"))
    }
    val authentication = authHeader.stripPrefix(BasicPrefix)
    val values: Array[String] = new String(Base64.getMimeDecoder.decode(authentication), StandardCharsets.UTF_8).split(":")
    if (values.length < 2) {
      return Failure(VitalsException("Not HTTP Basic auth"))
    }
    val username = values(0)
    val password = values(1)
    if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
      return Failure(VitalsException("Username or password missing"))
    }
    checkUser(username, password)
  }

  protected def checkUser(user: String, password: String): Try[User] = {
    (user, password) match {
      case ("burst", "burstomatic") => Success(new User("burst", Array.empty))
      case _ => Failure(VitalsException("Unknown username or password"))
    }
  }

  override def unauthorizedResponseHeaders: Map[String, String] = Map(
    "WWW-Authenticate" -> s"$BasicPrefix realm=\"Burst\"",
  )

  override def isPathPublic(path: String): Boolean = false
}
