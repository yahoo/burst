/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.eql

import org.burstsys.eql.generators.ActionPhase.ActionPhase
import org.burstsys.eql.generators.DeclarationScope.DeclarationScope
import org.burstsys.eql.generators.HydraVisitLanes.{PHASED, SITU, VisitType}
import org.burstsys.eql.generators.hydra.actions.{BooleanExpressionGenerators, SymbolAccessorGenerators, ValueExpressionGenerators}
import org.burstsys.eql.generators.hydra.analysis.AnalysisGeneratorImpl
import org.burstsys.eql.generators.hydra.utils._
import org.burstsys.eql.paths.PathAccessorGenerators
import org.burstsys.eql.paths.VisitPath
import org.burstsys.eql.planning.EqlExpression
import org.burstsys.eql.planning.lanes._
import org.burstsys.eql.planning.queries.Query
import org.burstsys.motif.common.DataType
import org.burstsys.motif.motif.tree.constant.Constant
import org.burstsys.motif.motif.tree.data.{ParameterAccessor, PathAccessor}
import org.burstsys.motif.motif.tree.eql.funnels.BoundaryBooleanExpression
import org.burstsys.motif.motif.tree.expression.Expression
import org.burstsys.motif.motif.tree.logical._
import org.burstsys.motif.motif.tree.values._
import org.burstsys.motif.paths.schemas.RelationType
import org.burstsys.motif.schema.model.MotifSchema

import scala.annotation.tailrec
import scala.jdk.CollectionConverters._
import scala.language.implicitConversions

package object generators extends BooleanExpressionGenerators with ValueExpressionGenerators with
  PathAccessorGenerators with SymbolAccessorGenerators {

  trait CubeDeclarationGenerator {
    /**
     * Generate Hydra codeblock for the cube declaration
     *
     * @return string of source
     */
    def generateCubeDeclarationSource(): CodeBlock
  }

  trait SourceGenerator extends Any {
    def generateSource()(implicit context: GlobalContext): CodeBlock

    def getDeclarations(scope: DeclarationScope)(implicit context: GlobalContext): Array[Declaration] = Array.empty

    def generateDeclarationsSource(scope: DeclarationScope)(implicit context: GlobalContext): CodeBlock =
      CodeBlock { implicit cb =>
        getDeclarations(scope).groupBy(_.name).values.foreach(_.head.generateDeclarationSource().source())
      }
  }

  trait BlockGenerator extends Any {
    /**
     * Generate Hydra for the analyzed tree
     *
     * @return string of source
     */
    def generateSource()(implicit context: GlobalContext): String
  }

  object BlockGenerator {
    def apply(analysis: Query)(implicit globalContext: GlobalContext): BlockGenerator =
      new AnalysisGeneratorImpl(analysis)
  }

  trait AnalysisSourceGenerator extends SourceGenerator

  trait VisitSourceGenerator extends SourceGenerator {
    def path: VisitPath
  }

  trait LaneSourceGenerator extends SourceGenerator {
    def name: LaneName

    def path: VisitPath

    // Lanes further partition into the phase of the visit
    def generateSource(actionPhase: ActionPhase)(implicit context: GlobalContext): CodeBlock

    // so the generic generate is disabled
    final override def generateSource()(implicit context: GlobalContext): CodeBlock = {
      throw new IllegalStateException()
    }
  }

  trait LaneActionsSourceGenerator {
    // Generate the action block once the control code has allowed it
    def generateSource(actionPhase: ActionPhase, actions: Iterable[ActionSourceGenerator])(implicit context: GlobalContext): CodeBlock = {
      CodeBlock { implicit cb =>
        actions.foreach(a => a.generateSource().source())
      }
    }
  }

  object ActionPhase extends Enumeration {
    type ActionPhase = Value
    val Before, Pre, Post, After, Situ = Value
  }

  trait ActionSourceGenerator extends SourceGenerator {
    def phase(): ActionPhase = ActionPhase.Pre

    def providesDimensionWrite: Boolean = false
  }

  final case class ActionSourceGeneratorProxy(action: ActionSourceGenerator, override val phase: ActionPhase) extends ActionSourceGenerator {
    override  def providesDimensionWrite: Boolean = action.providesDimensionWrite

    def generateSource()(implicit context: GlobalContext): CodeBlock = action.generateSource()

    override def getDeclarations(scope: DeclarationScope)(implicit context: GlobalContext): Array[Declaration] = action.getDeclarations(scope)

    override def generateDeclarationsSource(scope: DeclarationScope)(implicit context: GlobalContext): CodeBlock = action.generateDeclarationsSource(scope)
  }

  @tailrec
  implicit def toActionGenerator(exp: Expression): ActionSourceGenerator = {
    exp match {
      case a: ActionSourceGenerator =>
        a
      case eew: EqlExpression =>
        toActionGenerator(eew.self)
      case bb: BinaryBooleanExpression =>
        new BinaryBooleanSourceGenerator(bb)
      case ub: UnaryBooleanExpression =>
        new UnaryBooleanSourceGenerator(ub)
      case vb: BooleanValueExpression =>
        new BooleanValueSourceGenerator(vb)
      case bv: BinaryValueExpression =>
        new BinaryValueExpressionSourceGenerator(bv)
      case uv: UnaryValueExpression =>
        new UnaryValueSourceGenerator(uv)
      case p: PathAccessor =>
        new PathAccessorSourceGenerator(p)
      case pa: ParameterAccessor =>
        new ParameterAccessorSourceGenerator(pa)
      case c: Constant =>
        new ConstantSourceGenerator(c)
      case n: NowValueExpression =>
        new NowValueSourceGenerator(n)
      case vc: ValueComparisonBooleanExpression =>
        new ValueComparisonBooleanExpressionSourceGenerator(vc)
      case dto: DateTimeOrdinalExpression =>
        new DateTimeOrdinalSourceGenerator(dto)
      case dtq: DateTimeQuantumExpression =>
        new DateTimeQuantumSourceGenerator(dtq)
      case dtc: DateTimeConversionExpression =>
        new DateTimeConversionSourceGenerator(dtc)
      case cv: CastValueExpression =>
        new CastValueExpressionSourceGenerator(cv)
      case nte: NullTestBooleanExpression =>
        new NullTestBooleanExpressionSourceGenerator(nte)
      case bte: BoundsTestBooleanExpression =>
        new BoundsTestBooleanExpressionSourceGenerator(bte)
      case bbe: BoundaryBooleanExpression =>
        new NoopBooleanExpressionSourceGenerator({
          if (bbe.getType == BoundaryBooleanExpression.Type.END)
            ActionPhase.Post
          else
            ActionPhase.Pre
        })
      case mte: ExplicitMembershipTestBooleanExpression =>
        new ExplicitMembershipTestBooleanExpressionSourceGenerator(mte)
      case vte: VectorMembershipTestBooleanExpression =>
        ???  //TODO need parameter support
      case fe:  FunctionExpression =>
        chooseGenerator(fe)
      case d =>
        throw new UnsupportedOperationException(s"Expression ${d.getClass} not reccognized")
    }
  }

  def quantumFunctionToHydraDecl(function: DateTimeQuantumOperatorType): String = {
    function match {
      case DateTimeQuantumOperatorType.YEAR => "yearGrain"
      case DateTimeQuantumOperatorType.HALF => "halfGrain"
      case DateTimeQuantumOperatorType.QUARTER => "quarterGrain"
      case DateTimeQuantumOperatorType.MONTH => "monthGrain"
      case DateTimeQuantumOperatorType.WEEK => "weekGrain"
      case DateTimeQuantumOperatorType.DAY => "dayGrain"
      case DateTimeQuantumOperatorType.HOUR => "hourGrain"
      case DateTimeQuantumOperatorType.MINUTE => "minuteGrain"
      case DateTimeQuantumOperatorType.SECOND => "secondGrain"
    }
  }

  def ordinalFunctionToHydraDecl(function: DateTimeOrdinalOperatorType): String = {
    function match {
      case DateTimeOrdinalOperatorType.SECONDOFMINUTE => "secondOfMinuteOrdinal"
      case DateTimeOrdinalOperatorType.MINUTEOFHOUR => "minuteOfHourOrdinal"
      case DateTimeOrdinalOperatorType.HOUROFDAY => "hourOfDayOrdinal"
      case DateTimeOrdinalOperatorType.DAYOFWEEK => "dayOfWeekOrdinal"
      case DateTimeOrdinalOperatorType.DAYOFMONTH => "dayOfMonthOrdinal"
      case DateTimeOrdinalOperatorType.DAYOFYEAR => "dayOfYearOrdinal"
      case DateTimeOrdinalOperatorType.WEEKOFYEAR => "weekOfYearOrdinal"
      case DateTimeOrdinalOperatorType.MONTHOFYEAR => "monthOfYearOrdinal"
      case DateTimeOrdinalOperatorType.THEYEAR => "yearOfEraOrdinal"
    }
  }

  def conversionFunctionToHydraDecl(function: DateTimeConversionOperatorType): String = {
    function match {
      case DateTimeConversionOperatorType.YEARS => "yearTicks"
      case DateTimeConversionOperatorType.MONTHS => "monthTicks"
      case DateTimeConversionOperatorType.WEEKS => "weekTicks"
      case DateTimeConversionOperatorType.DAYS => "dayTicks"
      case DateTimeConversionOperatorType.HOURS => "hourTicks"
      case DateTimeConversionOperatorType.MINUTES => "minuteTicks"
      case DateTimeConversionOperatorType.SECONDS => "secondTicks"
    }
  }

  def typeToHydraTypeDecl(datatype: DataType): String = {
    datatype match {
      case DataType.DATETIME => DataType.LONG.toString.toLowerCase()
      case _ => datatype.toString.toLowerCase
    }
  }

  object DeclarationScope extends Enumeration {
    type DeclarationScope = Value
    val Analysis, Lane, Visit, Frame = Value
  }

  trait Declaration {
    // val name: String, val scope: DeclarationScope
    def name: String

    def scope: DeclarationScope

    def generateDeclarationSource(): CodeBlock
  }

  trait Collector extends Declaration

  abstract class NavigatingDeclaration(val name: String, val scope: DeclarationScope) extends Declaration {
    def selectHydraVisitLaneType(path: VisitPath)(implicit schema: MotifSchema): VisitType
  }

  case class SchemaDeclaration(override val scope: DeclarationScope, schema: MotifSchema)
    extends NavigatingDeclaration(schema.getSchemaName, scope) {
    override def selectHydraVisitLaneType(path: VisitPath)(implicit schema: MotifSchema): VisitType = {
      val visitType =
        if (path.isRoot && path.getPathAsString == schema.getRootFieldName)
          RelationType.REFERENCE_SCALAR
        else {
          if (schema.getRelationPathMap.asScala.contains(path.getPathAsString))
            schema.getRelationPathMap.asScala(path.getPathAsString).getRelationType
          else {
            assert(false)
          }
        }

      visitType match {
        case RelationType.VALUE_MAP | RelationType.VALUE_SCALAR | RelationType.VALUE_VECTOR =>
          SITU
        case RelationType.REFERENCE_SCALAR | RelationType.REFERENCE_VECTOR =>
          PHASED
        case RelationType.INSTANCE =>
          throw new IllegalArgumentException(s"$visitType not expected")
        case RelationType.TARGET =>
          throw new IllegalArgumentException(s"$visitType not expected")
      }
    }

    override def generateDeclarationSource(): CodeBlock = ???
  }

  case class Var(val name: String, val scope: DeclarationScope, varType: DataType, nulled: Boolean = false)
    extends Declaration {
    def generateDeclarationSource(): CodeBlock = varType match {
      case DataType.BOOLEAN =>
        s"var $name:${typeToHydraTypeDecl(varType)}=${if (nulled) "null" else "false"}"
      case _ =>
        s"var $name:${typeToHydraTypeDecl(varType)}=${if (nulled) "null" else "0"}"
    }
  }

}

