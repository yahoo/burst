/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model

import org.burstsys.felt.model.analysis.decl.FeltAnalysisDecl
import org.burstsys.felt.model.collectors.decl.FeltCollectorDecl
import org.burstsys.felt.model.visits.decl.FeltStaticVisitDecl

/**
 * A [[FeltFrameDecl]] is one of a series of individual ''questions'' that can be
 * asked in a [[FeltAnalysisDecl]]. They are executed in parallel during the single
 * pass scan. This package contains the model for [[FeltCollectorDecl]]
 * and [[FeltStaticVisitDecl]] since they are contained
 * within a analytic frame.
 */
package object frame {


  trait FeltFrameElement {

    /**
     * the assigned frame Id
     *
     * @return
     */
    def frameId: Int

    /**
     * the assigned frame name
     *
     * @return
     */
    def frameName: String

  }

}
