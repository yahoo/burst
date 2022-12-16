/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.supervisor.http.endpoints

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.core.EntityTag
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.Response.Status
import org.burstsys.vitals

@Path("/")
class WaveSupervisorHtmlAssetEndpoint extends WaveSupervisorEndpoint {
  private final val INDEX_HTML = "/static/burst.html"
  private final val etag = new EntityTag(vitals.git.commitId)

  @GET
  final def home(): Response = {
    Response.status(Response.Status.MOVED_PERMANENTLY)
      .header(HttpHeaders.LOCATION, "/ui/")
      .build()
  }

  @GET
  @Path("/ui{route: .*}")
  final def uiAppBundle(): Response = {
    log.debug(s"ui route: '${uriInfo.getAbsolutePath}'")
    serve(INDEX_HTML)
  }

  @GET
  @Path("/static/{resource: .*}")
  final def staticResource(): Response = {
    log.debug(s"static resource: '${uriInfo.getAbsolutePath}'")
    serve(uriInfo.getPath)
  }

  private def serve(path: String): Response = {
    val p: String = s"/${path.stripPrefix("/")}"
    log debug p
    resultOrErrorResponse {
      val stream = getClass.getResourceAsStream(p)
      if (stream != null) {
        val builder = request.evaluatePreconditions(etag) match {
          case null => Response.status(Status.OK).`type`(mediaTypeFor(path)).entity(stream).tag(etag)
          case builder => builder
        }
        builder.build()
      } else {
        log warn s"missing resource: '$p'"
        Response.status(Status.NOT_FOUND).build()
      }
    }
  }

  private def mediaTypeFor(path: String): String = {
    val index = path.lastIndexOf(".")
    if (index != -1)
      path.substring(index) match {
        case ".css" => "text/css"
        case ".js" => "application/javascript"
        case ".ico" => "image/x-icon"
        case ".jpg" => "image/jpeg"
        case ".png" => "image/png"
        case ".svg" => "image/svg+xml"
        case ".woff" => "font/woff"
        case ".woff2" => "font/woff2"
        case ".ttf" => "font/truetype"
        case ".otf" => "font/opentype"
        case _ => MediaType.TEXT_HTML
      }
    else
      MediaType.TEXT_HTML
  }

}

