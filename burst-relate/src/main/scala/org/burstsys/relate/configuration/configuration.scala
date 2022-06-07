/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.relate

import org.burstsys.vitals.logging._
import org.burstsys.vitals.properties._

package object configuration extends VitalsLogger  with VitalsPropertyRegistry {

  val burstRelateDebugProperty: VitalsPropertySpecification[Boolean] = VitalsPropertySpecification[Boolean](
    key = "burst.relate.debug",
    description = "enable debug mode for relate",
    default = Some(false)
  )

  val burstRelateMysqlConnectionOpts: VitalsPropertySpecification[String] = VitalsPropertySpecification[String](
    key = "burst.relate.mysql.conn.opts",
    description = "options to append to the jdbc connection string",
    default = Some("")
  )

  val burstRelateDerbyConnectionOpts: VitalsPropertySpecification[String] = VitalsPropertySpecification[String](
    key = "burst.relate.derby.conn.opts",
    description = "options to append to the jdbc connection string",
    default = Some("create=true")
  )
}
