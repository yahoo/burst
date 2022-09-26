/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.endpoints.catalog


import org.burstsys.dash.application.BurstDashEndpointBase
import org.burstsys.dash.endpoints._
import jakarta.ws.rs._
import jakarta.ws.rs.core.MediaType

/**
  * The rest endpoint for performing catalog operations.
  *
  * The actual operations are implemented in the mixed-in traits.
  */
@Path(CatalogApiPath)
@Consumes(Array(MediaType.APPLICATION_FORM_URLENCODED))
@Produces(Array(MediaType.APPLICATION_JSON))
final class BurstDashCatalogRest extends BurstDashEndpointBase
  with BurstDashCatalogSearch with BurstDashCatalogLookup with BurstDashCatalogUpdate
  with BurstDashCatalogNew with BurstDashCatalogDelete with BurstDashCatalogMotif  {

}
