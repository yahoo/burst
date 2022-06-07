/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.fabric.data.model.slice.region

import java.util.concurrent.ArrayBlockingQueue

import org.burstsys.tesla.parcel.TeslaParcel
import org.burstsys.vitals.logging._

package object writer extends VitalsLogger {

  final val regionParcelWriteQueueSize: Int = 10e3.toInt

  type FabricRegionParcelWriteQueue = ArrayBlockingQueue[TeslaParcel]

}
