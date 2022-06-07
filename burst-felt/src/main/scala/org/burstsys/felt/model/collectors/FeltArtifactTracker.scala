/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors

import scala.collection.mutable

final case
class FeltArtifactTracker() {

  final val useageMap = new mutable.HashSet[String]

  def +=(usage: String): String = {
    useageMap += usage
    usage
  }

  def isActive(usage: String): Boolean = {
    useageMap.contains(usage)
  }

}
