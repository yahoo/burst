/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.route.decl

package object visit {

  final case class FeltRouteVisitType(extension: String) {
    override def toString: String = extension

    def methodTag: String = extension.replaceAll("\\.", "_")
  }

  final val FeltRoutePathsVisit = FeltRouteVisitType("paths")
  final val FeltRouteStepsVisit = FeltRouteVisitType("paths.steps")
  final val FeltRouteCoursesVisit = FeltRouteVisitType("courses")

}
