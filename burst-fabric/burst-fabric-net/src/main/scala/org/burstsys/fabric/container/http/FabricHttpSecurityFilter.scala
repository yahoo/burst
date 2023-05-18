/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.container.http

import jakarta.inject.Inject
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.container.PreMatching
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Request
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.SecurityContext
import jakarta.ws.rs.core.UriInfo
import jakarta.ws.rs.ext.Provider
import org.glassfish.jersey.server.ContainerRequest

import java.security.Principal
import scala.util.Failure
import scala.util.Success
import scala.util.Try

class User(val username: String, val roles: Array[String]) extends Principal {
  override def getName: String = username
}

case class Authorizer(user: User, secure: Boolean) extends SecurityContext {

  def getUserPrincipal: Principal = user

  def isUserInRole(role: String): Boolean = user.roles.contains(role)

  def isSecure: Boolean = secure

  def getAuthenticationScheme: String = SecurityContext.BASIC_AUTH

}

trait FabricAuthorizationProvider {

  def authorize(authHeader: String): Try[User]

  def unauthorizedResponseHeaders: Map[String, String]

  def isPathPublic(path: String): Boolean

}

/**
 * Determine if the user has presented valid credentials
 */
@Provider
@PreMatching
class FabricHttpSecurityFilter extends ContainerRequestFilter {

  @Inject
  var uriInfo: UriInfo = _

  @Inject
  var authorizer: FabricAuthorizationProvider = _

  private final val basicPrefix = "Basic "

  /**
   * Ensure that users are authenticated on protected paths
   */
  def filter(filterContext: ContainerRequestContext): Unit = {
    uriInfo.getPath match {
      case "logout" => userMustAuthenticate()
      case "status" => // health check URL that doesn't require auth
      case path if authorizer.isPathPublic(path) => // a path for public consumption
      case protectedPath =>
        val user = authenticate(filterContext.getRequest)
        val isSecure = "https" == uriInfo.getRequestUri.getScheme
        filterContext.setSecurityContext(Authorizer(user, isSecure))
    }
  }

  def unauthenticatedResponse(reason: String): Response = {
    val r = Response.status(401)
      .`type`(MediaType.APPLICATION_JSON)
      .entity(Map("reason" -> reason))
    authorizer.unauthorizedResponseHeaders.foreach(h => r.header(h._1, h._2))
    r.build
  }

  def userMustAuthenticate(reason: String = "Credentials Required"): Nothing = {
    throw new WebApplicationException(unauthenticatedResponse(reason))
  }

  private def authenticate(request: Request): User = {
    val authentication: String = request.asInstanceOf[ContainerRequest].getHeaderString(HttpHeaders.AUTHORIZATION)
    if (authentication == null) {
      userMustAuthenticate()
    }
    authorizer.authorize(authentication) match {
      case Failure(t) =>
        log debug s"SecurityFilter failure $t"
        userMustAuthenticate(t.getMessage)
      case Success(user) =>
        user
    }
  }

}
