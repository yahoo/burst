/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.dash.endpoints

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.burstsys.vitals.json.VitalsJsonSanatizers._
import org.burstsys.vitals.logging._

package object catalog extends VitalsLogger {

  final case class CatalogTreeNodeJson(pk: Long,
                                       @JsonSerialize(using = classOf[Values]) moniker: String,
                                       @JsonSerialize(using = classOf[Values]) udk: String,
                                       var children: Array[CatalogTreeNodeJson] = Array.empty
                                      ) extends ClientJsonObject

}
