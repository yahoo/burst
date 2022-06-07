/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.schema.decl

import org.burstsys.felt.model.FeltDeclaration
import org.burstsys.felt.model.reference.path.FeltPathExpr
import org.burstsys.felt.model.schema.FeltSchema
import org.burstsys.felt.model.tree.code._
import org.burstsys.felt.model.tree.source._
import org.burstsys.felt.model.tree.{FeltLocation, FeltNode}

import scala.language.postfixOps
import scala.reflect.ClassTag

trait FeltSchemaDecl extends FeltDeclaration {

  final override val nodeName: String = "felt-schema-decl"

  def feltSchema: FeltSchema = _feltSchema

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // PRIVATE STATE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private[this]
  final lazy val _feltSchema: FeltSchema = FeltSchema(global.brioSchema, schemaExtensions)

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NODE API
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * the name of the schema
   *
   * @return
   */
  def schemaName: FeltPathExpr

  /**
   * the parsed set of schema extensions
   *
   * @return
   */
  def schemaExtensions: Array[FeltSchemaExtension]

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // TREE OPS
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def treeApply[R: ClassTag](rule: FeltNode => Array[R]): Array[R] = rule(this)

  final override
  def children: Array[_ <: FeltNode] = schemaName.asArray

  final override
  def canInferTypes: Boolean = schemaName.canInferTypes

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // STATIC REDUCTION
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def reduceStatics: FeltSchemaDecl = new FeltSchemaDecl {
    sync(FeltSchemaDecl.this)
    final override val location: FeltLocation = FeltSchemaDecl.this.location
    final override val feltSchema = FeltSchemaDecl.this.feltSchema
    final override val schemaName: FeltPathExpr = FeltSchemaDecl.this.schemaName.reduceStatics.resolveTypes
    final override val schemaExtensions: Array[FeltSchemaExtension] = {
      FeltSchemaDecl.this.schemaExtensions.map(_.reduceStatics.resolveTypes)
    }
  }

  final override
  def resolveTypes: this.type = {
    schemaName.resolveTypes
    this
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // NORMALIZED SOURCE GEN
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final override
  def normalizedSource(implicit index: Int): String = {
    val extensions = if (schemaExtensions.isEmpty) FeltNoCode else {
      schemaExtensions.map(_.normalizedSource).mkString(s"${S1}{\n${S3}", s"\n${S3}", s"\n${S2}}")
    }
    s"""${S1}schema ${schemaName.fullPath} $extensions"""
  }

}
