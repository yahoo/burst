/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.reference

import org.burstsys.brio.model.schema.tree.BrioNode
import org.burstsys.felt.model.analysis.decl.FeltAnalysisDecl
import org.burstsys.felt.model.brio.decl.FeltBrioStdDecl
import org.burstsys.felt.model.reference.path.{FeltPathExpr, FeltSimplePath}
import org.burstsys.felt.model.tree._
import org.burstsys.vitals.logging.VitalsLogger

import java.util.concurrent.atomic.AtomicInteger

package object names extends VitalsLogger {

  final val debugNames = false

  val ROOT_NAME = "ROOT"

  /**
   * special character to mark anonymous scopes
   */
  val anonymousScopeMarker = "@"

  /**
   * add namespace global info to the [[FeltGlobal]]
   */
  trait FeltNameSpaceGlobal extends AnyRef {

    /**
     * allow for unique anonymous namespaces
     */
    private
    val _anonymousScopeIndex = new AtomicInteger()

    final
    def newAnonymousName: String = s"$anonymousScopeMarker${_anonymousScopeIndex.incrementAndGet}"

  }

  /**
   * all nodes that define a namespace scope i.e. have a 'name' implement
   * this trait and provide that name...
   */
  trait FeltNamedNode extends FeltNode {
    /**
     * the '''name''' of this node as used in its namespace
     *
     * @return
     */
    def nsName: String
  }

  final implicit
  class FeltNameSpaceRules(analysis: FeltAnalysisDecl) extends FeltTreeRules {

    /**
     * starting at the [[FeltNode]] root provided, setup a hierarchical
     * name space tree and annotate the [[FeltTree]] as appropriate.
     */
    def wireNameSpace(): Unit = {

      /**
       * the root name space is just a holder for two subtrees - the analysis and schema namespace trees
       */
      val rootNs = FeltNameSpace()
      analysis.nameSpace = rootNs
      analysis.global.rootNameSpace = rootNs

      // the 'analysis' subtree
      val analysisNs = FeltNameSpace(analysis, rootNs)
      rootNs += analysisNs
      wireAnalysisTree(analysis, analysisNs)

      // the 'schema' subtree
      wireSchemaTree(analysis.global.feltSchema.rootNode, rootNs)

    }

    /**
     * create a peer namespace/reference subtree for the analysis structure
     *
     * @param parentNode
     * @param parentNs
     * @return
     */
    private
    def wireAnalysisTree(parentNode: FeltNode, parentNs: FeltNameSpace): Unit = {
      parentNode.children foreach {
        case namedChildNode: FeltNamedNode =>
          namedChildNode.nameSpace = FeltNameSpace(namedChildNode, parentNs)
          parentNs += namedChildNode
          parentNs += namedChildNode.nameSpace
          wireAnalysisTree(namedChildNode, namedChildNode.nameSpace)
        case childNode: FeltNode =>
          parentNs += childNode
          childNode.nameSpace = parentNs
          wireAnalysisTree(childNode, parentNs)
        case _ => ???
      }
    }

    /**
     * create a peer namespace/reference subtree for the BRIO schema/lattice world...
     *
     * @param schemaNode
     * @param parentNameSpace
     * @return
     */
    private
    def wireSchemaTree(schemaNode: BrioNode, parentNameSpace: FeltNameSpace): Array[FeltNameSpace] = {
      val stdDecl = new FeltBrioStdDecl {
        final override val refName: FeltPathExpr = FeltSimplePath(schemaNode.relation.relationName)
        final override val nsName: String = schemaNode.relation.relationName
        final override val brioNode: BrioNode = schemaNode
        global = analysis.global
      }
      val childNameSpace = FeltNameSpace(stdDecl, parentNameSpace)
      parentNameSpace += childNameSpace
      stdDecl.nameSpace = childNameSpace
      stdDecl.refName.sync(stdDecl)
      analysis.global.linker.nominate(stdDecl) // we add to linker here instead of a scan of the tree
      //      log info s"WIRE_SCHEMA_TREE( parent=${parentNameSpace.shortName} child=${stdDecl.nsName})"
      Array(childNameSpace) ++ schemaNode.children.flatMap(wireSchemaTree(_, childNameSpace))
    }

  }

}
