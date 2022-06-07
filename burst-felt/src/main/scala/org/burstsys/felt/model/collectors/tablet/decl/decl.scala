/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.collectors.tablet

package object decl {

  final case class FeltTabletVisitType(extension: String) {
    override def toString: String = extension

    def methodTag: String = extension.replaceAll("\\.", "_")
  }

  final val FeltTabletMembersVisit = FeltTabletVisitType("members")

}
