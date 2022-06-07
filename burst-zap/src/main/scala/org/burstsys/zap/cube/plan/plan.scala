/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.zap.cube

import org.burstsys.felt.model.collectors.cube.FeltCubeId

import scala.collection.mutable.ArrayBuffer

package object plan {

  type ZapCubeIdArray = Array[Array[FeltCubeId]]

  type ZapCubeNodeList = ArrayBuffer[ZapCubePlanNode]

}
