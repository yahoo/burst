/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.net

import org.burstsys.vitals.logging._

package object receiver extends VitalsLogger {

  /**
    * handle message events associated with a fabric network client
    */
  trait FabricNetClientMsgListener extends Any {

  }

  /**
    * handle message events associated with a fabric network server
    */
  trait FabricNetServerMsgListener extends Any {

  }

}
