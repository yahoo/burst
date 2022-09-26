/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.application

import org.burstsys.catalog.CatalogService
import org.burstsys.dash.endpoints
import org.apache.commons.lang3.StringUtils
import org.apache.commons.net.util.Base64
import org.glassfish.jersey.server.ContainerRequest

import java.nio.charset.StandardCharsets
import java.security.Principal
import jakarta.inject.Inject
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.container.PreMatching
import jakarta.ws.rs.core._
import jakarta.ws.rs.ext.Provider
import scala.util.Failure
import scala.util.Success


case class User(username: String, role: Array[String])

case class Authorizer(user: User, secure: Boolean) extends SecurityContext {
  val principal: Principal = new Principal {
    def getName: String = user.username
  }

  def getUserPrincipal: Principal = principal

  def isUserInRole(role: String): Boolean = user.role.contains(role)

  def isSecure: Boolean = secure

  def getAuthenticationScheme: String = SecurityContext.BASIC_AUTH
}


/**
 * Determine if the user has presented valid credentials
 */
@Provider
@PreMatching
class BurstDashSecurityFilter extends ContainerRequestFilter {

  @Inject
  var uriInfo: UriInfo = _

  @Inject
  var catalog: CatalogService = _

  private final val basicPrefix = "Basic "
  private final val burstRealm = "\"Burst\""
  private final val WwwAuthenticate = "WWW-Authenticate"
  private final val restPathRegex = s"${endpoints.BurstRestUrlBase.stripPrefix("/")}.+"
  /**
   * Ensure that users are authenticated on protected paths
   */
  def filter(filterContext: ContainerRequestContext): Unit = {
    uriInfo.getPath match {
      case "status.html" | "akamai" => // health check URLs that don't require auth
      case "logout" => userMustAuthenticate()
      case restEndpoint if restEndpoint.matches(restPathRegex)  =>
        val user: User = authenticate(filterContext.getRequest)
        val isSecure = "https" == uriInfo.getRequestUri.getScheme
        filterContext.setSecurityContext(Authorizer(user, isSecure))
      case path =>
        log debug s"no authentication required for path='$path'"
    }
  }

  def unauthenticatedResponse(reason: String): Response = {
    Response.status(401)
      .`type`(MediaType.APPLICATION_JSON)
      .entity(Map("reason" -> reason))
      .header(WwwAuthenticate, s"$basicPrefix realm=$burstRealm").build
  }

  def userMustAuthenticate(reason: String = "Credentials Required"): Nothing = {
    throw new WebApplicationException(unauthenticatedResponse(reason))
  }

  private def authenticate(request: Request): User = {
    var authentication: String = request.asInstanceOf[ContainerRequest].getHeaderString(HttpHeaders.AUTHORIZATION)
    if (authentication == null) {
      userMustAuthenticate()
    }
    if (!authentication.startsWith(basicPrefix)) {
      return null
    }
    authentication = authentication.stripPrefix(basicPrefix)
    val values: Array[String] = new String(Base64.decodeBase64(authentication), StandardCharsets.UTF_8).split(":")
    if (values.length < 2) {
      userMustAuthenticate()
    }
    val username = values(0)
    val password = values(1)
    if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
      userMustAuthenticate()
    }
    catalog.verifyAccount(username, password) match {
      case Failure(t) =>
        log debug s"SecurityFilter failure $t"
        userMustAuthenticate(t.getMessage)
      case Success(account) =>
        val roles = account.labels
          .flatMap(_.get("roles"))
          .map(_.split(","))
          .getOrElse(Array.empty)
        User(account.moniker, roles)
    }
  }

}
