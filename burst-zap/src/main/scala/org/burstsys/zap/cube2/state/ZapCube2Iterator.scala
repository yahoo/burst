/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube2.state

import org.burstsys.zap.cube2.row.ZapCube2Row

trait ZapCube2Iterator extends Any with ZapCube2State {

  @inline final def foreachRow(body: ZapCube2Row => Unit): Unit = {
    var i = 0
    while (i < itemCount) {
      body(row(i))
      i += 1
    }
  }

}
