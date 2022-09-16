/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.felt.model.schema.traveler

import org.burstsys.brio.model.schema.BrioSchema
import org.burstsys.brio.types.BrioPath.BrioPathKey
import org.burstsys.felt.FeltReporter
import org.burstsys.felt.compile.FeltCompileEngine
import org.burstsys.felt.compile.artifact.{FeltArtifact, FeltArtifactKey, FeltArtifactTag, FeltArtifactory}
import org.burstsys.felt.model.runtime.FeltRuntime
import org.burstsys.felt.model.sweep.FeltSweep
import org.burstsys.felt.model.tree.FeltGlobal
import org.burstsys.felt.model.tree.code.FeltCodeCursor
import org.burstsys.vitals.VitalsService.{VitalsServiceModality, VitalsSingleton}

import scala.util.{Failure, Success}

/**
 * A FELT schema 'traveler' is the underlying code generated traversal of a specific
 * Brio Schema object tree data model
 */
trait FeltTraveler[R <: FeltRuntime] extends AnyRef {

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // PRIVATE STATE
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  final private[this]
  var _artifact: FeltArtifact[_] = _

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // API
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * The actual scala 'classname' of the generated traveler class
   *
   * @return
   */
  def travelerClassName: String

  /**
   * the actual scala 'classname' of the generated schema runtime base type
   *
   * @return
   */
  def runtimeClassName: String

  /**
   * The Brio Schema associated with this Felt Schema
   *
   * @return
   */
  def brioSchema: BrioSchema

  final def artifact: FeltArtifact[_] = _artifact

  final def artifact_=(artifact: FeltArtifact[_]): Unit = _artifact = artifact

  /**
   * this is called to scan traverse a brio object tree blob
   *
   * @param runtime the per traversal runtime support structure
   * @param sweep   the sweep to run through this scan traversal
   */
  def apply(runtime: R, sweep: FeltSweep): Unit

  /**
   * Is the given path in the scope (on axis and below in the traversal)
   * defined by a second root path key
   *
   * @param visitKey
   * @param scopeKey
   * @return
   */
  def visitInScope(visitKey: BrioPathKey, scopeKey: BrioPathKey): Boolean = true

}

object FeltTraveler extends FeltArtifactory[FeltGlobal, FeltTravelerArtifact] {

  override def modality: VitalsServiceModality = VitalsSingleton

  override def serviceName: String = s"felt-traveler-artifactory"

  override val lruEnabled: Boolean = false

  override protected def maxCount: Int = 10

  implicit val index: Int = 0

  override protected def onCacheHit(): Unit = FeltReporter.recordTravelerCacheHit()

  override def createArtifact(key: FeltArtifactKey, tag: FeltArtifactTag, global: FeltGlobal): FeltTravelerArtifact =
    FeltTravelerArtifact(key, tag, global)

  override val artifactName = "FeltSchema"

  def apply(global: FeltGlobal): String = {
    val tag = global.feltSchema.name
    fetchArtifact(key = tag, tag = tag, input = global).travelerClassName
  }

  protected override
  def generateContent(artifact: FeltTravelerArtifact): Unit = {
    startIfNotRunning
    val generationStart = System.nanoTime
    try {
      val generator = FeltTravelerGenerator(
        travelerClassName = artifact.travelerClassName,
        runtimeClassName = artifact.runtimeClassName,
        brioSchema = artifact.input.brioSchema
      )
      artifact.generatedSource = generator.generateTravelerCode(FeltCodeCursor(artifact.input, rangeId = 1))
    } finally FeltReporter.recordTravelerGenerate(System.nanoTime - generationStart)

    val compilationStart = System.nanoTime
    try {
      FeltCompileEngine.generatedSourceToTravelerClassNames(artifact.key, artifact.tag, artifact.generatedSource) match {
        case Failure(t) =>
          throw t
        case Success(r) =>
          r.filter(!_.contains(runtimeSuffix)).head
      }
    } finally FeltReporter.recordTravelerCompile(System.nanoTime - compilationStart, artifact.generatedSource)

  }

  // -------- generated felt 'traveler' for brio schema 'Unity' WITH_LEXICON) -----------------
  // -------- traveler specific abstract base runtime class  ----------------------------------
  trait BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime extends org.burstsys.felt.model.runtime.FeltRuntime {
    // -------- lattice variable declarations ---------------------------------------------------
    final var lattice_user_relation: org.burstsys.brio.lattice.BrioLatticeReference = org.burstsys.brio.lattice.BrioLatticeReference();
    final var lattice_user_relation_isNull: Boolean = true;
    final var lattice_user_application_relation: org.burstsys.brio.lattice.BrioLatticeReference = org.burstsys.brio.lattice.BrioLatticeReference();
    final var lattice_user_application_relation_isNull: Boolean = true;
    final var lattice_user_application_channels_relation: org.burstsys.brio.lattice.BrioLatticeReference = org.burstsys.brio.lattice.BrioLatticeReference();
    final var lattice_user_application_channels_relation_isNull: Boolean = true;
    final var lattice_user_application_channels_vector_is_first: Boolean = false;
    final var lattice_user_application_channels_vector_is_last: Boolean = false;
    final var lattice_user_application_channels_parameters_relation_isNull: Boolean = true;
    final var lattice_user_application_channels_parameters_value_map_key: scala.Short = _;
    final var lattice_user_application_channels_parameters_value_map_value: scala.Short = _;
    final var lattice_user_application_channels_parameters_vector_is_first: Boolean = false;
    final var lattice_user_application_channels_parameters_vector_is_last: Boolean = false;
    final var lattice_user_application_parameters_relation_isNull: Boolean = true;
    final var lattice_user_application_parameters_value_map_key: scala.Short = _;
    final var lattice_user_application_parameters_value_map_value: scala.Short = _;
    final var lattice_user_application_parameters_vector_is_first: Boolean = false;
    final var lattice_user_application_parameters_vector_is_last: Boolean = false;
    final var lattice_user_application_firstUse_relation: org.burstsys.brio.lattice.BrioLatticeReference = org.burstsys.brio.lattice.BrioLatticeReference();
    final var lattice_user_application_firstUse_relation_isNull: Boolean = true;
    final var lattice_user_application_firstUse_appVersion_relation: org.burstsys.brio.lattice.BrioLatticeReference = org.burstsys.brio.lattice.BrioLatticeReference();
    final var lattice_user_application_firstUse_appVersion_relation_isNull: Boolean = true;
    final var lattice_user_application_lastUse_relation: org.burstsys.brio.lattice.BrioLatticeReference = org.burstsys.brio.lattice.BrioLatticeReference();
    final var lattice_user_application_lastUse_relation_isNull: Boolean = true;
    final var lattice_user_application_lastUse_appVersion_relation: org.burstsys.brio.lattice.BrioLatticeReference = org.burstsys.brio.lattice.BrioLatticeReference();
    final var lattice_user_application_lastUse_appVersion_relation_isNull: Boolean = true;
    final var lattice_user_application_mostUse_relation: org.burstsys.brio.lattice.BrioLatticeReference = org.burstsys.brio.lattice.BrioLatticeReference();
    final var lattice_user_application_mostUse_relation_isNull: Boolean = true;
    final var lattice_user_application_mostUse_appVersion_relation: org.burstsys.brio.lattice.BrioLatticeReference = org.burstsys.brio.lattice.BrioLatticeReference();
    final var lattice_user_application_mostUse_appVersion_relation_isNull: Boolean = true;
    final var lattice_user_sessions_relation: org.burstsys.brio.lattice.BrioLatticeReference = org.burstsys.brio.lattice.BrioLatticeReference();
    final var lattice_user_sessions_relation_isNull: Boolean = true;
    final var lattice_user_sessions_vector_is_first: Boolean = false;
    final var lattice_user_sessions_vector_is_last: Boolean = false;
    final var lattice_user_sessions_events_relation: org.burstsys.brio.lattice.BrioLatticeReference = org.burstsys.brio.lattice.BrioLatticeReference();
    final var lattice_user_sessions_events_relation_isNull: Boolean = true;
    final var lattice_user_sessions_events_vector_is_first: Boolean = false;
    final var lattice_user_sessions_events_vector_is_last: Boolean = false;
    final var lattice_user_sessions_events_parameters_relation_isNull: Boolean = true;
    final var lattice_user_sessions_events_parameters_value_map_key: scala.Short = _;
    final var lattice_user_sessions_events_parameters_value_map_value: scala.Short = _;
    final var lattice_user_sessions_events_parameters_vector_is_first: Boolean = false;
    final var lattice_user_sessions_events_parameters_vector_is_last: Boolean = false;
    final var lattice_user_sessions_variants_relation: org.burstsys.brio.lattice.BrioLatticeReference = org.burstsys.brio.lattice.BrioLatticeReference();
    final var lattice_user_sessions_variants_relation_isNull: Boolean = true;
    final var lattice_user_sessions_variants_vector_is_first: Boolean = false;
    final var lattice_user_sessions_variants_vector_is_last: Boolean = false;
    final var lattice_user_sessions_parameters_relation_isNull: Boolean = true;
    final var lattice_user_sessions_parameters_value_map_key: scala.Short = _;
    final var lattice_user_sessions_parameters_value_map_value: scala.Short = _;
    final var lattice_user_sessions_parameters_vector_is_first: Boolean = false;
    final var lattice_user_sessions_parameters_vector_is_last: Boolean = false;
    final var lattice_user_sessions_appVersion_relation: org.burstsys.brio.lattice.BrioLatticeReference = org.burstsys.brio.lattice.BrioLatticeReference();
    final var lattice_user_sessions_appVersion_relation_isNull: Boolean = true;
    final var lattice_user_interests_relation_isNull: Boolean = true;
    final var lattice_user_interests_value_vector_value: scala.Long = _;
    final var lattice_user_interests_value_vector_value_is_null: Boolean = true;
    final var lattice_user_interests_vector_is_first: Boolean = false;
    final var lattice_user_interests_vector_is_last: Boolean = false;
    final var lattice_user_traits_relation: org.burstsys.brio.lattice.BrioLatticeReference = org.burstsys.brio.lattice.BrioLatticeReference();
    final var lattice_user_traits_relation_isNull: Boolean = true;
    final var lattice_user_traits_vector_is_first: Boolean = false;
    final var lattice_user_traits_vector_is_last: Boolean = false;
    final var lattice_user_parameters_relation_isNull: Boolean = true;
    final var lattice_user_parameters_value_map_key: scala.Short = _;
    final var lattice_user_parameters_value_map_value: scala.Short = _;
    final var lattice_user_parameters_vector_is_first: Boolean = false;
    final var lattice_user_parameters_vector_is_last: Boolean = false;
  }

  class BF03EEE56C3CC4047833A822C1D58BCA2_lexicon extends org.burstsys.felt.model.schema.traveler.FeltTraveler[BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime] {
    final def travelerClassName: String = "BF03EEE56C3CC4047833A822C1D58BCA2_lexicon";

    final def runtimeClassName: String = "BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime";
    final val brioSchema = org.burstsys.brio.model.schema.BrioSchema("Unity");
    // -------- schematic references ------------------------------------------------------------
    final val schema_structure_Use_V1_schematic: org.burstsys.brio.model.schema.encoding.BrioSchematic = brioSchema.schematic(56, 1);
    final val schema_structure_AppVersion_V1_schematic: org.burstsys.brio.model.schema.encoding.BrioSchematic = brioSchema.schematic(57, 1);
    final val schema_structure_Channel_V1_schematic: org.burstsys.brio.model.schema.encoding.BrioSchematic = brioSchema.schematic(58, 1);
    final val schema_structure_Event_V1_schematic: org.burstsys.brio.model.schema.encoding.BrioSchematic = brioSchema.schematic(59, 1);
    final val schema_structure_Application_V1_schematic: org.burstsys.brio.model.schema.encoding.BrioSchematic = brioSchema.schematic(60, 1);
    final val schema_structure_User_V1_schematic: org.burstsys.brio.model.schema.encoding.BrioSchematic = brioSchema.schematic(61, 1);
    final val schema_structure_Trait_V1_schematic: org.burstsys.brio.model.schema.encoding.BrioSchematic = brioSchema.schematic(62, 1);
    final val schema_structure_Variant_V1_schematic: org.burstsys.brio.model.schema.encoding.BrioSchematic = brioSchema.schematic(63, 1);
    final val schema_structure_Session_V1_schematic: org.burstsys.brio.model.schema.encoding.BrioSchematic = brioSchema.schematic(64, 1);

    // -------- called per brio blob traversed with analysis-specific parallel 'sweep' (reentrant) and 'runtime' (one per thread)
    @inline final def apply(runtime: BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep): Unit = {
      val reader = runtime.reader;
      latticeVarInitialize(runtime);
      val rootPathKey = brioSchema.rootNode.pathKey;
      sweep.rootSplice(runtime, rootPathKey, 15); // TraverseCommencePlace
      { // BEGIN reference scalar tunnels for 'user'
        runtime.lattice_user_application_channels_parameters_relation_isNull = true; // reset
        runtime.lattice_user_application_channels_relation_isNull = true; // reset
        runtime.lattice_user_application_firstUse_appVersion_relation_isNull = true; // reset
        runtime.lattice_user_application_firstUse_relation_isNull = true; // reset
        runtime.lattice_user_application_lastUse_appVersion_relation_isNull = true; // reset
        runtime.lattice_user_application_lastUse_relation_isNull = true; // reset
        runtime.lattice_user_application_parameters_relation_isNull = true; // reset
        runtime.lattice_user_application_mostUse_appVersion_relation_isNull = true; // reset
        runtime.lattice_user_application_mostUse_relation_isNull = true; // reset
        runtime.lattice_user_application_relation_isNull = true; // reset
        runtime.lattice_user_sessions_appVersion_relation_isNull = true; // reset
        runtime.lattice_user_sessions_parameters_relation_isNull = true; // reset
        runtime.lattice_user_sessions_events_parameters_relation_isNull = true; // reset
        runtime.lattice_user_sessions_events_relation_isNull = true; // reset
        runtime.lattice_user_sessions_variants_relation_isNull = true; // reset
        runtime.lattice_user_sessions_relation_isNull = true; // reset
        runtime.lattice_user_interests_relation_isNull = true; // reset
        runtime.lattice_user_parameters_relation_isNull = true; // reset
        runtime.lattice_user_traits_relation_isNull = true; // reset
        runtime.lattice_user_relation.versionKey(reader) match {
          case 1 => { // schema version 1
            tunnel_v1_user_application(runtime, sweep, reader);
          }
        }
      } // END reference scalar tunnels for 'user'
      { // BEGIN reference scalar relations for 'user'
        runtime.lattice_user_relation.versionKey(reader) match {
          case 1 => { // schema version 1
            search_v1_user(runtime, sweep, reader);
          }
        }
      } // END reference scalar relations for 'user'
      sweep.rootSplice(runtime, rootPathKey, 16); // TraverseCompletePlace
    }

    // -------- lattice variable initialization -------------------------------------------------
    @inline
    def latticeVarInitialize(runtime: BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime): Unit = {
      runtime.lattice_user_relation = runtime.lattice; // init root of lattice...
      runtime.lattice_user_relation_isNull = false;
    }

    // -------- support test for path in scope (on axis and below) ------------------------------
    @inline override
    def visitInScope(visitKey: Int, scopeKey: Int): Boolean = {
      if (visitKey == -1 || scopeKey == -1) return false;
      visitKey match {
        case 1 => // VISIT: user
          scopeKey match {
            case 1 // SCOPE: user
            => true
            case _ => false
          }
        case 3 => // VISIT: user.application
          scopeKey match {
            case 1 // SCOPE: user
                 | 3 // SCOPE: user.application
            => true
            case _ => false
          }
        case 5 => // VISIT: user.application.channels
          scopeKey match {
            case 1 // SCOPE: user
                 | 3 // SCOPE: user.application
                 | 5 // SCOPE: user.application.channels
            => true
            case _ => false
          }
        case 7 => // VISIT: user.application.channels.parameters
          scopeKey match {
            case 1 // SCOPE: user
                 | 3 // SCOPE: user.application
                 | 5 // SCOPE: user.application.channels
                 | 7 // SCOPE: user.application.channels.parameters
            => true
            case _ => false
          }
        case 10 => // VISIT: user.application.parameters
          scopeKey match {
            case 1 // SCOPE: user
                 | 3 // SCOPE: user.application
                 | 10 // SCOPE: user.application.parameters
            => true
            case _ => false
          }
        case 11 => // VISIT: user.application.firstUse
          scopeKey match {
            case 1 // SCOPE: user
                 | 3 // SCOPE: user.application
                 | 11 // SCOPE: user.application.firstUse
            => true
            case _ => false
          }
        case 13 => // VISIT: user.application.firstUse.appVersion
          scopeKey match {
            case 1 // SCOPE: user
                 | 3 // SCOPE: user.application
                 | 11 // SCOPE: user.application.firstUse
                 | 13 // SCOPE: user.application.firstUse.appVersion
            => true
            case _ => false
          }
        case 32 => // VISIT: user.application.lastUse
          scopeKey match {
            case 1 // SCOPE: user
                 | 3 // SCOPE: user.application
                 | 32 // SCOPE: user.application.lastUse
            => true
            case _ => false
          }
        case 34 => // VISIT: user.application.lastUse.appVersion
          scopeKey match {
            case 1 // SCOPE: user
                 | 3 // SCOPE: user.application
                 | 32 // SCOPE: user.application.lastUse
                 | 34 // SCOPE: user.application.lastUse.appVersion
            => true
            case _ => false
          }
        case 53 => // VISIT: user.application.mostUse
          scopeKey match {
            case 1 // SCOPE: user
                 | 3 // SCOPE: user.application
                 | 53 // SCOPE: user.application.mostUse
            => true
            case _ => false
          }
        case 55 => // VISIT: user.application.mostUse.appVersion
          scopeKey match {
            case 1 // SCOPE: user
                 | 3 // SCOPE: user.application
                 | 53 // SCOPE: user.application.mostUse
                 | 55 // SCOPE: user.application.mostUse.appVersion
            => true
            case _ => false
          }
        case 74 => // VISIT: user.sessions
          scopeKey match {
            case 1 // SCOPE: user
                 | 74 // SCOPE: user.sessions
            => true
            case _ => false
          }
        case 76 => // VISIT: user.sessions.events
          scopeKey match {
            case 1 // SCOPE: user
                 | 74 // SCOPE: user.sessions
                 | 76 // SCOPE: user.sessions.events
            => true
            case _ => false
          }
        case 78 => // VISIT: user.sessions.events.parameters
          scopeKey match {
            case 1 // SCOPE: user
                 | 74 // SCOPE: user.sessions
                 | 76 // SCOPE: user.sessions.events
                 | 78 // SCOPE: user.sessions.events.parameters
            => true
            case _ => false
          }
        case 83 => // VISIT: user.sessions.variants
          scopeKey match {
            case 1 // SCOPE: user
                 | 74 // SCOPE: user.sessions
                 | 83 // SCOPE: user.sessions.variants
            => true
            case _ => false
          }
        case 86 => // VISIT: user.sessions.parameters
          scopeKey match {
            case 1 // SCOPE: user
                 | 74 // SCOPE: user.sessions
                 | 86 // SCOPE: user.sessions.parameters
            => true
            case _ => false
          }
        case 101 => // VISIT: user.sessions.appVersion
          scopeKey match {
            case 1 // SCOPE: user
                 | 74 // SCOPE: user.sessions
                 | 101 // SCOPE: user.sessions.appVersion
            => true
            case _ => false
          }
        case 113 => // VISIT: user.interests
          scopeKey match {
            case 1 // SCOPE: user
                 | 113 // SCOPE: user.interests
            => true
            case _ => false
          }
        case 114 => // VISIT: user.traits
          scopeKey match {
            case 1 // SCOPE: user
                 | 114 // SCOPE: user.traits
            => true
            case _ => false
          }
        case 117 => // VISIT: user.parameters
          scopeKey match {
            case 1 // SCOPE: user
                 | 117 // SCOPE: user.parameters
            => true
            case _ => false
          }
        case _ => ???
      }
    }

    // -------- traveler search methods ---------------------------------------------------------
    @inline final def tunnel_v1_user_application(runtime: BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader: org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      // -------- tunnel scalar reference path='user.application:3' version='1' ordinal=1 ---------
      if (sweep.skipTunnelPath(3) || runtime.lattice_user_relation.relationIsNull(reader, schema_structure_User_V1_schematic, 1)) {
        runtime.lattice_user_application_relation_isNull = true;
      } else {
        runtime.lattice_user_application_relation_isNull = false;
        runtime.lattice_user_application_relation = runtime.lattice_user_relation.referenceScalar(reader, schema_structure_User_V1_schematic, 1);
        runtime.lattice_user_application_channels_parameters_relation_isNull = true; // reset
        runtime.lattice_user_application_channels_relation_isNull = true; // reset
        runtime.lattice_user_application_firstUse_appVersion_relation_isNull = true; // reset
        runtime.lattice_user_application_firstUse_relation_isNull = true; // reset
        runtime.lattice_user_application_lastUse_appVersion_relation_isNull = true; // reset
        runtime.lattice_user_application_lastUse_relation_isNull = true; // reset
        runtime.lattice_user_application_parameters_relation_isNull = true; // reset
        runtime.lattice_user_application_mostUse_appVersion_relation_isNull = true; // reset
        runtime.lattice_user_application_mostUse_relation_isNull = true; // reset
        runtime.lattice_user_application_relation.versionKey(reader) match {
          case 1 => { // schema version 1
            tunnel_v1_user_application_firstUse(runtime, sweep, reader);
            tunnel_v1_user_application_lastUse(runtime, sweep, reader);
            tunnel_v1_user_application_mostUse(runtime, sweep, reader);
          }
        }
      }
    }

    @inline final def search_v1_user(runtime: BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader: org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      sweep.referenceScalarSplice(runtime, 1, 6) // user InstanceAllocPlace
      sweep.referenceScalarSplice(runtime, 1, 1) // user InstancePrePlace
      // -------- START CHILD RELATION VISITS (BEF|E POST) 'user'  -------------------------------
      // -------- visit static child relation(s) before dynamic ones  -----------------------------
      // -------- START reference-scalar path='user.application:3' version=1 ordinal=1 ------------
      if (sweep.skipVisitPath(3) || runtime.lattice_user_relation.relationIsNull(reader, schema_structure_User_V1_schematic, 1)) {
        if (sweep.skipTunnelPath(3)) {
          runtime.lattice_user_application_relation_isNull = true;
        }
      } else {
        runtime.lattice_user_application_relation_isNull = false;
        runtime.lattice_user_application_relation = runtime.lattice_user_relation.referenceScalar(reader, schema_structure_User_V1_schematic, 1);
        { // BEGIN reference scalar tunnels for 'user.application'
          runtime.lattice_user_application_channels_parameters_relation_isNull = true; // reset
          runtime.lattice_user_application_channels_relation_isNull = true; // reset
          runtime.lattice_user_application_firstUse_appVersion_relation_isNull = true; // reset
          runtime.lattice_user_application_firstUse_relation_isNull = true; // reset
          runtime.lattice_user_application_lastUse_appVersion_relation_isNull = true; // reset
          runtime.lattice_user_application_lastUse_relation_isNull = true; // reset
          runtime.lattice_user_application_parameters_relation_isNull = true; // reset
          runtime.lattice_user_application_mostUse_appVersion_relation_isNull = true; // reset
          runtime.lattice_user_application_mostUse_relation_isNull = true; // reset
          runtime.lattice_user_application_relation.versionKey(reader) match {
            case 1 => { // schema version 1
              tunnel_v1_user_application_firstUse(runtime, sweep, reader);
              tunnel_v1_user_application_lastUse(runtime, sweep, reader);
              tunnel_v1_user_application_mostUse(runtime, sweep, reader);
            }
          }
        } // END reference scalar tunnels for 'user.application'
        { // BEGIN reference scalar relations for 'user.application'
          runtime.lattice_user_application_relation.versionKey(reader) match {
            case 1 => { // schema version 1
              search_v1_user_application(runtime, sweep, reader);
            }
          }
        } // END reference scalar relations for 'user.application'
      } // END reference-scalar path='user.application:3' version=1 ordinal=1
      sweep.referenceScalarSplice(runtime, 3, 8) // user.application ChildMergePlace
      // -------- START reference-vector path='user.sessions:74' version=1 ordinal=2 --------------
      if (sweep.skipVisitPath(74) || runtime.lattice_user_relation.relationIsNull(reader, schema_structure_User_V1_schematic, 2)) {
        runtime.lattice_user_sessions_relation_isNull = true;
      } else {
        iterate_refvec_v1_user_sessions(runtime, sweep, reader);
      } // END reference-vector path='user.sessions:74' version=1 ordinal=2
      sweep.referenceScalarSplice(runtime, 74, 8) // user.sessions ChildMergePlace
      // -------- START value-vector path='user.interests:113' version=1 ordinal=3 ----------------
      if (sweep.skipVisitPath(113) || runtime.lattice_user_relation.relationIsNull(reader, schema_structure_User_V1_schematic, 3)) {
        runtime.lattice_user_interests_relation_isNull = true;
      } else {
        iterate_valvec_v1_user_interests(runtime, sweep, reader);
      } // END value-vector path='user.interests:113' version=1 ordinal=3
      sweep.referenceScalarSplice(runtime, 113, 8) // user.interests ChildMergePlace
      // -------- START value-map path='user.parameters:117' version=1 ordinal=5 ------------------
      if (sweep.skipVisitPath(117) || runtime.lattice_user_relation.relationIsNull(reader, schema_structure_User_V1_schematic, 5)) {
        runtime.lattice_user_parameters_relation_isNull = true;
      } else {
        iterate_valmap_v1_user_parameters(runtime, sweep, reader);
      } // END value-map path='user.parameters:117' version=1 ordinal=5
      sweep.referenceScalarSplice(runtime, 117, 8) // user.parameters ChildMergePlace
      // -------- START reference-vector path='user.traits:114' version=1 ordinal=4 ---------------
      if (sweep.skipVisitPath(114) || runtime.lattice_user_relation.relationIsNull(reader, schema_structure_User_V1_schematic, 4)) {
        runtime.lattice_user_traits_relation_isNull = true;
      } else {
        iterate_refvec_v1_user_traits(runtime, sweep, reader);
      } // END reference-vector path='user.traits:114' version=1 ordinal=4
      sweep.referenceScalarSplice(runtime, 114, 8) // user.traits ChildMergePlace
      // -------- visit dynamic child relation(s) after static ones  ------------------------------
      sweep.dynamicRelationSplices(runtime, 1, 17); // 'user' 'DynamicVisitPlace'
      // -------- END CHILD RELATION VISITS 'user' ------------------------------------------------
      sweep.referenceScalarSplice(runtime, 1, 2) // user InstancePostPlace
      // -------- START CHILD RELATION JOINS (AFTER POST) 'user' ----------------------------------
      // -------- join static child relation(s) before dynamic ones  ------------------------------
      sweep.referenceScalarSplice(runtime, 3, 9) // user.application ChildJoinPlace
      sweep.referenceScalarSplice(runtime, 74, 9) // user.sessions ChildJoinPlace
      sweep.referenceScalarSplice(runtime, 113, 9) // user.interests ChildJoinPlace
      sweep.referenceScalarSplice(runtime, 117, 9) // user.parameters ChildJoinPlace
      sweep.referenceScalarSplice(runtime, 114, 9) // user.traits ChildJoinPlace
      // -------- join dynamic child relation(s) after static ones  -------------------------------
      sweep.dynamicRelationSplices(runtime, 1, 18); // 'user'  'DynamicJoinPlace'
      // -------- END CHILD RELATION JOINS 'user' -------------------------------------------------
      // -------- DYNAMIC RELATIONS CLEANUP 'user' ------------------------------------------------
      sweep.dynamicRelationSplices(runtime, 1, 19); // 'user'  'DynamicCleanupPlace'
      sweep.referenceScalarSplice(runtime, 1, 7) // user InstanceFreePlace
    }

    @inline final def tunnel_v1_user_application_firstUse(runtime: BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader: org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      // -------- tunnel scalar reference path='user.application.firstUse:11' version='1' ordinal=3
      if (sweep.skipTunnelPath(11) || runtime.lattice_user_application_relation.relationIsNull(reader, schema_structure_Application_V1_schematic, 3)) {
        runtime.lattice_user_application_firstUse_relation_isNull = true;
      } else {
        runtime.lattice_user_application_firstUse_relation_isNull = false;
        runtime.lattice_user_application_firstUse_relation = runtime.lattice_user_application_relation.referenceScalar(reader, schema_structure_Application_V1_schematic, 3);
        runtime.lattice_user_application_firstUse_appVersion_relation_isNull = true; // reset
        runtime.lattice_user_application_firstUse_relation.versionKey(reader) match {
          case 1 => { // schema version 1
            tunnel_v1_user_application_firstUse_appVersion(runtime, sweep, reader);
          }
        }
      }
    }

    @inline final def tunnel_v1_user_application_lastUse(runtime: BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader: org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      // -------- tunnel scalar reference path='user.application.lastUse:32' version='1' ordinal=4
      if (sweep.skipTunnelPath(32) || runtime.lattice_user_application_relation.relationIsNull(reader, schema_structure_Application_V1_schematic, 4)) {
        runtime.lattice_user_application_lastUse_relation_isNull = true;
      } else {
        runtime.lattice_user_application_lastUse_relation_isNull = false;
        runtime.lattice_user_application_lastUse_relation = runtime.lattice_user_application_relation.referenceScalar(reader, schema_structure_Application_V1_schematic, 4);
        runtime.lattice_user_application_lastUse_appVersion_relation_isNull = true; // reset
        runtime.lattice_user_application_lastUse_relation.versionKey(reader) match {
          case 1 => { // schema version 1
            tunnel_v1_user_application_lastUse_appVersion(runtime, sweep, reader);
          }
        }
      }
    }

    @inline final def tunnel_v1_user_application_mostUse(runtime: BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader: org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      // -------- tunnel scalar reference path='user.application.mostUse:53' version='1' ordinal=5
      if (sweep.skipTunnelPath(53) || runtime.lattice_user_application_relation.relationIsNull(reader, schema_structure_Application_V1_schematic, 5)) {
        runtime.lattice_user_application_mostUse_relation_isNull = true;
      } else {
        runtime.lattice_user_application_mostUse_relation_isNull = false;
        runtime.lattice_user_application_mostUse_relation = runtime.lattice_user_application_relation.referenceScalar(reader, schema_structure_Application_V1_schematic, 5);
        runtime.lattice_user_application_mostUse_appVersion_relation_isNull = true; // reset
        runtime.lattice_user_application_mostUse_relation.versionKey(reader) match {
          case 1 => { // schema version 1
            tunnel_v1_user_application_mostUse_appVersion(runtime, sweep, reader);
          }
        }
      }
    }

    @inline final def search_v1_user_application(runtime: BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader: org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      sweep.referenceScalarSplice(runtime, 3, 6) // user.application InstanceAllocPlace
      sweep.referenceScalarSplice(runtime, 3, 1) // user.application InstancePrePlace
      // -------- START CHILD RELATION VISITS (BEF|E POST) 'user.application'  -------------------
      // -------- visit static child relation(s) before dynamic ones  -----------------------------
      // -------- START reference-vector path='user.application.channels:5' version=1 ordinal=1 ---
      if (sweep.skipVisitPath(5) || runtime.lattice_user_application_relation.relationIsNull(reader, schema_structure_Application_V1_schematic, 1)) {
        runtime.lattice_user_application_channels_relation_isNull = true;
      } else {
        iterate_refvec_v1_user_application_channels(runtime, sweep, reader);
      } // END reference-vector path='user.application.channels:5' version=1 ordinal=1
      sweep.referenceScalarSplice(runtime, 5, 8) // user.application.channels ChildMergePlace
      // -------- START reference-scalar path='user.application.firstUse:11' version=1 ordinal=3 --
      if (sweep.skipVisitPath(11) || runtime.lattice_user_application_relation.relationIsNull(reader, schema_structure_Application_V1_schematic, 3)) {
        if (sweep.skipTunnelPath(11)) {
          runtime.lattice_user_application_firstUse_relation_isNull = true;
        }
      } else {
        runtime.lattice_user_application_firstUse_relation_isNull = false;
        runtime.lattice_user_application_firstUse_relation = runtime.lattice_user_application_relation.referenceScalar(reader, schema_structure_Application_V1_schematic, 3);
        { // BEGIN reference scalar tunnels for 'user.application.firstUse'
          runtime.lattice_user_application_firstUse_appVersion_relation_isNull = true; // reset
          runtime.lattice_user_application_firstUse_relation.versionKey(reader) match {
            case 1 => { // schema version 1
              tunnel_v1_user_application_firstUse_appVersion(runtime, sweep, reader);
            }
          }
        } // END reference scalar tunnels for 'user.application.firstUse'
        { // BEGIN reference scalar relations for 'user.application.firstUse'
          runtime.lattice_user_application_firstUse_relation.versionKey(reader) match {
            case 1 => { // schema version 1
              search_v1_user_application_firstUse(runtime, sweep, reader);
            }
          }
        } // END reference scalar relations for 'user.application.firstUse'
      } // END reference-scalar path='user.application.firstUse:11' version=1 ordinal=3
      sweep.referenceScalarSplice(runtime, 11, 8) // user.application.firstUse ChildMergePlace
      // -------- START reference-scalar path='user.application.lastUse:32' version=1 ordinal=4 ---
      if (sweep.skipVisitPath(32) || runtime.lattice_user_application_relation.relationIsNull(reader, schema_structure_Application_V1_schematic, 4)) {
        if (sweep.skipTunnelPath(32)) {
          runtime.lattice_user_application_lastUse_relation_isNull = true;
        }
      } else {
        runtime.lattice_user_application_lastUse_relation_isNull = false;
        runtime.lattice_user_application_lastUse_relation = runtime.lattice_user_application_relation.referenceScalar(reader, schema_structure_Application_V1_schematic, 4);
        { // BEGIN reference scalar tunnels for 'user.application.lastUse'
          runtime.lattice_user_application_lastUse_appVersion_relation_isNull = true; // reset
          runtime.lattice_user_application_lastUse_relation.versionKey(reader) match {
            case 1 => { // schema version 1
              tunnel_v1_user_application_lastUse_appVersion(runtime, sweep, reader);
            }
          }
        } // END reference scalar tunnels for 'user.application.lastUse'
        { // BEGIN reference scalar relations for 'user.application.lastUse'
          runtime.lattice_user_application_lastUse_relation.versionKey(reader) match {
            case 1 => { // schema version 1
              search_v1_user_application_lastUse(runtime, sweep, reader);
            }
          }
        } // END reference scalar relations for 'user.application.lastUse'
      } // END reference-scalar path='user.application.lastUse:32' version=1 ordinal=4
      sweep.referenceScalarSplice(runtime, 32, 8) // user.application.lastUse ChildMergePlace
      // -------- START value-map path='user.application.parameters:10' version=1 ordinal=2 -------
      if (sweep.skipVisitPath(10) || runtime.lattice_user_application_relation.relationIsNull(reader, schema_structure_Application_V1_schematic, 2)) {
        runtime.lattice_user_application_parameters_relation_isNull = true;
      } else {
        iterate_valmap_v1_user_application_parameters(runtime, sweep, reader);
      } // END value-map path='user.application.parameters:10' version=1 ordinal=2
      sweep.referenceScalarSplice(runtime, 10, 8) // user.application.parameters ChildMergePlace
      // -------- START reference-scalar path='user.application.mostUse:53' version=1 ordinal=5 ---
      if (sweep.skipVisitPath(53) || runtime.lattice_user_application_relation.relationIsNull(reader, schema_structure_Application_V1_schematic, 5)) {
        if (sweep.skipTunnelPath(53)) {
          runtime.lattice_user_application_mostUse_relation_isNull = true;
        }
      } else {
        runtime.lattice_user_application_mostUse_relation_isNull = false;
        runtime.lattice_user_application_mostUse_relation = runtime.lattice_user_application_relation.referenceScalar(reader, schema_structure_Application_V1_schematic, 5);
        { // BEGIN reference scalar tunnels for 'user.application.mostUse'
          runtime.lattice_user_application_mostUse_appVersion_relation_isNull = true; // reset
          runtime.lattice_user_application_mostUse_relation.versionKey(reader) match {
            case 1 => { // schema version 1
              tunnel_v1_user_application_mostUse_appVersion(runtime, sweep, reader);
            }
          }
        } // END reference scalar tunnels for 'user.application.mostUse'
        { // BEGIN reference scalar relations for 'user.application.mostUse'
          runtime.lattice_user_application_mostUse_relation.versionKey(reader) match {
            case 1 => { // schema version 1
              search_v1_user_application_mostUse(runtime, sweep, reader);
            }
          }
        } // END reference scalar relations for 'user.application.mostUse'
      } // END reference-scalar path='user.application.mostUse:53' version=1 ordinal=5
      sweep.referenceScalarSplice(runtime, 53, 8) // user.application.mostUse ChildMergePlace
      // -------- visit dynamic child relation(s) after static ones  ------------------------------
      sweep.dynamicRelationSplices(runtime, 3, 17); // 'user.application' 'DynamicVisitPlace'
      // -------- END CHILD RELATION VISITS 'user.application' ------------------------------------
      sweep.referenceScalarSplice(runtime, 3, 2) // user.application InstancePostPlace
      // -------- START CHILD RELATION JOINS (AFTER POST) 'user.application' ----------------------
      // -------- join static child relation(s) before dynamic ones  ------------------------------
      sweep.referenceScalarSplice(runtime, 5, 9) // user.application.channels ChildJoinPlace
      sweep.referenceScalarSplice(runtime, 11, 9) // user.application.firstUse ChildJoinPlace
      sweep.referenceScalarSplice(runtime, 32, 9) // user.application.lastUse ChildJoinPlace
      sweep.referenceScalarSplice(runtime, 10, 9) // user.application.parameters ChildJoinPlace
      sweep.referenceScalarSplice(runtime, 53, 9) // user.application.mostUse ChildJoinPlace
      // -------- join dynamic child relation(s) after static ones  -------------------------------
      sweep.dynamicRelationSplices(runtime, 3, 18); // 'user.application'  'DynamicJoinPlace'
      // -------- END CHILD RELATION JOINS 'user.application' -------------------------------------
      // -------- DYNAMIC RELATIONS CLEANUP 'user.application' ------------------------------------
      sweep.dynamicRelationSplices(runtime, 3, 19); // 'user.application'  'DynamicCleanupPlace'
      sweep.referenceScalarSplice(runtime, 3, 7) // user.application InstanceFreePlace
    }

    @inline final def iterate_refvec_v1_user_sessions(runtime: BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader: org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      runtime.lattice_user_sessions_relation_isNull = false;
      runtime.lattice_user_sessions_vector_is_first = false;
      runtime.lattice_user_sessions_vector_is_last = false;
      val memberIterator = runtime.lattice_user_relation.referenceVectorIterator(reader, schema_structure_User_V1_schematic, 2);
      var vectorIndex = memberIterator.start(reader);
      val memberCount = memberIterator.length(reader);
      sweep.referenceVectorSplice(runtime, 74, 10); // user.sessions VectorAllocPlace
      sweep.referenceVectorSplice(runtime, 74, 3); // user.sessions VectorBeforePlace
      var i = 0;
      while (i < memberCount && !runtime.skipControlMemberPath(74)) {
        runtime.lattice_user_sessions_vector_is_first = (i == 0);
        runtime.lattice_user_sessions_vector_is_last = i == (memberCount - 1);
        runtime.lattice_user_sessions_relation = memberIterator.member(reader, vectorIndex);
        sweep.referenceVectorMemberSplice(runtime, 74, 12); // user.sessions VectorMemberAllocPlace
        { // BEGIN reference scalar tunnels for 'user.sessions'
          runtime.lattice_user_sessions_appVersion_relation_isNull = true; // reset
          runtime.lattice_user_sessions_parameters_relation_isNull = true; // reset
          runtime.lattice_user_sessions_events_parameters_relation_isNull = true; // reset
          runtime.lattice_user_sessions_events_relation_isNull = true; // reset
          runtime.lattice_user_sessions_variants_relation_isNull = true; // reset
          runtime.lattice_user_sessions_relation.versionKey(reader) match {
            case 1 => { // schema version 1
              tunnel_v1_user_sessions_appVersion(runtime, sweep, reader);
            }
          }
        } // END reference scalar tunnels for 'user.sessions'
        { // BEGIN reference scalar relations for 'user.sessions'
          runtime.lattice_user_sessions_relation.versionKey(reader) match {
            case 1 => { // schema version 1
              search_v1_user_sessions(runtime, sweep, reader);
            }
          }
        } // END reference scalar relations for 'user.sessions'
        sweep.referenceVectorMemberSplice(runtime, 74, 14); // user.sessions VectorMemberMergePlace
        sweep.referenceVectorMemberSplice(runtime, 74, 13); // user.sessions VectorMemberFreePlace
        vectorIndex = memberIterator.advance(reader, vectorIndex);
        i += 1;
      }
      runtime.lattice_user_sessions_vector_is_first = false; // this should be false for 'after' splices
      runtime.lattice_user_sessions_vector_is_last = false; // this should be false for 'after' splices
      sweep.referenceVectorSplice(runtime, 74, 4); // user.sessions VectorAfterPlace
      sweep.referenceVectorSplice(runtime, 74, 11); // user.sessions VectorFreePlace
    }

    @inline final def iterate_valvec_v1_user_interests(runtime: BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader: org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      runtime.lattice_user_interests_relation_isNull = false;
      runtime.lattice_user_interests_vector_is_first = false;
      runtime.lattice_user_interests_vector_is_last = false;
      val memberIterator = runtime.lattice_user_relation.valueVectorIterator(reader, schema_structure_User_V1_schematic, 3);
      var memberOffset = memberIterator.start(reader)
      val memberCount = memberIterator.length(reader);
      sweep.valueVectorSplice(runtime, 113, 10); // user.interests VectorAllocPlace
      sweep.valueVectorSplice(runtime, 113, 3); // user.interests VectorBeforePlace
      var i = 0;
      while (i < memberCount && !runtime.skipControlMemberPath(113)) {
        runtime.lattice_user_interests_vector_is_first = (i == 0);
        runtime.lattice_user_interests_vector_is_last = i == (memberCount - 1);
        sweep.valueVectorMemberSplice(runtime, 113, 12) // user.interests VectorMemberAllocPlace
        // -------- data for value vector user.interests --------------------------------------------
        runtime.lattice_user_interests_value_vector_value = memberIterator.readLong(reader, memberOffset)
        runtime.lattice_user_interests_value_vector_value_is_null = false
        sweep.valueVectorMemberSplice(runtime, 113, 5) // user.interests VectorMemberSituPlace
        sweep.valueVectorMemberSplice(runtime, 113, 14) // user.interests VectorMemberMergePlace
        sweep.valueVectorMemberSplice(runtime, 113, 13) // user.interests VectorMemberFreePlace
        memberOffset = memberIterator.advance(reader, memberOffset, 6)
        i += 1;
      }
      runtime.lattice_user_interests_vector_is_first = false; // this should be false for 'after' splices
      runtime.lattice_user_interests_vector_is_last = false; // this should be false for 'after' splices
      sweep.valueVectorSplice(runtime, 113, 4) // user.interests VectorAfterPlace
      sweep.valueVectorSplice(runtime, 113, 11) // user.interests VectorFreePlace
    }

    @inline final def iterate_valmap_v1_user_parameters(runtime: BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader: org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      runtime.lattice_user_parameters_relation_isNull = false;
      runtime.lattice_user_parameters_vector_is_first = false;
      runtime.lattice_user_parameters_vector_is_last = false;
      val memberIterator = runtime.lattice_user_relation.valueMapIterator(reader, schema_structure_User_V1_schematic, 5);
      val memberCount = memberIterator.length(reader);
      sweep.valueMapSplice(runtime, 117, 10); // user.parameters VectorAllocPlace
      sweep.valueMapSplice(runtime, 117, 3); // user.parameters VectorBeforePlace
      var i = 0;
      while (i < memberCount && !runtime.skipControlMemberPath(117)) {
        runtime.lattice_user_parameters_vector_is_first = (i == 0);
        runtime.lattice_user_parameters_vector_is_last = i == (memberCount - 1);
        sweep.valueMapMemberSplice(runtime, 117, 12); // user.parameters VectorMemberAllocPlace
        // -------- current member key/value tuple for value map user.parameters --------------------
        runtime.lattice_user_parameters_value_map_key = memberIterator.readLexiconStringKey(i, reader);
        runtime.lattice_user_parameters_value_map_value = memberIterator.readLexiconStringStringValue(i, reader);
        sweep.valueMapMemberSplice(runtime, 117, 5); // user.parameters VectorMemberSituPlace
        sweep.valueMapMemberSplice(runtime, 117, 14); // user.parameters VectorMemberMergePlace
        sweep.valueMapMemberSplice(runtime, 117, 13); // user.parameters VectorMemberFreePlace
        i += 1;
      }
      runtime.lattice_user_parameters_vector_is_first = false; // this should be false for 'after' splices
      runtime.lattice_user_parameters_vector_is_last = false; // this should be false for 'after' splices
      sweep.valueMapSplice(runtime, 117, 4); // user.parameters VectorAfterPlace
      sweep.valueMapSplice(runtime, 117, 11); // user.parameters VectorFreePlace
    }

    @inline final def iterate_refvec_v1_user_traits(runtime: BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader: org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      runtime.lattice_user_traits_relation_isNull = false;
      runtime.lattice_user_traits_vector_is_first = false;
      runtime.lattice_user_traits_vector_is_last = false;
      val memberIterator = runtime.lattice_user_relation.referenceVectorIterator(reader, schema_structure_User_V1_schematic, 4);
      var vectorIndex = memberIterator.start(reader);
      val memberCount = memberIterator.length(reader);
      sweep.referenceVectorSplice(runtime, 114, 10); // user.traits VectorAllocPlace
      sweep.referenceVectorSplice(runtime, 114, 3); // user.traits VectorBeforePlace
      var i = 0;
      while (i < memberCount && !runtime.skipControlMemberPath(114)) {
        runtime.lattice_user_traits_vector_is_first = (i == 0);
        runtime.lattice_user_traits_vector_is_last = i == (memberCount - 1);
        runtime.lattice_user_traits_relation = memberIterator.member(reader, vectorIndex);
        sweep.referenceVectorMemberSplice(runtime, 114, 12); // user.traits VectorMemberAllocPlace
        { // BEGIN reference scalar relations for 'user.traits'
          runtime.lattice_user_traits_relation.versionKey(reader) match {
            case 1 => { // schema version 1
              search_v1_user_traits(runtime, sweep, reader);
            }
          }
        } // END reference scalar relations for 'user.traits'
        sweep.referenceVectorMemberSplice(runtime, 114, 14); // user.traits VectorMemberMergePlace
        sweep.referenceVectorMemberSplice(runtime, 114, 13); // user.traits VectorMemberFreePlace
        vectorIndex = memberIterator.advance(reader, vectorIndex);
        i += 1;
      }
      runtime.lattice_user_traits_vector_is_first = false; // this should be false for 'after' splices
      runtime.lattice_user_traits_vector_is_last = false; // this should be false for 'after' splices
      sweep.referenceVectorSplice(runtime, 114, 4); // user.traits VectorAfterPlace
      sweep.referenceVectorSplice(runtime, 114, 11); // user.traits VectorFreePlace
    }

    @inline final def tunnel_v1_user_application_firstUse_appVersion(runtime: BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader: org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      // -------- tunnel scalar reference path='user.application.firstUse.appVersion:13' version='1' ordinal=1
      if (sweep.skipTunnelPath(13) || runtime.lattice_user_application_firstUse_relation.relationIsNull(reader, schema_structure_Use_V1_schematic, 1)) {
        runtime.lattice_user_application_firstUse_appVersion_relation_isNull = true;
      } else {
        runtime.lattice_user_application_firstUse_appVersion_relation_isNull = false;
        runtime.lattice_user_application_firstUse_appVersion_relation = runtime.lattice_user_application_firstUse_relation.referenceScalar(reader, schema_structure_Use_V1_schematic, 1);
      }
    }

    @inline final def tunnel_v1_user_application_lastUse_appVersion(runtime: BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader: org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      // -------- tunnel scalar reference path='user.application.lastUse.appVersion:34' version='1' ordinal=1
      if (sweep.skipTunnelPath(34) || runtime.lattice_user_application_lastUse_relation.relationIsNull(reader, schema_structure_Use_V1_schematic, 1)) {
        runtime.lattice_user_application_lastUse_appVersion_relation_isNull = true;
      } else {
        runtime.lattice_user_application_lastUse_appVersion_relation_isNull = false;
        runtime.lattice_user_application_lastUse_appVersion_relation = runtime.lattice_user_application_lastUse_relation.referenceScalar(reader, schema_structure_Use_V1_schematic, 1);
      }
    }

    @inline final def tunnel_v1_user_application_mostUse_appVersion(runtime: BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader: org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      // -------- tunnel scalar reference path='user.application.mostUse.appVersion:55' version='1' ordinal=1
      if (sweep.skipTunnelPath(55) || runtime.lattice_user_application_mostUse_relation.relationIsNull(reader, schema_structure_Use_V1_schematic, 1)) {
        runtime.lattice_user_application_mostUse_appVersion_relation_isNull = true;
      } else {
        runtime.lattice_user_application_mostUse_appVersion_relation_isNull = false;
        runtime.lattice_user_application_mostUse_appVersion_relation = runtime.lattice_user_application_mostUse_relation.referenceScalar(reader, schema_structure_Use_V1_schematic, 1);
      }
    }

    @inline final def iterate_refvec_v1_user_application_channels(runtime: BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader: org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      runtime.lattice_user_application_channels_relation_isNull = false;
      runtime.lattice_user_application_channels_vector_is_first = false;
      runtime.lattice_user_application_channels_vector_is_last = false;
      val memberIterator = runtime.lattice_user_application_relation.referenceVectorIterator(reader, schema_structure_Application_V1_schematic, 1);
      var vectorIndex = memberIterator.start(reader);
      val memberCount = memberIterator.length(reader);
      sweep.referenceVectorSplice(runtime, 5, 10); // user.application.channels VectorAllocPlace
      sweep.referenceVectorSplice(runtime, 5, 3); // user.application.channels VectorBeforePlace
      var i = 0;
      while (i < memberCount && !runtime.skipControlMemberPath(5)) {
        runtime.lattice_user_application_channels_vector_is_first = (i == 0);
        runtime.lattice_user_application_channels_vector_is_last = i == (memberCount - 1);
        runtime.lattice_user_application_channels_relation = memberIterator.member(reader, vectorIndex);
        sweep.referenceVectorMemberSplice(runtime, 5, 12); // user.application.channels VectorMemberAllocPlace
        { // BEGIN reference scalar relations for 'user.application.channels'
          runtime.lattice_user_application_channels_relation.versionKey(reader) match {
            case 1 => { // schema version 1
              search_v1_user_application_channels(runtime, sweep, reader);
            }
          }
        } // END reference scalar relations for 'user.application.channels'
        sweep.referenceVectorMemberSplice(runtime, 5, 14); // user.application.channels VectorMemberMergePlace
        sweep.referenceVectorMemberSplice(runtime, 5, 13); // user.application.channels VectorMemberFreePlace
        vectorIndex = memberIterator.advance(reader, vectorIndex);
        i += 1;
      }
      runtime.lattice_user_application_channels_vector_is_first = false; // this should be false for 'after' splices
      runtime.lattice_user_application_channels_vector_is_last = false; // this should be false for 'after' splices
      sweep.referenceVectorSplice(runtime, 5, 4); // user.application.channels VectorAfterPlace
      sweep.referenceVectorSplice(runtime, 5, 11); // user.application.channels VectorFreePlace
    }

    @inline final def search_v1_user_application_firstUse(runtime: BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader: org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      sweep.referenceScalarSplice(runtime, 11, 6) // user.application.firstUse InstanceAllocPlace
      sweep.referenceScalarSplice(runtime, 11, 1) // user.application.firstUse InstancePrePlace
      // -------- START CHILD RELATION VISITS (BEF|E POST) 'user.application.firstUse'  ----------
      // -------- visit static child relation(s) before dynamic ones  -----------------------------
      // -------- START reference-scalar path='user.application.firstUse.appVersion:13' version=1 ordinal=1
      if (sweep.skipVisitPath(13) || runtime.lattice_user_application_firstUse_relation.relationIsNull(reader, schema_structure_Use_V1_schematic, 1)) {
        if (sweep.skipTunnelPath(13)) {
          runtime.lattice_user_application_firstUse_appVersion_relation_isNull = true;
        }
      } else {
        runtime.lattice_user_application_firstUse_appVersion_relation_isNull = false;
        runtime.lattice_user_application_firstUse_appVersion_relation = runtime.lattice_user_application_firstUse_relation.referenceScalar(reader, schema_structure_Use_V1_schematic, 1);
        { // BEGIN reference scalar relations for 'user.application.firstUse.appVersion'
          runtime.lattice_user_application_firstUse_appVersion_relation.versionKey(reader) match {
            case 1 => { // schema version 1
              search_v1_user_application_firstUse_appVersion(runtime, sweep, reader);
            }
          }
        } // END reference scalar relations for 'user.application.firstUse.appVersion'
      } // END reference-scalar path='user.application.firstUse.appVersion:13' version=1 ordinal=1
      sweep.referenceScalarSplice(runtime, 13, 8) // user.application.firstUse.appVersion ChildMergePlace
      // -------- visit dynamic child relation(s) after static ones  ------------------------------
      sweep.dynamicRelationSplices(runtime, 11, 17); // 'user.application.firstUse' 'DynamicVisitPlace'
      // -------- END CHILD RELATION VISITS 'user.application.firstUse' ---------------------------
      sweep.referenceScalarSplice(runtime, 11, 2) // user.application.firstUse InstancePostPlace
      // -------- START CHILD RELATION JOINS (AFTER POST) 'user.application.firstUse' -------------
      // -------- join static child relation(s) before dynamic ones  ------------------------------
      sweep.referenceScalarSplice(runtime, 13, 9) // user.application.firstUse.appVersion ChildJoinPlace
      // -------- join dynamic child relation(s) after static ones  -------------------------------
      sweep.dynamicRelationSplices(runtime, 11, 18); // 'user.application.firstUse'  'DynamicJoinPlace'
      // -------- END CHILD RELATION JOINS 'user.application.firstUse' ----------------------------
      // -------- DYNAMIC RELATIONS CLEANUP 'user.application.firstUse' ---------------------------
      sweep.dynamicRelationSplices(runtime, 11, 19); // 'user.application.firstUse'  'DynamicCleanupPlace'
      sweep.referenceScalarSplice(runtime, 11, 7) // user.application.firstUse InstanceFreePlace
    }

    @inline final def search_v1_user_application_lastUse(runtime: BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader: org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      sweep.referenceScalarSplice(runtime, 32, 6) // user.application.lastUse InstanceAllocPlace
      sweep.referenceScalarSplice(runtime, 32, 1) // user.application.lastUse InstancePrePlace
      // -------- START CHILD RELATION VISITS (BEF|E POST) 'user.application.lastUse'  -----------
      // -------- visit static child relation(s) before dynamic ones  -----------------------------
      // -------- START reference-scalar path='user.application.lastUse.appVersion:34' version=1 ordinal=1
      if (sweep.skipVisitPath(34) || runtime.lattice_user_application_lastUse_relation.relationIsNull(reader, schema_structure_Use_V1_schematic, 1)) {
        if (sweep.skipTunnelPath(34)) {
          runtime.lattice_user_application_lastUse_appVersion_relation_isNull = true;
        }
      } else {
        runtime.lattice_user_application_lastUse_appVersion_relation_isNull = false;
        runtime.lattice_user_application_lastUse_appVersion_relation = runtime.lattice_user_application_lastUse_relation.referenceScalar(reader, schema_structure_Use_V1_schematic, 1);
        { // BEGIN reference scalar relations for 'user.application.lastUse.appVersion'
          runtime.lattice_user_application_lastUse_appVersion_relation.versionKey(reader) match {
            case 1 => { // schema version 1
              search_v1_user_application_lastUse_appVersion(runtime, sweep, reader);
            }
          }
        } // END reference scalar relations for 'user.application.lastUse.appVersion'
      } // END reference-scalar path='user.application.lastUse.appVersion:34' version=1 ordinal=1
      sweep.referenceScalarSplice(runtime, 34, 8) // user.application.lastUse.appVersion ChildMergePlace
      // -------- visit dynamic child relation(s) after static ones  ------------------------------
      sweep.dynamicRelationSplices(runtime, 32, 17); // 'user.application.lastUse' 'DynamicVisitPlace'
      // -------- END CHILD RELATION VISITS 'user.application.lastUse' ----------------------------
      sweep.referenceScalarSplice(runtime, 32, 2) // user.application.lastUse InstancePostPlace
      // -------- START CHILD RELATION JOINS (AFTER POST) 'user.application.lastUse' --------------
      // -------- join static child relation(s) before dynamic ones  ------------------------------
      sweep.referenceScalarSplice(runtime, 34, 9) // user.application.lastUse.appVersion ChildJoinPlace
      // -------- join dynamic child relation(s) after static ones  -------------------------------
      sweep.dynamicRelationSplices(runtime, 32, 18); // 'user.application.lastUse'  'DynamicJoinPlace'
      // -------- END CHILD RELATION JOINS 'user.application.lastUse' -----------------------------
      // -------- DYNAMIC RELATIONS CLEANUP 'user.application.lastUse' ----------------------------
      sweep.dynamicRelationSplices(runtime, 32, 19); // 'user.application.lastUse'  'DynamicCleanupPlace'
      sweep.referenceScalarSplice(runtime, 32, 7) // user.application.lastUse InstanceFreePlace
    }

    @inline final def iterate_valmap_v1_user_application_parameters(runtime: BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader: org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      runtime.lattice_user_application_parameters_relation_isNull = false;
      runtime.lattice_user_application_parameters_vector_is_first = false;
      runtime.lattice_user_application_parameters_vector_is_last = false;
      val memberIterator = runtime.lattice_user_application_relation.valueMapIterator(reader, schema_structure_Application_V1_schematic, 2);
      val memberCount = memberIterator.length(reader);
      sweep.valueMapSplice(runtime, 10, 10); // user.application.parameters VectorAllocPlace
      sweep.valueMapSplice(runtime, 10, 3); // user.application.parameters VectorBeforePlace
      var i = 0;
      while (i < memberCount && !runtime.skipControlMemberPath(10)) {
        runtime.lattice_user_application_parameters_vector_is_first = (i == 0);
        runtime.lattice_user_application_parameters_vector_is_last = i == (memberCount - 1);
        sweep.valueMapMemberSplice(runtime, 10, 12); // user.application.parameters VectorMemberAllocPlace
        // -------- current member key/value tuple for value map user.application.parameters --------
        runtime.lattice_user_application_parameters_value_map_key = memberIterator.readLexiconStringKey(i, reader);
        runtime.lattice_user_application_parameters_value_map_value = memberIterator.readLexiconStringStringValue(i, reader);
        sweep.valueMapMemberSplice(runtime, 10, 5); // user.application.parameters VectorMemberSituPlace
        sweep.valueMapMemberSplice(runtime, 10, 14); // user.application.parameters VectorMemberMergePlace
        sweep.valueMapMemberSplice(runtime, 10, 13); // user.application.parameters VectorMemberFreePlace
        i += 1;
      }
      runtime.lattice_user_application_parameters_vector_is_first = false; // this should be false for 'after' splices
      runtime.lattice_user_application_parameters_vector_is_last = false; // this should be false for 'after' splices
      sweep.valueMapSplice(runtime, 10, 4); // user.application.parameters VectorAfterPlace
      sweep.valueMapSplice(runtime, 10, 11); // user.application.parameters VectorFreePlace
    }

    @inline final def search_v1_user_application_mostUse(runtime: BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader: org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      sweep.referenceScalarSplice(runtime, 53, 6) // user.application.mostUse InstanceAllocPlace
      sweep.referenceScalarSplice(runtime, 53, 1) // user.application.mostUse InstancePrePlace
      // -------- START CHILD RELATION VISITS (BEF|E POST) 'user.application.mostUse'  -----------
      // -------- visit static child relation(s) before dynamic ones  -----------------------------
      // -------- START reference-scalar path='user.application.mostUse.appVersion:55' version=1 ordinal=1
      if (sweep.skipVisitPath(55) || runtime.lattice_user_application_mostUse_relation.relationIsNull(reader, schema_structure_Use_V1_schematic, 1)) {
        if (sweep.skipTunnelPath(55)) {
          runtime.lattice_user_application_mostUse_appVersion_relation_isNull = true;
        }
      } else {
        runtime.lattice_user_application_mostUse_appVersion_relation_isNull = false;
        runtime.lattice_user_application_mostUse_appVersion_relation = runtime.lattice_user_application_mostUse_relation.referenceScalar(reader, schema_structure_Use_V1_schematic, 1);
        { // BEGIN reference scalar relations for 'user.application.mostUse.appVersion'
          runtime.lattice_user_application_mostUse_appVersion_relation.versionKey(reader) match {
            case 1 => { // schema version 1
              search_v1_user_application_mostUse_appVersion(runtime, sweep, reader);
            }
          }
        } // END reference scalar relations for 'user.application.mostUse.appVersion'
      } // END reference-scalar path='user.application.mostUse.appVersion:55' version=1 ordinal=1
      sweep.referenceScalarSplice(runtime, 55, 8) // user.application.mostUse.appVersion ChildMergePlace
      // -------- visit dynamic child relation(s) after static ones  ------------------------------
      sweep.dynamicRelationSplices(runtime, 53, 17); // 'user.application.mostUse' 'DynamicVisitPlace'
      // -------- END CHILD RELATION VISITS 'user.application.mostUse' ----------------------------
      sweep.referenceScalarSplice(runtime, 53, 2) // user.application.mostUse InstancePostPlace
      // -------- START CHILD RELATION JOINS (AFTER POST) 'user.application.mostUse' --------------
      // -------- join static child relation(s) before dynamic ones  ------------------------------
      sweep.referenceScalarSplice(runtime, 55, 9) // user.application.mostUse.appVersion ChildJoinPlace
      // -------- join dynamic child relation(s) after static ones  -------------------------------
      sweep.dynamicRelationSplices(runtime, 53, 18); // 'user.application.mostUse'  'DynamicJoinPlace'
      // -------- END CHILD RELATION JOINS 'user.application.mostUse' -----------------------------
      // -------- DYNAMIC RELATIONS CLEANUP 'user.application.mostUse' ----------------------------
      sweep.dynamicRelationSplices(runtime, 53, 19); // 'user.application.mostUse'  'DynamicCleanupPlace'
      sweep.referenceScalarSplice(runtime, 53, 7) // user.application.mostUse InstanceFreePlace
    }

    @inline final def tunnel_v1_user_sessions_appVersion(runtime: BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader: org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      // -------- tunnel scalar reference path='user.sessions.appVersion:101' version='1' ordinal=18
      if (sweep.skipTunnelPath(101) || runtime.lattice_user_sessions_relation.relationIsNull(reader, schema_structure_Session_V1_schematic, 18)) {
        runtime.lattice_user_sessions_appVersion_relation_isNull = true;
      } else {
        runtime.lattice_user_sessions_appVersion_relation_isNull = false;
        runtime.lattice_user_sessions_appVersion_relation = runtime.lattice_user_sessions_relation.referenceScalar(reader, schema_structure_Session_V1_schematic, 18);
      }
    }

    @inline final def search_v1_user_sessions(runtime: BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader: org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      sweep.referenceScalarSplice(runtime, 74, 6) // user.sessions InstanceAllocPlace
      sweep.referenceScalarSplice(runtime, 74, 1) // user.sessions InstancePrePlace
      // -------- START CHILD RELATION VISITS (BEF|E POST) 'user.sessions'  ----------------------
      // -------- visit static child relation(s) before dynamic ones  -----------------------------
      // -------- START reference-scalar path='user.sessions.appVersion:101' version=1 ordinal=18 -
      if (sweep.skipVisitPath(101) || runtime.lattice_user_sessions_relation.relationIsNull(reader, schema_structure_Session_V1_schematic, 18)) {
        if (sweep.skipTunnelPath(101)) {
          runtime.lattice_user_sessions_appVersion_relation_isNull = true;
        }
      } else {
        runtime.lattice_user_sessions_appVersion_relation_isNull = false;
        runtime.lattice_user_sessions_appVersion_relation = runtime.lattice_user_sessions_relation.referenceScalar(reader, schema_structure_Session_V1_schematic, 18);
        { // BEGIN reference scalar relations for 'user.sessions.appVersion'
          runtime.lattice_user_sessions_appVersion_relation.versionKey(reader) match {
            case 1 => { // schema version 1
              search_v1_user_sessions_appVersion(runtime, sweep, reader);
            }
          }
        } // END reference scalar relations for 'user.sessions.appVersion'
      } // END reference-scalar path='user.sessions.appVersion:101' version=1 ordinal=18
      sweep.referenceScalarSplice(runtime, 101, 8) // user.sessions.appVersion ChildMergePlace
      // -------- START value-map path='user.sessions.parameters:86' version=1 ordinal=3 ----------
      if (sweep.skipVisitPath(86) || runtime.lattice_user_sessions_relation.relationIsNull(reader, schema_structure_Session_V1_schematic, 3)) {
        runtime.lattice_user_sessions_parameters_relation_isNull = true;
      } else {
        iterate_valmap_v1_user_sessions_parameters(runtime, sweep, reader);
      } // END value-map path='user.sessions.parameters:86' version=1 ordinal=3
      sweep.referenceScalarSplice(runtime, 86, 8) // user.sessions.parameters ChildMergePlace
      // -------- START reference-vector path='user.sessions.events:76' version=1 ordinal=1 -------
      if (sweep.skipVisitPath(76) || runtime.lattice_user_sessions_relation.relationIsNull(reader, schema_structure_Session_V1_schematic, 1)) {
        runtime.lattice_user_sessions_events_relation_isNull = true;
      } else {
        iterate_refvec_v1_user_sessions_events(runtime, sweep, reader);
      } // END reference-vector path='user.sessions.events:76' version=1 ordinal=1
      sweep.referenceScalarSplice(runtime, 76, 8) // user.sessions.events ChildMergePlace
      // -------- START reference-vector path='user.sessions.variants:83' version=1 ordinal=2 -----
      if (sweep.skipVisitPath(83) || runtime.lattice_user_sessions_relation.relationIsNull(reader, schema_structure_Session_V1_schematic, 2)) {
        runtime.lattice_user_sessions_variants_relation_isNull = true;
      } else {
        iterate_refvec_v1_user_sessions_variants(runtime, sweep, reader);
      } // END reference-vector path='user.sessions.variants:83' version=1 ordinal=2
      sweep.referenceScalarSplice(runtime, 83, 8) // user.sessions.variants ChildMergePlace
      // -------- visit dynamic child relation(s) after static ones  ------------------------------
      sweep.dynamicRelationSplices(runtime, 74, 17); // 'user.sessions' 'DynamicVisitPlace'
      // -------- END CHILD RELATION VISITS 'user.sessions' ---------------------------------------
      sweep.referenceScalarSplice(runtime, 74, 2) // user.sessions InstancePostPlace
      // -------- START CHILD RELATION JOINS (AFTER POST) 'user.sessions' -------------------------
      // -------- join static child relation(s) before dynamic ones  ------------------------------
      sweep.referenceScalarSplice(runtime, 101, 9) // user.sessions.appVersion ChildJoinPlace
      sweep.referenceScalarSplice(runtime, 86, 9) // user.sessions.parameters ChildJoinPlace
      sweep.referenceScalarSplice(runtime, 76, 9) // user.sessions.events ChildJoinPlace
      sweep.referenceScalarSplice(runtime, 83, 9) // user.sessions.variants ChildJoinPlace
      // -------- join dynamic child relation(s) after static ones  -------------------------------
      sweep.dynamicRelationSplices(runtime, 74, 18); // 'user.sessions'  'DynamicJoinPlace'
      // -------- END CHILD RELATION JOINS 'user.sessions' ----------------------------------------
      // -------- DYNAMIC RELATIONS CLEANUP 'user.sessions' ---------------------------------------
      sweep.dynamicRelationSplices(runtime, 74, 19); // 'user.sessions'  'DynamicCleanupPlace'
      sweep.referenceScalarSplice(runtime, 74, 7) // user.sessions InstanceFreePlace
    }

    @inline final def search_v1_user_traits(runtime: BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader: org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      sweep.referenceScalarSplice(runtime, 114, 6) // user.traits InstanceAllocPlace
      sweep.referenceScalarSplice(runtime, 114, 1) // user.traits InstancePrePlace
      // -------- START CHILD RELATION VISITS (BEF|E POST) 'user.traits'  ------------------------
      // -------- visit static child relation(s) before dynamic ones  -----------------------------
      // -------- visit dynamic child relation(s) after static ones  ------------------------------
      sweep.dynamicRelationSplices(runtime, 114, 17); // 'user.traits' 'DynamicVisitPlace'
      // -------- END CHILD RELATION VISITS 'user.traits' -----------------------------------------
      sweep.referenceScalarSplice(runtime, 114, 2) // user.traits InstancePostPlace
      // -------- START CHILD RELATION JOINS (AFTER POST) 'user.traits' ---------------------------
      // -------- join static child relation(s) before dynamic ones  ------------------------------
      // -------- join dynamic child relation(s) after static ones  -------------------------------
      sweep.dynamicRelationSplices(runtime, 114, 18); // 'user.traits'  'DynamicJoinPlace'
      // -------- END CHILD RELATION JOINS 'user.traits' ------------------------------------------
      // -------- DYNAMIC RELATIONS CLEANUP 'user.traits' -----------------------------------------
      sweep.dynamicRelationSplices(runtime, 114, 19); // 'user.traits'  'DynamicCleanupPlace'
      sweep.referenceScalarSplice(runtime, 114, 7) // user.traits InstanceFreePlace
    }

    @inline final def search_v1_user_application_channels(runtime: BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader: org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      sweep.referenceScalarSplice(runtime, 5, 6) // user.application.channels InstanceAllocPlace
      sweep.referenceScalarSplice(runtime, 5, 1) // user.application.channels InstancePrePlace
      // -------- START CHILD RELATION VISITS (BEF|E POST) 'user.application.channels'  ----------
      // -------- visit static child relation(s) before dynamic ones  -----------------------------
      // -------- START value-map path='user.application.channels.parameters:7' version=1 ordinal=1
      if (sweep.skipVisitPath(7) || runtime.lattice_user_application_channels_relation.relationIsNull(reader, schema_structure_Channel_V1_schematic, 1)) {
        runtime.lattice_user_application_channels_parameters_relation_isNull = true;
      } else {
        iterate_valmap_v1_user_application_channels_parameters(runtime, sweep, reader);
      } // END value-map path='user.application.channels.parameters:7' version=1 ordinal=1
      sweep.referenceScalarSplice(runtime, 7, 8) // user.application.channels.parameters ChildMergePlace
      // -------- visit dynamic child relation(s) after static ones  ------------------------------
      sweep.dynamicRelationSplices(runtime, 5, 17); // 'user.application.channels' 'DynamicVisitPlace'
      // -------- END CHILD RELATION VISITS 'user.application.channels' ---------------------------
      sweep.referenceScalarSplice(runtime, 5, 2) // user.application.channels InstancePostPlace
      // -------- START CHILD RELATION JOINS (AFTER POST) 'user.application.channels' -------------
      // -------- join static child relation(s) before dynamic ones  ------------------------------
      sweep.referenceScalarSplice(runtime, 7, 9) // user.application.channels.parameters ChildJoinPlace
      // -------- join dynamic child relation(s) after static ones  -------------------------------
      sweep.dynamicRelationSplices(runtime, 5, 18); // 'user.application.channels'  'DynamicJoinPlace'
      // -------- END CHILD RELATION JOINS 'user.application.channels' ----------------------------
      // -------- DYNAMIC RELATIONS CLEANUP 'user.application.channels' ---------------------------
      sweep.dynamicRelationSplices(runtime, 5, 19); // 'user.application.channels'  'DynamicCleanupPlace'
      sweep.referenceScalarSplice(runtime, 5, 7) // user.application.channels InstanceFreePlace
    }

    @inline final def search_v1_user_application_firstUse_appVersion(runtime: BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader: org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      sweep.referenceScalarSplice(runtime, 13, 6) // user.application.firstUse.appVersion InstanceAllocPlace
      sweep.referenceScalarSplice(runtime, 13, 1) // user.application.firstUse.appVersion InstancePrePlace
      // -------- START CHILD RELATION VISITS (BEF|E POST) 'user.application.firstUse.appVersion'
      // -------- visit static child relation(s) before dynamic ones  -----------------------------
      // -------- visit dynamic child relation(s) after static ones  ------------------------------
      sweep.dynamicRelationSplices(runtime, 13, 17); // 'user.application.firstUse.appVersion' 'DynamicVisitPlace'
      // -------- END CHILD RELATION VISITS 'user.application.firstUse.appVersion' ----------------
      sweep.referenceScalarSplice(runtime, 13, 2) // user.application.firstUse.appVersion InstancePostPlace
      // -------- START CHILD RELATION JOINS (AFTER POST) 'user.application.firstUse.appVersion' --
      // -------- join static child relation(s) before dynamic ones  ------------------------------
      // -------- join dynamic child relation(s) after static ones  -------------------------------
      sweep.dynamicRelationSplices(runtime, 13, 18); // 'user.application.firstUse.appVersion'  'DynamicJoinPlace'
      // -------- END CHILD RELATION JOINS 'user.application.firstUse.appVersion' -----------------
      // -------- DYNAMIC RELATIONS CLEANUP 'user.application.firstUse.appVersion' ----------------
      sweep.dynamicRelationSplices(runtime, 13, 19); // 'user.application.firstUse.appVersion'  'DynamicCleanupPlace'
      sweep.referenceScalarSplice(runtime, 13, 7) // user.application.firstUse.appVersion InstanceFreePlace
    }

    @inline final def search_v1_user_application_lastUse_appVersion(runtime: BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader: org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      sweep.referenceScalarSplice(runtime, 34, 6) // user.application.lastUse.appVersion InstanceAllocPlace
      sweep.referenceScalarSplice(runtime, 34, 1) // user.application.lastUse.appVersion InstancePrePlace
      // -------- START CHILD RELATION VISITS (BEF|E POST) 'user.application.lastUse.appVersion'
      // -------- visit static child relation(s) before dynamic ones  -----------------------------
      // -------- visit dynamic child relation(s) after static ones  ------------------------------
      sweep.dynamicRelationSplices(runtime, 34, 17); // 'user.application.lastUse.appVersion' 'DynamicVisitPlace'
      // -------- END CHILD RELATION VISITS 'user.application.lastUse.appVersion' -----------------
      sweep.referenceScalarSplice(runtime, 34, 2) // user.application.lastUse.appVersion InstancePostPlace
      // -------- START CHILD RELATION JOINS (AFTER POST) 'user.application.lastUse.appVersion' ---
      // -------- join static child relation(s) before dynamic ones  ------------------------------
      // -------- join dynamic child relation(s) after static ones  -------------------------------
      sweep.dynamicRelationSplices(runtime, 34, 18); // 'user.application.lastUse.appVersion'  'DynamicJoinPlace'
      // -------- END CHILD RELATION JOINS 'user.application.lastUse.appVersion' ------------------
      // -------- DYNAMIC RELATIONS CLEANUP 'user.application.lastUse.appVersion' -----------------
      sweep.dynamicRelationSplices(runtime, 34, 19); // 'user.application.lastUse.appVersion'  'DynamicCleanupPlace'
      sweep.referenceScalarSplice(runtime, 34, 7) // user.application.lastUse.appVersion InstanceFreePlace
    }

    @inline final def search_v1_user_application_mostUse_appVersion(runtime: BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader: org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      sweep.referenceScalarSplice(runtime, 55, 6) // user.application.mostUse.appVersion InstanceAllocPlace
      sweep.referenceScalarSplice(runtime, 55, 1) // user.application.mostUse.appVersion InstancePrePlace
      // -------- START CHILD RELATION VISITS (BEF|E POST) 'user.application.mostUse.appVersion'
      // -------- visit static child relation(s) before dynamic ones  -----------------------------
      // -------- visit dynamic child relation(s) after static ones  ------------------------------
      sweep.dynamicRelationSplices(runtime, 55, 17); // 'user.application.mostUse.appVersion' 'DynamicVisitPlace'
      // -------- END CHILD RELATION VISITS 'user.application.mostUse.appVersion' -----------------
      sweep.referenceScalarSplice(runtime, 55, 2) // user.application.mostUse.appVersion InstancePostPlace
      // -------- START CHILD RELATION JOINS (AFTER POST) 'user.application.mostUse.appVersion' ---
      // -------- join static child relation(s) before dynamic ones  ------------------------------
      // -------- join dynamic child relation(s) after static ones  -------------------------------
      sweep.dynamicRelationSplices(runtime, 55, 18); // 'user.application.mostUse.appVersion'  'DynamicJoinPlace'
      // -------- END CHILD RELATION JOINS 'user.application.mostUse.appVersion' ------------------
      // -------- DYNAMIC RELATIONS CLEANUP 'user.application.mostUse.appVersion' -----------------
      sweep.dynamicRelationSplices(runtime, 55, 19); // 'user.application.mostUse.appVersion'  'DynamicCleanupPlace'
      sweep.referenceScalarSplice(runtime, 55, 7) // user.application.mostUse.appVersion InstanceFreePlace
    }

    @inline final def search_v1_user_sessions_appVersion(runtime: BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader: org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      sweep.referenceScalarSplice(runtime, 101, 6) // user.sessions.appVersion InstanceAllocPlace
      sweep.referenceScalarSplice(runtime, 101, 1) // user.sessions.appVersion InstancePrePlace
      // -------- START CHILD RELATION VISITS (BEF|E POST) 'user.sessions.appVersion'  -----------
      // -------- visit static child relation(s) before dynamic ones  -----------------------------
      // -------- visit dynamic child relation(s) after static ones  ------------------------------
      sweep.dynamicRelationSplices(runtime, 101, 17); // 'user.sessions.appVersion' 'DynamicVisitPlace'
      // -------- END CHILD RELATION VISITS 'user.sessions.appVersion' ----------------------------
      sweep.referenceScalarSplice(runtime, 101, 2) // user.sessions.appVersion InstancePostPlace
      // -------- START CHILD RELATION JOINS (AFTER POST) 'user.sessions.appVersion' --------------
      // -------- join static child relation(s) before dynamic ones  ------------------------------
      // -------- join dynamic child relation(s) after static ones  -------------------------------
      sweep.dynamicRelationSplices(runtime, 101, 18); // 'user.sessions.appVersion'  'DynamicJoinPlace'
      // -------- END CHILD RELATION JOINS 'user.sessions.appVersion' -----------------------------
      // -------- DYNAMIC RELATIONS CLEANUP 'user.sessions.appVersion' ----------------------------
      sweep.dynamicRelationSplices(runtime, 101, 19); // 'user.sessions.appVersion'  'DynamicCleanupPlace'
      sweep.referenceScalarSplice(runtime, 101, 7) // user.sessions.appVersion InstanceFreePlace
    }

    @inline final def iterate_valmap_v1_user_sessions_parameters(runtime: BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader: org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      runtime.lattice_user_sessions_parameters_relation_isNull = false;
      runtime.lattice_user_sessions_parameters_vector_is_first = false;
      runtime.lattice_user_sessions_parameters_vector_is_last = false;
      val memberIterator = runtime.lattice_user_sessions_relation.valueMapIterator(reader, schema_structure_Session_V1_schematic, 3);
      val memberCount = memberIterator.length(reader);
      sweep.valueMapSplice(runtime, 86, 10); // user.sessions.parameters VectorAllocPlace
      sweep.valueMapSplice(runtime, 86, 3); // user.sessions.parameters VectorBeforePlace
      var i = 0;
      while (i < memberCount && !runtime.skipControlMemberPath(86)) {
        runtime.lattice_user_sessions_parameters_vector_is_first = (i == 0);
        runtime.lattice_user_sessions_parameters_vector_is_last = i == (memberCount - 1);
        sweep.valueMapMemberSplice(runtime, 86, 12); // user.sessions.parameters VectorMemberAllocPlace
        // -------- current member key/value tuple for value map user.sessions.parameters -----------
        runtime.lattice_user_sessions_parameters_value_map_key = memberIterator.readLexiconStringKey(i, reader);
        runtime.lattice_user_sessions_parameters_value_map_value = memberIterator.readLexiconStringStringValue(i, reader);
        sweep.valueMapMemberSplice(runtime, 86, 5); // user.sessions.parameters VectorMemberSituPlace
        sweep.valueMapMemberSplice(runtime, 86, 14); // user.sessions.parameters VectorMemberMergePlace
        sweep.valueMapMemberSplice(runtime, 86, 13); // user.sessions.parameters VectorMemberFreePlace
        i += 1;
      }
      runtime.lattice_user_sessions_parameters_vector_is_first = false; // this should be false for 'after' splices
      runtime.lattice_user_sessions_parameters_vector_is_last = false; // this should be false for 'after' splices
      sweep.valueMapSplice(runtime, 86, 4); // user.sessions.parameters VectorAfterPlace
      sweep.valueMapSplice(runtime, 86, 11); // user.sessions.parameters VectorFreePlace
    }

    @inline final def iterate_refvec_v1_user_sessions_events(runtime: BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader: org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      runtime.lattice_user_sessions_events_relation_isNull = false;
      runtime.lattice_user_sessions_events_vector_is_first = false;
      runtime.lattice_user_sessions_events_vector_is_last = false;
      val memberIterator = runtime.lattice_user_sessions_relation.referenceVectorIterator(reader, schema_structure_Session_V1_schematic, 1);
      var vectorIndex = memberIterator.start(reader);
      val memberCount = memberIterator.length(reader);
      sweep.referenceVectorSplice(runtime, 76, 10); // user.sessions.events VectorAllocPlace
      sweep.referenceVectorSplice(runtime, 76, 3); // user.sessions.events VectorBeforePlace
      var i = 0;
      while (i < memberCount && !runtime.skipControlMemberPath(76)) {
        runtime.lattice_user_sessions_events_vector_is_first = (i == 0);
        runtime.lattice_user_sessions_events_vector_is_last = i == (memberCount - 1);
        runtime.lattice_user_sessions_events_relation = memberIterator.member(reader, vectorIndex);
        sweep.referenceVectorMemberSplice(runtime, 76, 12); // user.sessions.events VectorMemberAllocPlace
        { // BEGIN reference scalar relations for 'user.sessions.events'
          runtime.lattice_user_sessions_events_relation.versionKey(reader) match {
            case 1 => { // schema version 1
              search_v1_user_sessions_events(runtime, sweep, reader);
            }
          }
        } // END reference scalar relations for 'user.sessions.events'
        sweep.referenceVectorMemberSplice(runtime, 76, 14); // user.sessions.events VectorMemberMergePlace
        sweep.referenceVectorMemberSplice(runtime, 76, 13); // user.sessions.events VectorMemberFreePlace
        vectorIndex = memberIterator.advance(reader, vectorIndex);
        i += 1;
      }
      runtime.lattice_user_sessions_events_vector_is_first = false; // this should be false for 'after' splices
      runtime.lattice_user_sessions_events_vector_is_last = false; // this should be false for 'after' splices
      sweep.referenceVectorSplice(runtime, 76, 4); // user.sessions.events VectorAfterPlace
      sweep.referenceVectorSplice(runtime, 76, 11); // user.sessions.events VectorFreePlace
    }

    @inline final def iterate_refvec_v1_user_sessions_variants(runtime: BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader: org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      runtime.lattice_user_sessions_variants_relation_isNull = false;
      runtime.lattice_user_sessions_variants_vector_is_first = false;
      runtime.lattice_user_sessions_variants_vector_is_last = false;
      val memberIterator = runtime.lattice_user_sessions_relation.referenceVectorIterator(reader, schema_structure_Session_V1_schematic, 2);
      var vectorIndex = memberIterator.start(reader);
      val memberCount = memberIterator.length(reader);
      sweep.referenceVectorSplice(runtime, 83, 10); // user.sessions.variants VectorAllocPlace
      sweep.referenceVectorSplice(runtime, 83, 3); // user.sessions.variants VectorBeforePlace
      var i = 0;
      while (i < memberCount && !runtime.skipControlMemberPath(83)) {
        runtime.lattice_user_sessions_variants_vector_is_first = (i == 0);
        runtime.lattice_user_sessions_variants_vector_is_last = i == (memberCount - 1);
        runtime.lattice_user_sessions_variants_relation = memberIterator.member(reader, vectorIndex);
        sweep.referenceVectorMemberSplice(runtime, 83, 12); // user.sessions.variants VectorMemberAllocPlace
        { // BEGIN reference scalar relations for 'user.sessions.variants'
          runtime.lattice_user_sessions_variants_relation.versionKey(reader) match {
            case 1 => { // schema version 1
              search_v1_user_sessions_variants(runtime, sweep, reader);
            }
          }
        } // END reference scalar relations for 'user.sessions.variants'
        sweep.referenceVectorMemberSplice(runtime, 83, 14); // user.sessions.variants VectorMemberMergePlace
        sweep.referenceVectorMemberSplice(runtime, 83, 13); // user.sessions.variants VectorMemberFreePlace
        vectorIndex = memberIterator.advance(reader, vectorIndex);
        i += 1;
      }
      runtime.lattice_user_sessions_variants_vector_is_first = false; // this should be false for 'after' splices
      runtime.lattice_user_sessions_variants_vector_is_last = false; // this should be false for 'after' splices
      sweep.referenceVectorSplice(runtime, 83, 4); // user.sessions.variants VectorAfterPlace
      sweep.referenceVectorSplice(runtime, 83, 11); // user.sessions.variants VectorFreePlace
    }

    @inline final def iterate_valmap_v1_user_application_channels_parameters(runtime: BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader: org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      runtime.lattice_user_application_channels_parameters_relation_isNull = false;
      runtime.lattice_user_application_channels_parameters_vector_is_first = false;
      runtime.lattice_user_application_channels_parameters_vector_is_last = false;
      val memberIterator = runtime.lattice_user_application_channels_relation.valueMapIterator(reader, schema_structure_Channel_V1_schematic, 1);
      val memberCount = memberIterator.length(reader);
      sweep.valueMapSplice(runtime, 7, 10); // user.application.channels.parameters VectorAllocPlace
      sweep.valueMapSplice(runtime, 7, 3); // user.application.channels.parameters VectorBeforePlace
      var i = 0;
      while (i < memberCount && !runtime.skipControlMemberPath(7)) {
        runtime.lattice_user_application_channels_parameters_vector_is_first = (i == 0);
        runtime.lattice_user_application_channels_parameters_vector_is_last = i == (memberCount - 1);
        sweep.valueMapMemberSplice(runtime, 7, 12); // user.application.channels.parameters VectorMemberAllocPlace
        // -------- current member key/value tuple for value map user.application.channels.parameters
        runtime.lattice_user_application_channels_parameters_value_map_key = memberIterator.readLexiconStringKey(i, reader);
        runtime.lattice_user_application_channels_parameters_value_map_value = memberIterator.readLexiconStringStringValue(i, reader);
        sweep.valueMapMemberSplice(runtime, 7, 5); // user.application.channels.parameters VectorMemberSituPlace
        sweep.valueMapMemberSplice(runtime, 7, 14); // user.application.channels.parameters VectorMemberMergePlace
        sweep.valueMapMemberSplice(runtime, 7, 13); // user.application.channels.parameters VectorMemberFreePlace
        i += 1;
      }
      runtime.lattice_user_application_channels_parameters_vector_is_first = false; // this should be false for 'after' splices
      runtime.lattice_user_application_channels_parameters_vector_is_last = false; // this should be false for 'after' splices
      sweep.valueMapSplice(runtime, 7, 4); // user.application.channels.parameters VectorAfterPlace
      sweep.valueMapSplice(runtime, 7, 11); // user.application.channels.parameters VectorFreePlace
    }

    @inline final def search_v1_user_sessions_events(runtime: BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader: org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      sweep.referenceScalarSplice(runtime, 76, 6) // user.sessions.events InstanceAllocPlace
      sweep.referenceScalarSplice(runtime, 76, 1) // user.sessions.events InstancePrePlace
      // -------- START CHILD RELATION VISITS (BEF|E POST) 'user.sessions.events'  ---------------
      // -------- visit static child relation(s) before dynamic ones  -----------------------------
      // -------- START value-map path='user.sessions.events.parameters:78' version=1 ordinal=1 ---
      if (sweep.skipVisitPath(78) || runtime.lattice_user_sessions_events_relation.relationIsNull(reader, schema_structure_Event_V1_schematic, 1)) {
        runtime.lattice_user_sessions_events_parameters_relation_isNull = true;
      } else {
        iterate_valmap_v1_user_sessions_events_parameters(runtime, sweep, reader);
      } // END value-map path='user.sessions.events.parameters:78' version=1 ordinal=1
      sweep.referenceScalarSplice(runtime, 78, 8) // user.sessions.events.parameters ChildMergePlace
      // -------- visit dynamic child relation(s) after static ones  ------------------------------
      sweep.dynamicRelationSplices(runtime, 76, 17); // 'user.sessions.events' 'DynamicVisitPlace'
      // -------- END CHILD RELATION VISITS 'user.sessions.events' --------------------------------
      sweep.referenceScalarSplice(runtime, 76, 2) // user.sessions.events InstancePostPlace
      // -------- START CHILD RELATION JOINS (AFTER POST) 'user.sessions.events' ------------------
      // -------- join static child relation(s) before dynamic ones  ------------------------------
      sweep.referenceScalarSplice(runtime, 78, 9) // user.sessions.events.parameters ChildJoinPlace
      // -------- join dynamic child relation(s) after static ones  -------------------------------
      sweep.dynamicRelationSplices(runtime, 76, 18); // 'user.sessions.events'  'DynamicJoinPlace'
      // -------- END CHILD RELATION JOINS 'user.sessions.events' ---------------------------------
      // -------- DYNAMIC RELATIONS CLEANUP 'user.sessions.events' --------------------------------
      sweep.dynamicRelationSplices(runtime, 76, 19); // 'user.sessions.events'  'DynamicCleanupPlace'
      sweep.referenceScalarSplice(runtime, 76, 7) // user.sessions.events InstanceFreePlace
    }

    @inline final def search_v1_user_sessions_variants(runtime: BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader: org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      sweep.referenceScalarSplice(runtime, 83, 6) // user.sessions.variants InstanceAllocPlace
      sweep.referenceScalarSplice(runtime, 83, 1) // user.sessions.variants InstancePrePlace
      // -------- START CHILD RELATION VISITS (BEF|E POST) 'user.sessions.variants'  -------------
      // -------- visit static child relation(s) before dynamic ones  -----------------------------
      // -------- visit dynamic child relation(s) after static ones  ------------------------------
      sweep.dynamicRelationSplices(runtime, 83, 17); // 'user.sessions.variants' 'DynamicVisitPlace'
      // -------- END CHILD RELATION VISITS 'user.sessions.variants' ------------------------------
      sweep.referenceScalarSplice(runtime, 83, 2) // user.sessions.variants InstancePostPlace
      // -------- START CHILD RELATION JOINS (AFTER POST) 'user.sessions.variants' ----------------
      // -------- join static child relation(s) before dynamic ones  ------------------------------
      // -------- join dynamic child relation(s) after static ones  -------------------------------
      sweep.dynamicRelationSplices(runtime, 83, 18); // 'user.sessions.variants'  'DynamicJoinPlace'
      // -------- END CHILD RELATION JOINS 'user.sessions.variants' -------------------------------
      // -------- DYNAMIC RELATIONS CLEANUP 'user.sessions.variants' ------------------------------
      sweep.dynamicRelationSplices(runtime, 83, 19); // 'user.sessions.variants'  'DynamicCleanupPlace'
      sweep.referenceScalarSplice(runtime, 83, 7) // user.sessions.variants InstanceFreePlace
    }

    @inline final def iterate_valmap_v1_user_sessions_events_parameters(runtime: BF03EEE56C3CC4047833A822C1D58BCA2_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader: org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      runtime.lattice_user_sessions_events_parameters_relation_isNull = false;
      runtime.lattice_user_sessions_events_parameters_vector_is_first = false;
      runtime.lattice_user_sessions_events_parameters_vector_is_last = false;
      val memberIterator = runtime.lattice_user_sessions_events_relation.valueMapIterator(reader, schema_structure_Event_V1_schematic, 1);
      val memberCount = memberIterator.length(reader);
      sweep.valueMapSplice(runtime, 78, 10); // user.sessions.events.parameters VectorAllocPlace
      sweep.valueMapSplice(runtime, 78, 3); // user.sessions.events.parameters VectorBeforePlace
      var i = 0;
      while (i < memberCount && !runtime.skipControlMemberPath(78)) {
        runtime.lattice_user_sessions_events_parameters_vector_is_first = (i == 0);
        runtime.lattice_user_sessions_events_parameters_vector_is_last = i == (memberCount - 1);
        sweep.valueMapMemberSplice(runtime, 78, 12); // user.sessions.events.parameters VectorMemberAllocPlace
        // -------- current member key/value tuple for value map user.sessions.events.parameters ----
        runtime.lattice_user_sessions_events_parameters_value_map_key = memberIterator.readLexiconStringKey(i, reader);
        runtime.lattice_user_sessions_events_parameters_value_map_value = memberIterator.readLexiconStringStringValue(i, reader);
        sweep.valueMapMemberSplice(runtime, 78, 5); // user.sessions.events.parameters VectorMemberSituPlace
        sweep.valueMapMemberSplice(runtime, 78, 14); // user.sessions.events.parameters VectorMemberMergePlace
        sweep.valueMapMemberSplice(runtime, 78, 13); // user.sessions.events.parameters VectorMemberFreePlace
        i += 1;
      }
      runtime.lattice_user_sessions_events_parameters_vector_is_first = false; // this should be false for 'after' splices
      runtime.lattice_user_sessions_events_parameters_vector_is_last = false; // this should be false for 'after' splices
      sweep.valueMapSplice(runtime, 78, 4); // user.sessions.events.parameters VectorAfterPlace
      sweep.valueMapSplice(runtime, 78, 11); // user.sessions.events.parameters VectorFreePlace
    }
  }
}

