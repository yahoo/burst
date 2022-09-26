/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.alloy.store.mini

import org.burstsys.vitals.logging._
import org.burstsys.vitals.text.VitalsTextCodec

package object supervisor extends VitalsLogger {

  implicit val text: VitalsTextCodec = VitalsTextCodec() // OK

}
