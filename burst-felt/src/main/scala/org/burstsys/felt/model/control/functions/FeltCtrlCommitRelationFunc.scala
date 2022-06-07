/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.control.functions

/**
 * Stop processing all the current member in scope as defined by a given '''relationship''' scalar vector, or
 * map  'path' argument.
 * Do '''not''' throw any in-scope transactional data
 * <ul>
 * <li>'''scope:''' the scope of this control verb is the highest level 'member' being processed at the provided path </li>
 * <li>'''cube data:''' current state is maintained but all subsequent updates are ignored </li>
 * <li>'''other collectors such as routes, tablets:''' current state is maintained but all subsequent updates are ignored </li>
 * <li>'''global or local variables:''' current state is maintained but all subsequent updates are ignored </li>
 * </ul>
 */
object FeltCtrlCommitRelationFunc {
  final val functionName: String = "commitRelation"
}

trait FeltCtrlCommitRelationFunc extends FeltCtrlVerbFunc {

  final override val nodeName = "felt-commit-relation-call"

  final override val functionName: String = FeltCtrlCommitRelationFunc.functionName

  final override val isRelation: Boolean = true
  final override val isAbort: Boolean = false

  final override val usage: String =
    s"""
       |usage: $functionName(<relation_path>)
       |  stop processing the given scalar, vector, or map relationship (and all related visits) in a scalar, vector,
       |  or map relationship (current or at a level above the current). Keep any ''dirty'' (xactional)
       |  data collected in that scope
      """.stripMargin

}
