/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.support

import org.burstsys.brio.lattice.{BrioLatticeRefVecIterator, BrioLatticeReference}
import org.burstsys.felt.model.runtime.FeltRuntime
import org.burstsys.felt.model.schema.traveler.FeltTraveler

object GeneratedQuoSchema {


  // -------- generated felt 'traveler' for brio schema 'Quo' WITH_LEXICON) -------------------
  // -------- traveler specific abstract base runtime class  ----------------------------------
  trait QuoTraveler_lexicon_runtime extends org.burstsys.felt.model.runtime.FeltRuntime {
    // -------- lattice variable declarations ---------------------------------------------------
    final var lattice_user_relation:org.burstsys.brio.lattice.BrioLatticeReference = org.burstsys.brio.lattice.BrioLatticeReference();
    final var lattice_user_relation_isNull:Boolean = true;
    final var lattice_user_project_relation:org.burstsys.brio.lattice.BrioLatticeReference = org.burstsys.brio.lattice.BrioLatticeReference();
    final var lattice_user_project_relation_isNull:Boolean = true;
    final var lattice_user_sessions_relation:org.burstsys.brio.lattice.BrioLatticeReference = org.burstsys.brio.lattice.BrioLatticeReference();
    final var lattice_user_sessions_relation_isNull:Boolean = true;
    final var lattice_user_sessions_vector_is_first:Boolean = false;
    final var lattice_user_sessions_vector_is_last:Boolean = false;
    final var lattice_user_sessions_events_relation:org.burstsys.brio.lattice.BrioLatticeReference = org.burstsys.brio.lattice.BrioLatticeReference();
    final var lattice_user_sessions_events_relation_isNull:Boolean = true;
    final var lattice_user_sessions_events_vector_is_first:Boolean = false;
    final var lattice_user_sessions_events_vector_is_last:Boolean = false;
    final var lattice_user_sessions_events_parameters_relation_isNull:Boolean = true;
    final var lattice_user_sessions_events_parameters_value_map_key:scala.Short = _ ;
    final var lattice_user_sessions_events_parameters_value_map_value:scala.Short = _ ;
    final var lattice_user_sessions_events_parameters_vector_is_first:Boolean = false;
    final var lattice_user_sessions_events_parameters_vector_is_last:Boolean = false;
    final var lattice_user_sessions_parameters_relation_isNull:Boolean = true;
    final var lattice_user_sessions_parameters_value_map_key:scala.Short = _ ;
    final var lattice_user_sessions_parameters_value_map_value:scala.Short = _ ;
    final var lattice_user_sessions_parameters_vector_is_first:Boolean = false;
    final var lattice_user_sessions_parameters_vector_is_last:Boolean = false;
    final var lattice_user_segments_relation:org.burstsys.brio.lattice.BrioLatticeReference = org.burstsys.brio.lattice.BrioLatticeReference();
    final var lattice_user_segments_relation_isNull:Boolean = true;
    final var lattice_user_segments_vector_is_first:Boolean = false;
    final var lattice_user_segments_vector_is_last:Boolean = false;
    final var lattice_user_channels_relation:org.burstsys.brio.lattice.BrioLatticeReference = org.burstsys.brio.lattice.BrioLatticeReference();
    final var lattice_user_channels_relation_isNull:Boolean = true;
    final var lattice_user_channels_vector_is_first:Boolean = false;
    final var lattice_user_channels_vector_is_last:Boolean = false;
    final var lattice_user_personas_relation:org.burstsys.brio.lattice.BrioLatticeReference = org.burstsys.brio.lattice.BrioLatticeReference();
    final var lattice_user_personas_relation_isNull:Boolean = true;
    final var lattice_user_personas_vector_is_first:Boolean = false;
    final var lattice_user_personas_vector_is_last:Boolean = false;
    final var lattice_user_parameters_relation_isNull:Boolean = true;
    final var lattice_user_parameters_value_map_key:scala.Short = _ ;
    final var lattice_user_parameters_value_map_value:scala.Short = _ ;
    final var lattice_user_parameters_vector_is_first:Boolean = false;
    final var lattice_user_parameters_vector_is_last:Boolean = false;
  }
  class QuoTraveler_lexicon extends org.burstsys.felt.model.schema.traveler.FeltTraveler[QuoTraveler_lexicon_runtime] {
    final def travelerClassName:String = "QuoTraveler_lexicon";
    final def runtimeClassName:String = "QuoTraveler_lexicon_runtime";
    final val brioSchema = org.burstsys.brio.model.schema.BrioSchema("Quo");
    // -------- schematic references ------------------------------------------------------------
    final val schema_structure_Channel_V1_schematic:org.burstsys.brio.model.schema.encoding.BrioSchematic = brioSchema.schematic(56, 1);
    final val schema_structure_Channel_V2_schematic:org.burstsys.brio.model.schema.encoding.BrioSchematic = brioSchema.schematic(56, 2);
    final val schema_structure_Channel_V3_schematic:org.burstsys.brio.model.schema.encoding.BrioSchematic = brioSchema.schematic(56, 3);
    final val schema_structure_Event_V1_schematic:org.burstsys.brio.model.schema.encoding.BrioSchematic = brioSchema.schematic(57, 1);
    final val schema_structure_Event_V2_schematic:org.burstsys.brio.model.schema.encoding.BrioSchematic = brioSchema.schematic(57, 2);
    final val schema_structure_Event_V3_schematic:org.burstsys.brio.model.schema.encoding.BrioSchematic = brioSchema.schematic(57, 3);
    final val schema_structure_Session_V1_schematic:org.burstsys.brio.model.schema.encoding.BrioSchematic = brioSchema.schematic(58, 1);
    final val schema_structure_Session_V2_schematic:org.burstsys.brio.model.schema.encoding.BrioSchematic = brioSchema.schematic(58, 2);
    final val schema_structure_Session_V3_schematic:org.burstsys.brio.model.schema.encoding.BrioSchematic = brioSchema.schematic(58, 3);
    final val schema_structure_Segment_V1_schematic:org.burstsys.brio.model.schema.encoding.BrioSchematic = brioSchema.schematic(59, 1);
    final val schema_structure_Segment_V2_schematic:org.burstsys.brio.model.schema.encoding.BrioSchematic = brioSchema.schematic(59, 2);
    final val schema_structure_Segment_V3_schematic:org.burstsys.brio.model.schema.encoding.BrioSchematic = brioSchema.schematic(59, 3);
    final val schema_structure_Project_V1_schematic:org.burstsys.brio.model.schema.encoding.BrioSchematic = brioSchema.schematic(60, 1);
    final val schema_structure_Project_V2_schematic:org.burstsys.brio.model.schema.encoding.BrioSchematic = brioSchema.schematic(60, 2);
    final val schema_structure_Project_V3_schematic:org.burstsys.brio.model.schema.encoding.BrioSchematic = brioSchema.schematic(60, 3);
    final val schema_structure_Persona_V1_schematic:org.burstsys.brio.model.schema.encoding.BrioSchematic = brioSchema.schematic(61, 1);
    final val schema_structure_Persona_V2_schematic:org.burstsys.brio.model.schema.encoding.BrioSchematic = brioSchema.schematic(61, 2);
    final val schema_structure_Persona_V3_schematic:org.burstsys.brio.model.schema.encoding.BrioSchematic = brioSchema.schematic(61, 3);
    final val schema_structure_User_V1_schematic:org.burstsys.brio.model.schema.encoding.BrioSchematic = brioSchema.schematic(62, 1);
    final val schema_structure_User_V2_schematic:org.burstsys.brio.model.schema.encoding.BrioSchematic = brioSchema.schematic(62, 2);
    final val schema_structure_User_V3_schematic:org.burstsys.brio.model.schema.encoding.BrioSchematic = brioSchema.schematic(62, 3);
    // -------- called per brio blob traversed with analysis-specific parallel 'sweep' (reentrant) and 'runtime' (one per thread) 
    @inline final def apply(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep): Unit = {
      val reader = runtime.reader;
      latticeVarInitialize(runtime);
      val rootPathKey = brioSchema.rootNode.pathKey;
      sweep.rootSplice(runtime, rootPathKey, 15); // TraverseCommencePlace
      { // BEGIN reference scalar tunnels for 'user'
        runtime.lattice_user_channels_relation_isNull = true; // reset
        runtime.lattice_user_project_relation_isNull = true; // reset
        runtime.lattice_user_sessions_parameters_relation_isNull = true; // reset
        runtime.lattice_user_sessions_events_parameters_relation_isNull = true; // reset
        runtime.lattice_user_sessions_events_relation_isNull = true; // reset
        runtime.lattice_user_sessions_relation_isNull = true; // reset
        runtime.lattice_user_personas_relation_isNull = true; // reset
        runtime.lattice_user_segments_relation_isNull = true; // reset
        runtime.lattice_user_parameters_relation_isNull = true; // reset
        runtime.lattice_user_relation.versionKey(reader) match {
          case 1 ⇒ { // schema version 1
            tunnel_v1_user_project(runtime, sweep, reader);
          }
          case 2 ⇒ { // schema version 2
            tunnel_v2_user_project(runtime, sweep, reader);
          }
          case 3 ⇒ { // schema version 3
            tunnel_v3_user_project(runtime, sweep, reader);
          }
        }
      } // END reference scalar tunnels for 'user' 
      { // BEGIN reference scalar relations for 'user'
        runtime.lattice_user_relation.versionKey(reader) match {
          case 1 ⇒ { // schema version 1
            search_v1_user(runtime, sweep, reader);
          }
          case 2 ⇒ { // schema version 2
            search_v2_user(runtime, sweep, reader);
          }
          case 3 ⇒ { // schema version 3
            search_v3_user(runtime, sweep, reader);
          }
        }
      } // END reference scalar relations for 'user' 
      sweep.rootSplice(runtime, rootPathKey, 16); // TraverseCompletePlace
    }
    // -------- lattice variable initialization -------------------------------------------------
    @inline
    def latticeVarInitialize(runtime:QuoTraveler_lexicon_runtime): Unit = {
      runtime.lattice_user_relation = runtime.lattice; // init root of lattice...
      runtime.lattice_user_relation_isNull = false;
    }
    // -------- support test for path in scope (on axis and below) ------------------------------
    @inline override
    def visitInScope(visitKey: Int, scopeKey: Int): Boolean = {
      if(visitKey == -1 || scopeKey == -1) return false;
      visitKey match {
        case 1 => // VISIT: user
          scopeKey match {
            case 1 // SCOPE: user
            => true
            case _ => false
          }
        case 3 => // VISIT: user.project
          scopeKey match {
            case 1 // SCOPE: user
                 | 3 // SCOPE: user.project
            => true
            case _ => false
          }
        case 16 => // VISIT: user.sessions
          scopeKey match {
            case 1 // SCOPE: user
                 | 16 // SCOPE: user.sessions
            => true
            case _ => false
          }
        case 32 => // VISIT: user.sessions.events
          scopeKey match {
            case 1 // SCOPE: user
                 | 16 // SCOPE: user.sessions
                 | 32 // SCOPE: user.sessions.events
            => true
            case _ => false
          }
        case 38 => // VISIT: user.sessions.events.parameters
          scopeKey match {
            case 1 // SCOPE: user
                 | 16 // SCOPE: user.sessions
                 | 32 // SCOPE: user.sessions.events
                 | 38 // SCOPE: user.sessions.events.parameters
            => true
            case _ => false
          }
        case 43 => // VISIT: user.sessions.parameters
          scopeKey match {
            case 1 // SCOPE: user
                 | 16 // SCOPE: user.sessions
                 | 43 // SCOPE: user.sessions.parameters
            => true
            case _ => false
          }
        case 46 => // VISIT: user.segments
          scopeKey match {
            case 1 // SCOPE: user
                 | 46 // SCOPE: user.segments
            => true
            case _ => false
          }
        case 48 => // VISIT: user.channels
          scopeKey match {
            case 1 // SCOPE: user
                 | 48 // SCOPE: user.channels
            => true
            case _ => false
          }
        case 52 => // VISIT: user.personas
          scopeKey match {
            case 1 // SCOPE: user
                 | 52 // SCOPE: user.personas
            => true
            case _ => false
          }
        case 56 => // VISIT: user.parameters
          scopeKey match {
            case 1 // SCOPE: user
                 | 56 // SCOPE: user.parameters
            => true
            case _ => false
          }
        case _ => ???
      }
    }
    // -------- traveler search methods ---------------------------------------------------------
    @inline final def tunnel_v1_user_project(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      // -------- tunnel scalar reference path='user.project:3' version='1' ordinal=1 -------------
      if (sweep.skipTunnelPath(3) || runtime.lattice_user_relation.relationIsNull(reader, schema_structure_User_V1_schematic, 1)) {
        runtime.lattice_user_project_relation_isNull = true;
      } else {
        runtime.lattice_user_project_relation_isNull = false;
        runtime.lattice_user_project_relation = runtime.lattice_user_relation.referenceScalar(reader, schema_structure_User_V1_schematic, 1);
      }
    }
    @inline final def tunnel_v2_user_project(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      // -------- tunnel scalar reference path='user.project:3' version='2' ordinal=1 -------------
      if (sweep.skipTunnelPath(3) || runtime.lattice_user_relation.relationIsNull(reader, schema_structure_User_V2_schematic, 1)) {
        runtime.lattice_user_project_relation_isNull = true;
      } else {
        runtime.lattice_user_project_relation_isNull = false;
        runtime.lattice_user_project_relation = runtime.lattice_user_relation.referenceScalar(reader, schema_structure_User_V2_schematic, 1);
      }
    }
    @inline final def tunnel_v3_user_project(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      // -------- tunnel scalar reference path='user.project:3' version='3' ordinal=1 -------------
      if (sweep.skipTunnelPath(3) || runtime.lattice_user_relation.relationIsNull(reader, schema_structure_User_V3_schematic, 1)) {
        runtime.lattice_user_project_relation_isNull = true;
      } else {
        runtime.lattice_user_project_relation_isNull = false;
        runtime.lattice_user_project_relation = runtime.lattice_user_relation.referenceScalar(reader, schema_structure_User_V3_schematic, 1);
      }
    }
    @inline final def search_v1_user(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      sweep.referenceScalarSplice(runtime, 1, 6) // user InstanceAllocPlace
      sweep.referenceScalarSplice(runtime, 1, 1) // user InstancePrePlace
      // -------- START CHILD RELATION VISITS (BEF|E POST) 'user'  -------------------------------
      // -------- visit static child relation(s) before dynamic ones  -----------------------------
      // -------- START reference-vector path='user.channels:48' version=1 ordinal=4 --------------
      if (sweep.skipVisitPath(48) || runtime.lattice_user_relation.relationIsNull(reader, schema_structure_User_V1_schematic, 4)) {
        runtime.lattice_user_channels_relation_isNull = true;
      } else {
        iterate_refvec_v1_user_channels(runtime, sweep, reader);
      } // END reference-vector path='user.channels:48' version=1 ordinal=4
      sweep.referenceScalarSplice(runtime, 48, 8) // user.channels ChildMergePlace
      // -------- START reference-scalar path='user.project:3' version=1 ordinal=1 ----------------
      if (sweep.skipVisitPath(3) || runtime.lattice_user_relation.relationIsNull(reader, schema_structure_User_V1_schematic, 1)) {
        if (sweep.skipTunnelPath(3)) {
          runtime.lattice_user_project_relation_isNull = true;
        }
      } else {
        runtime.lattice_user_project_relation_isNull = false;
        runtime.lattice_user_project_relation = runtime.lattice_user_relation.referenceScalar(reader, schema_structure_User_V1_schematic, 1);
        { // BEGIN reference scalar relations for 'user.project'
          runtime.lattice_user_project_relation.versionKey(reader) match {
            case 1 ⇒ { // schema version 1
              search_v1_user_project(runtime, sweep, reader);
            }
            case 2 ⇒ { // schema version 2
              search_v2_user_project(runtime, sweep, reader);
            }
            case 3 ⇒ { // schema version 3
              search_v3_user_project(runtime, sweep, reader);
            }
          }
        } // END reference scalar relations for 'user.project' 
      } // END reference-scalar path='user.project:3' version=1 ordinal=1
      sweep.referenceScalarSplice(runtime, 3, 8) // user.project ChildMergePlace
      // -------- START reference-vector path='user.sessions:16' version=1 ordinal=2 --------------
      if (sweep.skipVisitPath(16) || runtime.lattice_user_relation.relationIsNull(reader, schema_structure_User_V1_schematic, 2)) {
        runtime.lattice_user_sessions_relation_isNull = true;
      } else {
        iterate_refvec_v1_user_sessions(runtime, sweep, reader);
      } // END reference-vector path='user.sessions:16' version=1 ordinal=2
      sweep.referenceScalarSplice(runtime, 16, 8) // user.sessions ChildMergePlace
      // -------- START reference-vector path='user.personas:52' version=1 ordinal=5 --------------
      if (sweep.skipVisitPath(52) || runtime.lattice_user_relation.relationIsNull(reader, schema_structure_User_V1_schematic, 5)) {
        runtime.lattice_user_personas_relation_isNull = true;
      } else {
        iterate_refvec_v1_user_personas(runtime, sweep, reader);
      } // END reference-vector path='user.personas:52' version=1 ordinal=5
      sweep.referenceScalarSplice(runtime, 52, 8) // user.personas ChildMergePlace
      // -------- START reference-vector path='user.segments:46' version=1 ordinal=3 --------------
      if (sweep.skipVisitPath(46) || runtime.lattice_user_relation.relationIsNull(reader, schema_structure_User_V1_schematic, 3)) {
        runtime.lattice_user_segments_relation_isNull = true;
      } else {
        iterate_refvec_v1_user_segments(runtime, sweep, reader);
      } // END reference-vector path='user.segments:46' version=1 ordinal=3
      sweep.referenceScalarSplice(runtime, 46, 8) // user.segments ChildMergePlace
      // -------- START value-map path='user.parameters:56' version=1 ordinal=8 -------------------
      if (sweep.skipVisitPath(56) || runtime.lattice_user_relation.relationIsNull(reader, schema_structure_User_V1_schematic, 8)) {
        runtime.lattice_user_parameters_relation_isNull = true;
      } else {
        iterate_valmap_v1_user_parameters(runtime, sweep, reader);
      } // END value-map path='user.parameters:56' version=1 ordinal=8
      sweep.referenceScalarSplice(runtime, 56, 8) // user.parameters ChildMergePlace
      // -------- visit dynamic child relation(s) after static ones  ------------------------------
      sweep.dynamicRelationSplices( runtime, 1, 17 ); // 'user' 'DynamicVisitPlace'
      // -------- END CHILD RELATION VISITS 'user' ------------------------------------------------ 
      sweep.referenceScalarSplice(runtime, 1, 2) // user InstancePostPlace
      // -------- START CHILD RELATION JOINS (AFTER POST) 'user' ----------------------------------
      // -------- join static child relation(s) before dynamic ones  ------------------------------
      sweep.referenceScalarSplice(runtime, 48, 9)  // user.channels ChildJoinPlace
      sweep.referenceScalarSplice(runtime, 3, 9)  // user.project ChildJoinPlace
      sweep.referenceScalarSplice(runtime, 16, 9)  // user.sessions ChildJoinPlace
      sweep.referenceScalarSplice(runtime, 52, 9)  // user.personas ChildJoinPlace
      sweep.referenceScalarSplice(runtime, 46, 9)  // user.segments ChildJoinPlace
      sweep.referenceScalarSplice(runtime, 56, 9)  // user.parameters ChildJoinPlace
      // -------- join dynamic child relation(s) after static ones  -------------------------------
      sweep.dynamicRelationSplices( runtime, 1, 18  ); // 'user'  'DynamicJoinPlace'
      // -------- END CHILD RELATION JOINS 'user' -------------------------------------------------
      // -------- DYNAMIC RELATIONS CLEANUP 'user' ------------------------------------------------
      sweep.dynamicRelationSplices( runtime, 1, 19  ); // 'user'  'DynamicCleanupPlace'
      sweep.referenceScalarSplice(runtime, 1, 7) // user InstanceFreePlace
    }
    @inline final def search_v2_user(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      sweep.referenceScalarSplice(runtime, 1, 6) // user InstanceAllocPlace
      sweep.referenceScalarSplice(runtime, 1, 1) // user InstancePrePlace
      // -------- START CHILD RELATION VISITS (BEF|E POST) 'user'  -------------------------------
      // -------- visit static child relation(s) before dynamic ones  -----------------------------
      // -------- START reference-vector path='user.channels:48' version=2 ordinal=4 --------------
      if (sweep.skipVisitPath(48) || runtime.lattice_user_relation.relationIsNull(reader, schema_structure_User_V2_schematic, 4)) {
        runtime.lattice_user_channels_relation_isNull = true;
      } else {
        iterate_refvec_v2_user_channels(runtime, sweep, reader);
      } // END reference-vector path='user.channels:48' version=2 ordinal=4
      sweep.referenceScalarSplice(runtime, 48, 8) // user.channels ChildMergePlace
      // -------- START reference-scalar path='user.project:3' version=2 ordinal=1 ----------------
      if (sweep.skipVisitPath(3) || runtime.lattice_user_relation.relationIsNull(reader, schema_structure_User_V2_schematic, 1)) {
        if (sweep.skipTunnelPath(3)) {
          runtime.lattice_user_project_relation_isNull = true;
        }
      } else {
        runtime.lattice_user_project_relation_isNull = false;
        runtime.lattice_user_project_relation = runtime.lattice_user_relation.referenceScalar(reader, schema_structure_User_V2_schematic, 1);
        { // BEGIN reference scalar relations for 'user.project'
          runtime.lattice_user_project_relation.versionKey(reader) match {
            case 1 ⇒ { // schema version 1
              search_v1_user_project(runtime, sweep, reader);
            }
            case 2 ⇒ { // schema version 2
              search_v2_user_project(runtime, sweep, reader);
            }
            case 3 ⇒ { // schema version 3
              search_v3_user_project(runtime, sweep, reader);
            }
          }
        } // END reference scalar relations for 'user.project' 
      } // END reference-scalar path='user.project:3' version=2 ordinal=1
      sweep.referenceScalarSplice(runtime, 3, 8) // user.project ChildMergePlace
      // -------- START reference-vector path='user.sessions:16' version=2 ordinal=2 --------------
      if (sweep.skipVisitPath(16) || runtime.lattice_user_relation.relationIsNull(reader, schema_structure_User_V2_schematic, 2)) {
        runtime.lattice_user_sessions_relation_isNull = true;
      } else {
        iterate_refvec_v2_user_sessions(runtime, sweep, reader);
      } // END reference-vector path='user.sessions:16' version=2 ordinal=2
      sweep.referenceScalarSplice(runtime, 16, 8) // user.sessions ChildMergePlace
      // -------- START reference-vector path='user.personas:52' version=2 ordinal=5 --------------
      if (sweep.skipVisitPath(52) || runtime.lattice_user_relation.relationIsNull(reader, schema_structure_User_V2_schematic, 5)) {
        runtime.lattice_user_personas_relation_isNull = true;
      } else {
        iterate_refvec_v2_user_personas(runtime, sweep, reader);
      } // END reference-vector path='user.personas:52' version=2 ordinal=5
      sweep.referenceScalarSplice(runtime, 52, 8) // user.personas ChildMergePlace
      // -------- START reference-vector path='user.segments:46' version=2 ordinal=3 --------------
      if (sweep.skipVisitPath(46) || runtime.lattice_user_relation.relationIsNull(reader, schema_structure_User_V2_schematic, 3)) {
        runtime.lattice_user_segments_relation_isNull = true;
      } else {
        iterate_refvec_v2_user_segments(runtime, sweep, reader);
      } // END reference-vector path='user.segments:46' version=2 ordinal=3
      sweep.referenceScalarSplice(runtime, 46, 8) // user.segments ChildMergePlace
      // -------- START value-map path='user.parameters:56' version=2 ordinal=8 -------------------
      if (sweep.skipVisitPath(56) || runtime.lattice_user_relation.relationIsNull(reader, schema_structure_User_V2_schematic, 8)) {
        runtime.lattice_user_parameters_relation_isNull = true;
      } else {
        iterate_valmap_v2_user_parameters(runtime, sweep, reader);
      } // END value-map path='user.parameters:56' version=2 ordinal=8
      sweep.referenceScalarSplice(runtime, 56, 8) // user.parameters ChildMergePlace
      // -------- visit dynamic child relation(s) after static ones  ------------------------------
      sweep.dynamicRelationSplices( runtime, 1, 17 ); // 'user' 'DynamicVisitPlace'
      // -------- END CHILD RELATION VISITS 'user' ------------------------------------------------ 
      sweep.referenceScalarSplice(runtime, 1, 2) // user InstancePostPlace
      // -------- START CHILD RELATION JOINS (AFTER POST) 'user' ----------------------------------
      // -------- join static child relation(s) before dynamic ones  ------------------------------
      sweep.referenceScalarSplice(runtime, 48, 9)  // user.channels ChildJoinPlace
      sweep.referenceScalarSplice(runtime, 3, 9)  // user.project ChildJoinPlace
      sweep.referenceScalarSplice(runtime, 16, 9)  // user.sessions ChildJoinPlace
      sweep.referenceScalarSplice(runtime, 52, 9)  // user.personas ChildJoinPlace
      sweep.referenceScalarSplice(runtime, 46, 9)  // user.segments ChildJoinPlace
      sweep.referenceScalarSplice(runtime, 56, 9)  // user.parameters ChildJoinPlace
      // -------- join dynamic child relation(s) after static ones  -------------------------------
      sweep.dynamicRelationSplices( runtime, 1, 18  ); // 'user'  'DynamicJoinPlace'
      // -------- END CHILD RELATION JOINS 'user' -------------------------------------------------
      // -------- DYNAMIC RELATIONS CLEANUP 'user' ------------------------------------------------
      sweep.dynamicRelationSplices( runtime, 1, 19  ); // 'user'  'DynamicCleanupPlace'
      sweep.referenceScalarSplice(runtime, 1, 7) // user InstanceFreePlace
    }
    @inline final def search_v3_user(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      sweep.referenceScalarSplice(runtime, 1, 6) // user InstanceAllocPlace
      sweep.referenceScalarSplice(runtime, 1, 1) // user InstancePrePlace
      // -------- START CHILD RELATION VISITS (BEF|E POST) 'user'  -------------------------------
      // -------- visit static child relation(s) before dynamic ones  -----------------------------
      // -------- START reference-vector path='user.channels:48' version=3 ordinal=4 --------------
      if (sweep.skipVisitPath(48) || runtime.lattice_user_relation.relationIsNull(reader, schema_structure_User_V3_schematic, 4)) {
        runtime.lattice_user_channels_relation_isNull = true;
      } else {
        iterate_refvec_v3_user_channels(runtime, sweep, reader);
      } // END reference-vector path='user.channels:48' version=3 ordinal=4
      sweep.referenceScalarSplice(runtime, 48, 8) // user.channels ChildMergePlace
      // -------- START reference-scalar path='user.project:3' version=3 ordinal=1 ----------------
      if (sweep.skipVisitPath(3) || runtime.lattice_user_relation.relationIsNull(reader, schema_structure_User_V3_schematic, 1)) {
        if (sweep.skipTunnelPath(3)) {
          runtime.lattice_user_project_relation_isNull = true;
        }
      } else {
        runtime.lattice_user_project_relation_isNull = false;
        runtime.lattice_user_project_relation = runtime.lattice_user_relation.referenceScalar(reader, schema_structure_User_V3_schematic, 1);
        { // BEGIN reference scalar relations for 'user.project'
          runtime.lattice_user_project_relation.versionKey(reader) match {
            case 1 ⇒ { // schema version 1
              search_v1_user_project(runtime, sweep, reader);
            }
            case 2 ⇒ { // schema version 2
              search_v2_user_project(runtime, sweep, reader);
            }
            case 3 ⇒ { // schema version 3
              search_v3_user_project(runtime, sweep, reader);
            }
          }
        } // END reference scalar relations for 'user.project' 
      } // END reference-scalar path='user.project:3' version=3 ordinal=1
      sweep.referenceScalarSplice(runtime, 3, 8) // user.project ChildMergePlace
      // -------- START reference-vector path='user.sessions:16' version=3 ordinal=2 --------------
      if (sweep.skipVisitPath(16) || runtime.lattice_user_relation.relationIsNull(reader, schema_structure_User_V3_schematic, 2)) {
        runtime.lattice_user_sessions_relation_isNull = true;
      } else {
        iterate_refvec_v3_user_sessions(runtime, sweep, reader);
      } // END reference-vector path='user.sessions:16' version=3 ordinal=2
      sweep.referenceScalarSplice(runtime, 16, 8) // user.sessions ChildMergePlace
      // -------- START reference-vector path='user.personas:52' version=3 ordinal=5 --------------
      if (sweep.skipVisitPath(52) || runtime.lattice_user_relation.relationIsNull(reader, schema_structure_User_V3_schematic, 5)) {
        runtime.lattice_user_personas_relation_isNull = true;
      } else {
        iterate_refvec_v3_user_personas(runtime, sweep, reader);
      } // END reference-vector path='user.personas:52' version=3 ordinal=5
      sweep.referenceScalarSplice(runtime, 52, 8) // user.personas ChildMergePlace
      // -------- START reference-vector path='user.segments:46' version=3 ordinal=3 --------------
      if (sweep.skipVisitPath(46) || runtime.lattice_user_relation.relationIsNull(reader, schema_structure_User_V3_schematic, 3)) {
        runtime.lattice_user_segments_relation_isNull = true;
      } else {
        iterate_refvec_v3_user_segments(runtime, sweep, reader);
      } // END reference-vector path='user.segments:46' version=3 ordinal=3
      sweep.referenceScalarSplice(runtime, 46, 8) // user.segments ChildMergePlace
      // -------- START value-map path='user.parameters:56' version=3 ordinal=8 -------------------
      if (sweep.skipVisitPath(56) || runtime.lattice_user_relation.relationIsNull(reader, schema_structure_User_V3_schematic, 8)) {
        runtime.lattice_user_parameters_relation_isNull = true;
      } else {
        iterate_valmap_v3_user_parameters(runtime, sweep, reader);
      } // END value-map path='user.parameters:56' version=3 ordinal=8
      sweep.referenceScalarSplice(runtime, 56, 8) // user.parameters ChildMergePlace
      // -------- visit dynamic child relation(s) after static ones  ------------------------------
      sweep.dynamicRelationSplices( runtime, 1, 17 ); // 'user' 'DynamicVisitPlace'
      // -------- END CHILD RELATION VISITS 'user' ------------------------------------------------ 
      sweep.referenceScalarSplice(runtime, 1, 2) // user InstancePostPlace
      // -------- START CHILD RELATION JOINS (AFTER POST) 'user' ----------------------------------
      // -------- join static child relation(s) before dynamic ones  ------------------------------
      sweep.referenceScalarSplice(runtime, 48, 9)  // user.channels ChildJoinPlace
      sweep.referenceScalarSplice(runtime, 3, 9)  // user.project ChildJoinPlace
      sweep.referenceScalarSplice(runtime, 16, 9)  // user.sessions ChildJoinPlace
      sweep.referenceScalarSplice(runtime, 52, 9)  // user.personas ChildJoinPlace
      sweep.referenceScalarSplice(runtime, 46, 9)  // user.segments ChildJoinPlace
      sweep.referenceScalarSplice(runtime, 56, 9)  // user.parameters ChildJoinPlace
      // -------- join dynamic child relation(s) after static ones  -------------------------------
      sweep.dynamicRelationSplices( runtime, 1, 18  ); // 'user'  'DynamicJoinPlace'
      // -------- END CHILD RELATION JOINS 'user' -------------------------------------------------
      // -------- DYNAMIC RELATIONS CLEANUP 'user' ------------------------------------------------
      sweep.dynamicRelationSplices( runtime, 1, 19  ); // 'user'  'DynamicCleanupPlace'
      sweep.referenceScalarSplice(runtime, 1, 7) // user InstanceFreePlace
    }
    @inline final def iterate_refvec_v1_user_channels(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      runtime.lattice_user_channels_relation_isNull = false;
      runtime.lattice_user_channels_vector_is_first = false;
      runtime.lattice_user_channels_vector_is_last = false;
      val memberIterator = runtime.lattice_user_relation.referenceVectorIterator(reader, schema_structure_User_V1_schematic, 4);
      var vectorIndex = memberIterator.start(reader);
      val memberCount = memberIterator.length(reader);
      sweep.referenceVectorSplice(runtime, 48, 10); // user.channels VectorAllocPlace
      sweep.referenceVectorSplice(runtime, 48, 3); // user.channels VectorBeforePlace
      var i = 0;
      while (i < memberCount && !runtime.skipControlMemberPath(48) ) {
        runtime.lattice_user_channels_vector_is_first = (i == 0);
        runtime.lattice_user_channels_vector_is_last = i == (memberCount - 1);
        runtime.lattice_user_channels_relation = memberIterator.member(reader, vectorIndex);
        sweep.referenceVectorMemberSplice(runtime, 48, 12); // user.channels VectorMemberAllocPlace
        { // BEGIN reference scalar relations for 'user.channels'
          runtime.lattice_user_channels_relation.versionKey(reader) match {
            case 1 ⇒ { // schema version 1
              search_v1_user_channels(runtime, sweep, reader);
            }
            case 2 ⇒ { // schema version 2
              search_v2_user_channels(runtime, sweep, reader);
            }
            case 3 ⇒ { // schema version 3
              search_v3_user_channels(runtime, sweep, reader);
            }
          }
        } // END reference scalar relations for 'user.channels' 
        sweep.referenceVectorMemberSplice(runtime, 48, 14); // user.channels VectorMemberMergePlace
        sweep.referenceVectorMemberSplice(runtime, 48, 13); // user.channels VectorMemberFreePlace
        vectorIndex = memberIterator.advance(reader, vectorIndex);
        i += 1;
      }
      runtime.lattice_user_channels_vector_is_first = false; // this should be false for 'after' splices
      runtime.lattice_user_channels_vector_is_last = false; // this should be false for 'after' splices
      sweep.referenceVectorSplice(runtime, 48, 4); // user.channels VectorAfterPlace
      sweep.referenceVectorSplice(runtime, 48, 11); // user.channels VectorFreePlace
    }
    @inline final def search_v1_user_project(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      sweep.referenceScalarSplice(runtime, 3, 6) // user.project InstanceAllocPlace
      sweep.referenceScalarSplice(runtime, 3, 1) // user.project InstancePrePlace
      // -------- START CHILD RELATION VISITS (BEF|E POST) 'user.project'  -----------------------
      // -------- visit static child relation(s) before dynamic ones  -----------------------------
      // -------- visit dynamic child relation(s) after static ones  ------------------------------
      sweep.dynamicRelationSplices( runtime, 3, 17 ); // 'user.project' 'DynamicVisitPlace'
      // -------- END CHILD RELATION VISITS 'user.project' ---------------------------------------- 
      sweep.referenceScalarSplice(runtime, 3, 2) // user.project InstancePostPlace
      // -------- START CHILD RELATION JOINS (AFTER POST) 'user.project' --------------------------
      // -------- join static child relation(s) before dynamic ones  ------------------------------
      // -------- join dynamic child relation(s) after static ones  -------------------------------
      sweep.dynamicRelationSplices( runtime, 3, 18  ); // 'user.project'  'DynamicJoinPlace'
      // -------- END CHILD RELATION JOINS 'user.project' -----------------------------------------
      // -------- DYNAMIC RELATIONS CLEANUP 'user.project' ----------------------------------------
      sweep.dynamicRelationSplices( runtime, 3, 19  ); // 'user.project'  'DynamicCleanupPlace'
      sweep.referenceScalarSplice(runtime, 3, 7) // user.project InstanceFreePlace
    }
    @inline final def search_v2_user_project(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      sweep.referenceScalarSplice(runtime, 3, 6) // user.project InstanceAllocPlace
      sweep.referenceScalarSplice(runtime, 3, 1) // user.project InstancePrePlace
      // -------- START CHILD RELATION VISITS (BEF|E POST) 'user.project'  -----------------------
      // -------- visit static child relation(s) before dynamic ones  -----------------------------
      // -------- visit dynamic child relation(s) after static ones  ------------------------------
      sweep.dynamicRelationSplices( runtime, 3, 17 ); // 'user.project' 'DynamicVisitPlace'
      // -------- END CHILD RELATION VISITS 'user.project' ---------------------------------------- 
      sweep.referenceScalarSplice(runtime, 3, 2) // user.project InstancePostPlace
      // -------- START CHILD RELATION JOINS (AFTER POST) 'user.project' --------------------------
      // -------- join static child relation(s) before dynamic ones  ------------------------------
      // -------- join dynamic child relation(s) after static ones  -------------------------------
      sweep.dynamicRelationSplices( runtime, 3, 18  ); // 'user.project'  'DynamicJoinPlace'
      // -------- END CHILD RELATION JOINS 'user.project' -----------------------------------------
      // -------- DYNAMIC RELATIONS CLEANUP 'user.project' ----------------------------------------
      sweep.dynamicRelationSplices( runtime, 3, 19  ); // 'user.project'  'DynamicCleanupPlace'
      sweep.referenceScalarSplice(runtime, 3, 7) // user.project InstanceFreePlace
    }
    @inline final def search_v3_user_project(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      sweep.referenceScalarSplice(runtime, 3, 6) // user.project InstanceAllocPlace
      sweep.referenceScalarSplice(runtime, 3, 1) // user.project InstancePrePlace
      // -------- START CHILD RELATION VISITS (BEF|E POST) 'user.project'  -----------------------
      // -------- visit static child relation(s) before dynamic ones  -----------------------------
      // -------- visit dynamic child relation(s) after static ones  ------------------------------
      sweep.dynamicRelationSplices( runtime, 3, 17 ); // 'user.project' 'DynamicVisitPlace'
      // -------- END CHILD RELATION VISITS 'user.project' ---------------------------------------- 
      sweep.referenceScalarSplice(runtime, 3, 2) // user.project InstancePostPlace
      // -------- START CHILD RELATION JOINS (AFTER POST) 'user.project' --------------------------
      // -------- join static child relation(s) before dynamic ones  ------------------------------
      // -------- join dynamic child relation(s) after static ones  -------------------------------
      sweep.dynamicRelationSplices( runtime, 3, 18  ); // 'user.project'  'DynamicJoinPlace'
      // -------- END CHILD RELATION JOINS 'user.project' -----------------------------------------
      // -------- DYNAMIC RELATIONS CLEANUP 'user.project' ----------------------------------------
      sweep.dynamicRelationSplices( runtime, 3, 19  ); // 'user.project'  'DynamicCleanupPlace'
      sweep.referenceScalarSplice(runtime, 3, 7) // user.project InstanceFreePlace
    }
    @inline final def iterate_refvec_v1_user_sessions(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      runtime.lattice_user_sessions_relation_isNull = false;
      runtime.lattice_user_sessions_vector_is_first = false;
      runtime.lattice_user_sessions_vector_is_last = false;
      val memberIterator = runtime.lattice_user_relation.referenceVectorIterator(reader, schema_structure_User_V1_schematic, 2);
      var vectorIndex = memberIterator.start(reader);
      val memberCount = memberIterator.length(reader);
      sweep.referenceVectorSplice(runtime, 16, 10); // user.sessions VectorAllocPlace
      sweep.referenceVectorSplice(runtime, 16, 3); // user.sessions VectorBeforePlace
      var i = 0;
      while (i < memberCount && !runtime.skipControlMemberPath(16) ) {
        runtime.lattice_user_sessions_vector_is_first = (i == 0);
        runtime.lattice_user_sessions_vector_is_last = i == (memberCount - 1);
        runtime.lattice_user_sessions_relation = memberIterator.member(reader, vectorIndex);
        sweep.referenceVectorMemberSplice(runtime, 16, 12); // user.sessions VectorMemberAllocPlace
        { // BEGIN reference scalar relations for 'user.sessions'
          runtime.lattice_user_sessions_relation.versionKey(reader) match {
            case 1 ⇒ { // schema version 1
              search_v1_user_sessions(runtime, sweep, reader);
            }
            case 2 ⇒ { // schema version 2
              search_v2_user_sessions(runtime, sweep, reader);
            }
            case 3 ⇒ { // schema version 3
              search_v3_user_sessions(runtime, sweep, reader);
            }
          }
        } // END reference scalar relations for 'user.sessions' 
        sweep.referenceVectorMemberSplice(runtime, 16, 14); // user.sessions VectorMemberMergePlace
        sweep.referenceVectorMemberSplice(runtime, 16, 13); // user.sessions VectorMemberFreePlace
        vectorIndex = memberIterator.advance(reader, vectorIndex);
        i += 1;
      }
      runtime.lattice_user_sessions_vector_is_first = false; // this should be false for 'after' splices
      runtime.lattice_user_sessions_vector_is_last = false; // this should be false for 'after' splices
      sweep.referenceVectorSplice(runtime, 16, 4); // user.sessions VectorAfterPlace
      sweep.referenceVectorSplice(runtime, 16, 11); // user.sessions VectorFreePlace
    }
    @inline final def iterate_refvec_v1_user_personas(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      runtime.lattice_user_personas_relation_isNull = false;
      runtime.lattice_user_personas_vector_is_first = false;
      runtime.lattice_user_personas_vector_is_last = false;
      val memberIterator = runtime.lattice_user_relation.referenceVectorIterator(reader, schema_structure_User_V1_schematic, 5);
      var vectorIndex = memberIterator.start(reader);
      val memberCount = memberIterator.length(reader);
      sweep.referenceVectorSplice(runtime, 52, 10); // user.personas VectorAllocPlace
      sweep.referenceVectorSplice(runtime, 52, 3); // user.personas VectorBeforePlace
      var i = 0;
      while (i < memberCount && !runtime.skipControlMemberPath(52) ) {
        runtime.lattice_user_personas_vector_is_first = (i == 0);
        runtime.lattice_user_personas_vector_is_last = i == (memberCount - 1);
        runtime.lattice_user_personas_relation = memberIterator.member(reader, vectorIndex);
        sweep.referenceVectorMemberSplice(runtime, 52, 12); // user.personas VectorMemberAllocPlace
        { // BEGIN reference scalar relations for 'user.personas'
          runtime.lattice_user_personas_relation.versionKey(reader) match {
            case 1 ⇒ { // schema version 1
              search_v1_user_personas(runtime, sweep, reader);
            }
            case 2 ⇒ { // schema version 2
              search_v2_user_personas(runtime, sweep, reader);
            }
            case 3 ⇒ { // schema version 3
              search_v3_user_personas(runtime, sweep, reader);
            }
          }
        } // END reference scalar relations for 'user.personas' 
        sweep.referenceVectorMemberSplice(runtime, 52, 14); // user.personas VectorMemberMergePlace
        sweep.referenceVectorMemberSplice(runtime, 52, 13); // user.personas VectorMemberFreePlace
        vectorIndex = memberIterator.advance(reader, vectorIndex);
        i += 1;
      }
      runtime.lattice_user_personas_vector_is_first = false; // this should be false for 'after' splices
      runtime.lattice_user_personas_vector_is_last = false; // this should be false for 'after' splices
      sweep.referenceVectorSplice(runtime, 52, 4); // user.personas VectorAfterPlace
      sweep.referenceVectorSplice(runtime, 52, 11); // user.personas VectorFreePlace
    }
    @inline final def iterate_refvec_v1_user_segments(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      runtime.lattice_user_segments_relation_isNull = false;
      runtime.lattice_user_segments_vector_is_first = false;
      runtime.lattice_user_segments_vector_is_last = false;
      val memberIterator = runtime.lattice_user_relation.referenceVectorIterator(reader, schema_structure_User_V1_schematic, 3);
      var vectorIndex = memberIterator.start(reader);
      val memberCount = memberIterator.length(reader);
      sweep.referenceVectorSplice(runtime, 46, 10); // user.segments VectorAllocPlace
      sweep.referenceVectorSplice(runtime, 46, 3); // user.segments VectorBeforePlace
      var i = 0;
      while (i < memberCount && !runtime.skipControlMemberPath(46) ) {
        runtime.lattice_user_segments_vector_is_first = (i == 0);
        runtime.lattice_user_segments_vector_is_last = i == (memberCount - 1);
        runtime.lattice_user_segments_relation = memberIterator.member(reader, vectorIndex);
        sweep.referenceVectorMemberSplice(runtime, 46, 12); // user.segments VectorMemberAllocPlace
        { // BEGIN reference scalar relations for 'user.segments'
          runtime.lattice_user_segments_relation.versionKey(reader) match {
            case 1 ⇒ { // schema version 1
              search_v1_user_segments(runtime, sweep, reader);
            }
            case 2 ⇒ { // schema version 2
              search_v2_user_segments(runtime, sweep, reader);
            }
            case 3 ⇒ { // schema version 3
              search_v3_user_segments(runtime, sweep, reader);
            }
          }
        } // END reference scalar relations for 'user.segments' 
        sweep.referenceVectorMemberSplice(runtime, 46, 14); // user.segments VectorMemberMergePlace
        sweep.referenceVectorMemberSplice(runtime, 46, 13); // user.segments VectorMemberFreePlace
        vectorIndex = memberIterator.advance(reader, vectorIndex);
        i += 1;
      }
      runtime.lattice_user_segments_vector_is_first = false; // this should be false for 'after' splices
      runtime.lattice_user_segments_vector_is_last = false; // this should be false for 'after' splices
      sweep.referenceVectorSplice(runtime, 46, 4); // user.segments VectorAfterPlace
      sweep.referenceVectorSplice(runtime, 46, 11); // user.segments VectorFreePlace
    }
    @inline final def iterate_valmap_v1_user_parameters(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      runtime.lattice_user_parameters_relation_isNull = false;
      runtime.lattice_user_parameters_vector_is_first = false;
      runtime.lattice_user_parameters_vector_is_last = false;
      val memberIterator = runtime.lattice_user_relation.valueMapIterator(reader, schema_structure_User_V1_schematic, 8);
      val memberCount = memberIterator.length(reader);
      sweep.valueMapSplice(runtime, 56, 10); // user.parameters VectorAllocPlace
      sweep.valueMapSplice(runtime, 56, 3); // user.parameters VectorBeforePlace
      var i = 0;
      while (i < memberCount && !runtime.skipControlMemberPath(56) ) {
        runtime.lattice_user_parameters_vector_is_first = (i == 0);
        runtime.lattice_user_parameters_vector_is_last = i == (memberCount - 1);
        sweep.valueMapMemberSplice(runtime, 56, 12); // user.parameters VectorMemberAllocPlace
        // -------- current member key/value tuple for value map user.parameters --------------------
        runtime.lattice_user_parameters_value_map_key = memberIterator.readLexiconStringKey(i, reader);
        runtime.lattice_user_parameters_value_map_value = memberIterator.readLexiconStringStringValue(i, reader);
        sweep.valueMapMemberSplice(runtime, 56, 5); // user.parameters VectorMemberSituPlace
        sweep.valueMapMemberSplice(runtime, 56, 14); // user.parameters VectorMemberMergePlace
        sweep.valueMapMemberSplice(runtime, 56, 13); // user.parameters VectorMemberFreePlace
        i += 1;
      }
      runtime.lattice_user_parameters_vector_is_first = false; // this should be false for 'after' splices
      runtime.lattice_user_parameters_vector_is_last = false; // this should be false for 'after' splices
      sweep.valueMapSplice(runtime, 56, 4); // user.parameters VectorAfterPlace
      sweep.valueMapSplice(runtime, 56, 11); // user.parameters VectorFreePlace 
    }
    @inline final def iterate_refvec_v2_user_channels(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      runtime.lattice_user_channels_relation_isNull = false;
      runtime.lattice_user_channels_vector_is_first = false;
      runtime.lattice_user_channels_vector_is_last = false;
      val memberIterator = runtime.lattice_user_relation.referenceVectorIterator(reader, schema_structure_User_V2_schematic, 4);
      var vectorIndex = memberIterator.start(reader);
      val memberCount = memberIterator.length(reader);
      sweep.referenceVectorSplice(runtime, 48, 10); // user.channels VectorAllocPlace
      sweep.referenceVectorSplice(runtime, 48, 3); // user.channels VectorBeforePlace
      var i = 0;
      while (i < memberCount && !runtime.skipControlMemberPath(48) ) {
        runtime.lattice_user_channels_vector_is_first = (i == 0);
        runtime.lattice_user_channels_vector_is_last = i == (memberCount - 1);
        runtime.lattice_user_channels_relation = memberIterator.member(reader, vectorIndex);
        sweep.referenceVectorMemberSplice(runtime, 48, 12); // user.channels VectorMemberAllocPlace
        { // BEGIN reference scalar relations for 'user.channels'
          runtime.lattice_user_channels_relation.versionKey(reader) match {
            case 1 ⇒ { // schema version 1
              search_v1_user_channels(runtime, sweep, reader);
            }
            case 2 ⇒ { // schema version 2
              search_v2_user_channels(runtime, sweep, reader);
            }
            case 3 ⇒ { // schema version 3
              search_v3_user_channels(runtime, sweep, reader);
            }
          }
        } // END reference scalar relations for 'user.channels' 
        sweep.referenceVectorMemberSplice(runtime, 48, 14); // user.channels VectorMemberMergePlace
        sweep.referenceVectorMemberSplice(runtime, 48, 13); // user.channels VectorMemberFreePlace
        vectorIndex = memberIterator.advance(reader, vectorIndex);
        i += 1;
      }
      runtime.lattice_user_channels_vector_is_first = false; // this should be false for 'after' splices
      runtime.lattice_user_channels_vector_is_last = false; // this should be false for 'after' splices
      sweep.referenceVectorSplice(runtime, 48, 4); // user.channels VectorAfterPlace
      sweep.referenceVectorSplice(runtime, 48, 11); // user.channels VectorFreePlace
    }
    @inline final def iterate_refvec_v2_user_sessions(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      runtime.lattice_user_sessions_relation_isNull = false;
      runtime.lattice_user_sessions_vector_is_first = false;
      runtime.lattice_user_sessions_vector_is_last = false;
      val memberIterator = runtime.lattice_user_relation.referenceVectorIterator(reader, schema_structure_User_V2_schematic, 2);
      var vectorIndex = memberIterator.start(reader);
      val memberCount = memberIterator.length(reader);
      sweep.referenceVectorSplice(runtime, 16, 10); // user.sessions VectorAllocPlace
      sweep.referenceVectorSplice(runtime, 16, 3); // user.sessions VectorBeforePlace
      var i = 0;
      while (i < memberCount && !runtime.skipControlMemberPath(16) ) {
        runtime.lattice_user_sessions_vector_is_first = (i == 0);
        runtime.lattice_user_sessions_vector_is_last = i == (memberCount - 1);
        runtime.lattice_user_sessions_relation = memberIterator.member(reader, vectorIndex);
        sweep.referenceVectorMemberSplice(runtime, 16, 12); // user.sessions VectorMemberAllocPlace
        { // BEGIN reference scalar relations for 'user.sessions'
          runtime.lattice_user_sessions_relation.versionKey(reader) match {
            case 1 ⇒ { // schema version 1
              search_v1_user_sessions(runtime, sweep, reader);
            }
            case 2 ⇒ { // schema version 2
              search_v2_user_sessions(runtime, sweep, reader);
            }
            case 3 ⇒ { // schema version 3
              search_v3_user_sessions(runtime, sweep, reader);
            }
          }
        } // END reference scalar relations for 'user.sessions' 
        sweep.referenceVectorMemberSplice(runtime, 16, 14); // user.sessions VectorMemberMergePlace
        sweep.referenceVectorMemberSplice(runtime, 16, 13); // user.sessions VectorMemberFreePlace
        vectorIndex = memberIterator.advance(reader, vectorIndex);
        i += 1;
      }
      runtime.lattice_user_sessions_vector_is_first = false; // this should be false for 'after' splices
      runtime.lattice_user_sessions_vector_is_last = false; // this should be false for 'after' splices
      sweep.referenceVectorSplice(runtime, 16, 4); // user.sessions VectorAfterPlace
      sweep.referenceVectorSplice(runtime, 16, 11); // user.sessions VectorFreePlace
    }
    @inline final def iterate_refvec_v2_user_personas(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      runtime.lattice_user_personas_relation_isNull = false;
      runtime.lattice_user_personas_vector_is_first = false;
      runtime.lattice_user_personas_vector_is_last = false;
      val memberIterator = runtime.lattice_user_relation.referenceVectorIterator(reader, schema_structure_User_V2_schematic, 5);
      var vectorIndex = memberIterator.start(reader);
      val memberCount = memberIterator.length(reader);
      sweep.referenceVectorSplice(runtime, 52, 10); // user.personas VectorAllocPlace
      sweep.referenceVectorSplice(runtime, 52, 3); // user.personas VectorBeforePlace
      var i = 0;
      while (i < memberCount && !runtime.skipControlMemberPath(52) ) {
        runtime.lattice_user_personas_vector_is_first = (i == 0);
        runtime.lattice_user_personas_vector_is_last = i == (memberCount - 1);
        runtime.lattice_user_personas_relation = memberIterator.member(reader, vectorIndex);
        sweep.referenceVectorMemberSplice(runtime, 52, 12); // user.personas VectorMemberAllocPlace
        { // BEGIN reference scalar relations for 'user.personas'
          runtime.lattice_user_personas_relation.versionKey(reader) match {
            case 1 ⇒ { // schema version 1
              search_v1_user_personas(runtime, sweep, reader);
            }
            case 2 ⇒ { // schema version 2
              search_v2_user_personas(runtime, sweep, reader);
            }
            case 3 ⇒ { // schema version 3
              search_v3_user_personas(runtime, sweep, reader);
            }
          }
        } // END reference scalar relations for 'user.personas' 
        sweep.referenceVectorMemberSplice(runtime, 52, 14); // user.personas VectorMemberMergePlace
        sweep.referenceVectorMemberSplice(runtime, 52, 13); // user.personas VectorMemberFreePlace
        vectorIndex = memberIterator.advance(reader, vectorIndex);
        i += 1;
      }
      runtime.lattice_user_personas_vector_is_first = false; // this should be false for 'after' splices
      runtime.lattice_user_personas_vector_is_last = false; // this should be false for 'after' splices
      sweep.referenceVectorSplice(runtime, 52, 4); // user.personas VectorAfterPlace
      sweep.referenceVectorSplice(runtime, 52, 11); // user.personas VectorFreePlace
    }
    @inline final def iterate_refvec_v2_user_segments(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      runtime.lattice_user_segments_relation_isNull = false;
      runtime.lattice_user_segments_vector_is_first = false;
      runtime.lattice_user_segments_vector_is_last = false;
      val memberIterator = runtime.lattice_user_relation.referenceVectorIterator(reader, schema_structure_User_V2_schematic, 3);
      var vectorIndex = memberIterator.start(reader);
      val memberCount = memberIterator.length(reader);
      sweep.referenceVectorSplice(runtime, 46, 10); // user.segments VectorAllocPlace
      sweep.referenceVectorSplice(runtime, 46, 3); // user.segments VectorBeforePlace
      var i = 0;
      while (i < memberCount && !runtime.skipControlMemberPath(46) ) {
        runtime.lattice_user_segments_vector_is_first = (i == 0);
        runtime.lattice_user_segments_vector_is_last = i == (memberCount - 1);
        runtime.lattice_user_segments_relation = memberIterator.member(reader, vectorIndex);
        sweep.referenceVectorMemberSplice(runtime, 46, 12); // user.segments VectorMemberAllocPlace
        { // BEGIN reference scalar relations for 'user.segments'
          runtime.lattice_user_segments_relation.versionKey(reader) match {
            case 1 ⇒ { // schema version 1
              search_v1_user_segments(runtime, sweep, reader);
            }
            case 2 ⇒ { // schema version 2
              search_v2_user_segments(runtime, sweep, reader);
            }
            case 3 ⇒ { // schema version 3
              search_v3_user_segments(runtime, sweep, reader);
            }
          }
        } // END reference scalar relations for 'user.segments' 
        sweep.referenceVectorMemberSplice(runtime, 46, 14); // user.segments VectorMemberMergePlace
        sweep.referenceVectorMemberSplice(runtime, 46, 13); // user.segments VectorMemberFreePlace
        vectorIndex = memberIterator.advance(reader, vectorIndex);
        i += 1;
      }
      runtime.lattice_user_segments_vector_is_first = false; // this should be false for 'after' splices
      runtime.lattice_user_segments_vector_is_last = false; // this should be false for 'after' splices
      sweep.referenceVectorSplice(runtime, 46, 4); // user.segments VectorAfterPlace
      sweep.referenceVectorSplice(runtime, 46, 11); // user.segments VectorFreePlace
    }
    @inline final def iterate_valmap_v2_user_parameters(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      runtime.lattice_user_parameters_relation_isNull = false;
      runtime.lattice_user_parameters_vector_is_first = false;
      runtime.lattice_user_parameters_vector_is_last = false;
      val memberIterator = runtime.lattice_user_relation.valueMapIterator(reader, schema_structure_User_V2_schematic, 8);
      val memberCount = memberIterator.length(reader);
      sweep.valueMapSplice(runtime, 56, 10); // user.parameters VectorAllocPlace
      sweep.valueMapSplice(runtime, 56, 3); // user.parameters VectorBeforePlace
      var i = 0;
      while (i < memberCount && !runtime.skipControlMemberPath(56) ) {
        runtime.lattice_user_parameters_vector_is_first = (i == 0);
        runtime.lattice_user_parameters_vector_is_last = i == (memberCount - 1);
        sweep.valueMapMemberSplice(runtime, 56, 12); // user.parameters VectorMemberAllocPlace
        // -------- current member key/value tuple for value map user.parameters --------------------
        runtime.lattice_user_parameters_value_map_key = memberIterator.readLexiconStringKey(i, reader);
        runtime.lattice_user_parameters_value_map_value = memberIterator.readLexiconStringStringValue(i, reader);
        sweep.valueMapMemberSplice(runtime, 56, 5); // user.parameters VectorMemberSituPlace
        sweep.valueMapMemberSplice(runtime, 56, 14); // user.parameters VectorMemberMergePlace
        sweep.valueMapMemberSplice(runtime, 56, 13); // user.parameters VectorMemberFreePlace
        i += 1;
      }
      runtime.lattice_user_parameters_vector_is_first = false; // this should be false for 'after' splices
      runtime.lattice_user_parameters_vector_is_last = false; // this should be false for 'after' splices
      sweep.valueMapSplice(runtime, 56, 4); // user.parameters VectorAfterPlace
      sweep.valueMapSplice(runtime, 56, 11); // user.parameters VectorFreePlace 
    }
    @inline final def iterate_refvec_v3_user_channels(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      runtime.lattice_user_channels_relation_isNull = false;
      runtime.lattice_user_channels_vector_is_first = false;
      runtime.lattice_user_channels_vector_is_last = false;
      val memberIterator = runtime.lattice_user_relation.referenceVectorIterator(reader, schema_structure_User_V3_schematic, 4);
      var vectorIndex = memberIterator.start(reader);
      val memberCount = memberIterator.length(reader);
      sweep.referenceVectorSplice(runtime, 48, 10); // user.channels VectorAllocPlace
      sweep.referenceVectorSplice(runtime, 48, 3); // user.channels VectorBeforePlace
      var i = 0;
      while (i < memberCount && !runtime.skipControlMemberPath(48) ) {
        runtime.lattice_user_channels_vector_is_first = (i == 0);
        runtime.lattice_user_channels_vector_is_last = i == (memberCount - 1);
        runtime.lattice_user_channels_relation = memberIterator.member(reader, vectorIndex);
        sweep.referenceVectorMemberSplice(runtime, 48, 12); // user.channels VectorMemberAllocPlace
        { // BEGIN reference scalar relations for 'user.channels'
          runtime.lattice_user_channels_relation.versionKey(reader) match {
            case 1 ⇒ { // schema version 1
              search_v1_user_channels(runtime, sweep, reader);
            }
            case 2 ⇒ { // schema version 2
              search_v2_user_channels(runtime, sweep, reader);
            }
            case 3 ⇒ { // schema version 3
              search_v3_user_channels(runtime, sweep, reader);
            }
          }
        } // END reference scalar relations for 'user.channels' 
        sweep.referenceVectorMemberSplice(runtime, 48, 14); // user.channels VectorMemberMergePlace
        sweep.referenceVectorMemberSplice(runtime, 48, 13); // user.channels VectorMemberFreePlace
        vectorIndex = memberIterator.advance(reader, vectorIndex);
        i += 1;
      }
      runtime.lattice_user_channels_vector_is_first = false; // this should be false for 'after' splices
      runtime.lattice_user_channels_vector_is_last = false; // this should be false for 'after' splices
      sweep.referenceVectorSplice(runtime, 48, 4); // user.channels VectorAfterPlace
      sweep.referenceVectorSplice(runtime, 48, 11); // user.channels VectorFreePlace
    }
    @inline final def iterate_refvec_v3_user_sessions(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      runtime.lattice_user_sessions_relation_isNull = false;
      runtime.lattice_user_sessions_vector_is_first = false;
      runtime.lattice_user_sessions_vector_is_last = false;
      val memberIterator = runtime.lattice_user_relation.referenceVectorIterator(reader, schema_structure_User_V3_schematic, 2);
      var vectorIndex = memberIterator.start(reader);
      val memberCount = memberIterator.length(reader);
      sweep.referenceVectorSplice(runtime, 16, 10); // user.sessions VectorAllocPlace
      sweep.referenceVectorSplice(runtime, 16, 3); // user.sessions VectorBeforePlace
      var i = 0;
      while (i < memberCount && !runtime.skipControlMemberPath(16) ) {
        runtime.lattice_user_sessions_vector_is_first = (i == 0);
        runtime.lattice_user_sessions_vector_is_last = i == (memberCount - 1);
        runtime.lattice_user_sessions_relation = memberIterator.member(reader, vectorIndex);
        sweep.referenceVectorMemberSplice(runtime, 16, 12); // user.sessions VectorMemberAllocPlace
        { // BEGIN reference scalar relations for 'user.sessions'
          runtime.lattice_user_sessions_relation.versionKey(reader) match {
            case 1 ⇒ { // schema version 1
              search_v1_user_sessions(runtime, sweep, reader);
            }
            case 2 ⇒ { // schema version 2
              search_v2_user_sessions(runtime, sweep, reader);
            }
            case 3 ⇒ { // schema version 3
              search_v3_user_sessions(runtime, sweep, reader);
            }
          }
        } // END reference scalar relations for 'user.sessions' 
        sweep.referenceVectorMemberSplice(runtime, 16, 14); // user.sessions VectorMemberMergePlace
        sweep.referenceVectorMemberSplice(runtime, 16, 13); // user.sessions VectorMemberFreePlace
        vectorIndex = memberIterator.advance(reader, vectorIndex);
        i += 1;
      }
      runtime.lattice_user_sessions_vector_is_first = false; // this should be false for 'after' splices
      runtime.lattice_user_sessions_vector_is_last = false; // this should be false for 'after' splices
      sweep.referenceVectorSplice(runtime, 16, 4); // user.sessions VectorAfterPlace
      sweep.referenceVectorSplice(runtime, 16, 11); // user.sessions VectorFreePlace
    }
    @inline final def iterate_refvec_v3_user_personas(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      runtime.lattice_user_personas_relation_isNull = false;
      runtime.lattice_user_personas_vector_is_first = false;
      runtime.lattice_user_personas_vector_is_last = false;
      val memberIterator = runtime.lattice_user_relation.referenceVectorIterator(reader, schema_structure_User_V3_schematic, 5);
      var vectorIndex = memberIterator.start(reader);
      val memberCount = memberIterator.length(reader);
      sweep.referenceVectorSplice(runtime, 52, 10); // user.personas VectorAllocPlace
      sweep.referenceVectorSplice(runtime, 52, 3); // user.personas VectorBeforePlace
      var i = 0;
      while (i < memberCount && !runtime.skipControlMemberPath(52) ) {
        runtime.lattice_user_personas_vector_is_first = (i == 0);
        runtime.lattice_user_personas_vector_is_last = i == (memberCount - 1);
        runtime.lattice_user_personas_relation = memberIterator.member(reader, vectorIndex);
        sweep.referenceVectorMemberSplice(runtime, 52, 12); // user.personas VectorMemberAllocPlace
        { // BEGIN reference scalar relations for 'user.personas'
          runtime.lattice_user_personas_relation.versionKey(reader) match {
            case 1 ⇒ { // schema version 1
              search_v1_user_personas(runtime, sweep, reader);
            }
            case 2 ⇒ { // schema version 2
              search_v2_user_personas(runtime, sweep, reader);
            }
            case 3 ⇒ { // schema version 3
              search_v3_user_personas(runtime, sweep, reader);
            }
          }
        } // END reference scalar relations for 'user.personas' 
        sweep.referenceVectorMemberSplice(runtime, 52, 14); // user.personas VectorMemberMergePlace
        sweep.referenceVectorMemberSplice(runtime, 52, 13); // user.personas VectorMemberFreePlace
        vectorIndex = memberIterator.advance(reader, vectorIndex);
        i += 1;
      }
      runtime.lattice_user_personas_vector_is_first = false; // this should be false for 'after' splices
      runtime.lattice_user_personas_vector_is_last = false; // this should be false for 'after' splices
      sweep.referenceVectorSplice(runtime, 52, 4); // user.personas VectorAfterPlace
      sweep.referenceVectorSplice(runtime, 52, 11); // user.personas VectorFreePlace
    }
    @inline final def iterate_refvec_v3_user_segments(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      runtime.lattice_user_segments_relation_isNull = false;
      runtime.lattice_user_segments_vector_is_first = false;
      runtime.lattice_user_segments_vector_is_last = false;
      val memberIterator = runtime.lattice_user_relation.referenceVectorIterator(reader, schema_structure_User_V3_schematic, 3);
      var vectorIndex = memberIterator.start(reader);
      val memberCount = memberIterator.length(reader);
      sweep.referenceVectorSplice(runtime, 46, 10); // user.segments VectorAllocPlace
      sweep.referenceVectorSplice(runtime, 46, 3); // user.segments VectorBeforePlace
      var i = 0;
      while (i < memberCount && !runtime.skipControlMemberPath(46) ) {
        runtime.lattice_user_segments_vector_is_first = (i == 0);
        runtime.lattice_user_segments_vector_is_last = i == (memberCount - 1);
        runtime.lattice_user_segments_relation = memberIterator.member(reader, vectorIndex);
        sweep.referenceVectorMemberSplice(runtime, 46, 12); // user.segments VectorMemberAllocPlace
        { // BEGIN reference scalar relations for 'user.segments'
          runtime.lattice_user_segments_relation.versionKey(reader) match {
            case 1 ⇒ { // schema version 1
              search_v1_user_segments(runtime, sweep, reader);
            }
            case 2 ⇒ { // schema version 2
              search_v2_user_segments(runtime, sweep, reader);
            }
            case 3 ⇒ { // schema version 3
              search_v3_user_segments(runtime, sweep, reader);
            }
          }
        } // END reference scalar relations for 'user.segments' 
        sweep.referenceVectorMemberSplice(runtime, 46, 14); // user.segments VectorMemberMergePlace
        sweep.referenceVectorMemberSplice(runtime, 46, 13); // user.segments VectorMemberFreePlace
        vectorIndex = memberIterator.advance(reader, vectorIndex);
        i += 1;
      }
      runtime.lattice_user_segments_vector_is_first = false; // this should be false for 'after' splices
      runtime.lattice_user_segments_vector_is_last = false; // this should be false for 'after' splices
      sweep.referenceVectorSplice(runtime, 46, 4); // user.segments VectorAfterPlace
      sweep.referenceVectorSplice(runtime, 46, 11); // user.segments VectorFreePlace
    }
    @inline final def iterate_valmap_v3_user_parameters(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      runtime.lattice_user_parameters_relation_isNull = false;
      runtime.lattice_user_parameters_vector_is_first = false;
      runtime.lattice_user_parameters_vector_is_last = false;
      val memberIterator = runtime.lattice_user_relation.valueMapIterator(reader, schema_structure_User_V3_schematic, 8);
      val memberCount = memberIterator.length(reader);
      sweep.valueMapSplice(runtime, 56, 10); // user.parameters VectorAllocPlace
      sweep.valueMapSplice(runtime, 56, 3); // user.parameters VectorBeforePlace
      var i = 0;
      while (i < memberCount && !runtime.skipControlMemberPath(56) ) {
        runtime.lattice_user_parameters_vector_is_first = (i == 0);
        runtime.lattice_user_parameters_vector_is_last = i == (memberCount - 1);
        sweep.valueMapMemberSplice(runtime, 56, 12); // user.parameters VectorMemberAllocPlace
        // -------- current member key/value tuple for value map user.parameters --------------------
        runtime.lattice_user_parameters_value_map_key = memberIterator.readLexiconStringKey(i, reader);
        runtime.lattice_user_parameters_value_map_value = memberIterator.readLexiconStringStringValue(i, reader);
        sweep.valueMapMemberSplice(runtime, 56, 5); // user.parameters VectorMemberSituPlace
        sweep.valueMapMemberSplice(runtime, 56, 14); // user.parameters VectorMemberMergePlace
        sweep.valueMapMemberSplice(runtime, 56, 13); // user.parameters VectorMemberFreePlace
        i += 1;
      }
      runtime.lattice_user_parameters_vector_is_first = false; // this should be false for 'after' splices
      runtime.lattice_user_parameters_vector_is_last = false; // this should be false for 'after' splices
      sweep.valueMapSplice(runtime, 56, 4); // user.parameters VectorAfterPlace
      sweep.valueMapSplice(runtime, 56, 11); // user.parameters VectorFreePlace 
    }
    @inline final def search_v1_user_channels(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      sweep.referenceScalarSplice(runtime, 48, 6) // user.channels InstanceAllocPlace
      sweep.referenceScalarSplice(runtime, 48, 1) // user.channels InstancePrePlace
      // -------- START CHILD RELATION VISITS (BEF|E POST) 'user.channels'  ----------------------
      // -------- visit static child relation(s) before dynamic ones  -----------------------------
      // -------- visit dynamic child relation(s) after static ones  ------------------------------
      sweep.dynamicRelationSplices( runtime, 48, 17 ); // 'user.channels' 'DynamicVisitPlace'
      // -------- END CHILD RELATION VISITS 'user.channels' --------------------------------------- 
      sweep.referenceScalarSplice(runtime, 48, 2) // user.channels InstancePostPlace
      // -------- START CHILD RELATION JOINS (AFTER POST) 'user.channels' -------------------------
      // -------- join static child relation(s) before dynamic ones  ------------------------------
      // -------- join dynamic child relation(s) after static ones  -------------------------------
      sweep.dynamicRelationSplices( runtime, 48, 18  ); // 'user.channels'  'DynamicJoinPlace'
      // -------- END CHILD RELATION JOINS 'user.channels' ----------------------------------------
      // -------- DYNAMIC RELATIONS CLEANUP 'user.channels' ---------------------------------------
      sweep.dynamicRelationSplices( runtime, 48, 19  ); // 'user.channels'  'DynamicCleanupPlace'
      sweep.referenceScalarSplice(runtime, 48, 7) // user.channels InstanceFreePlace
    }
    @inline final def search_v2_user_channels(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      sweep.referenceScalarSplice(runtime, 48, 6) // user.channels InstanceAllocPlace
      sweep.referenceScalarSplice(runtime, 48, 1) // user.channels InstancePrePlace
      // -------- START CHILD RELATION VISITS (BEF|E POST) 'user.channels'  ----------------------
      // -------- visit static child relation(s) before dynamic ones  -----------------------------
      // -------- visit dynamic child relation(s) after static ones  ------------------------------
      sweep.dynamicRelationSplices( runtime, 48, 17 ); // 'user.channels' 'DynamicVisitPlace'
      // -------- END CHILD RELATION VISITS 'user.channels' --------------------------------------- 
      sweep.referenceScalarSplice(runtime, 48, 2) // user.channels InstancePostPlace
      // -------- START CHILD RELATION JOINS (AFTER POST) 'user.channels' -------------------------
      // -------- join static child relation(s) before dynamic ones  ------------------------------
      // -------- join dynamic child relation(s) after static ones  -------------------------------
      sweep.dynamicRelationSplices( runtime, 48, 18  ); // 'user.channels'  'DynamicJoinPlace'
      // -------- END CHILD RELATION JOINS 'user.channels' ----------------------------------------
      // -------- DYNAMIC RELATIONS CLEANUP 'user.channels' ---------------------------------------
      sweep.dynamicRelationSplices( runtime, 48, 19  ); // 'user.channels'  'DynamicCleanupPlace'
      sweep.referenceScalarSplice(runtime, 48, 7) // user.channels InstanceFreePlace
    }
    @inline final def search_v3_user_channels(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      sweep.referenceScalarSplice(runtime, 48, 6) // user.channels InstanceAllocPlace
      sweep.referenceScalarSplice(runtime, 48, 1) // user.channels InstancePrePlace
      // -------- START CHILD RELATION VISITS (BEF|E POST) 'user.channels'  ----------------------
      // -------- visit static child relation(s) before dynamic ones  -----------------------------
      // -------- visit dynamic child relation(s) after static ones  ------------------------------
      sweep.dynamicRelationSplices( runtime, 48, 17 ); // 'user.channels' 'DynamicVisitPlace'
      // -------- END CHILD RELATION VISITS 'user.channels' --------------------------------------- 
      sweep.referenceScalarSplice(runtime, 48, 2) // user.channels InstancePostPlace
      // -------- START CHILD RELATION JOINS (AFTER POST) 'user.channels' -------------------------
      // -------- join static child relation(s) before dynamic ones  ------------------------------
      // -------- join dynamic child relation(s) after static ones  -------------------------------
      sweep.dynamicRelationSplices( runtime, 48, 18  ); // 'user.channels'  'DynamicJoinPlace'
      // -------- END CHILD RELATION JOINS 'user.channels' ----------------------------------------
      // -------- DYNAMIC RELATIONS CLEANUP 'user.channels' ---------------------------------------
      sweep.dynamicRelationSplices( runtime, 48, 19  ); // 'user.channels'  'DynamicCleanupPlace'
      sweep.referenceScalarSplice(runtime, 48, 7) // user.channels InstanceFreePlace
    }
    @inline final def search_v1_user_sessions(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      sweep.referenceScalarSplice(runtime, 16, 6) // user.sessions InstanceAllocPlace
      sweep.referenceScalarSplice(runtime, 16, 1) // user.sessions InstancePrePlace
      // -------- START CHILD RELATION VISITS (BEF|E POST) 'user.sessions'  ----------------------
      // -------- visit static child relation(s) before dynamic ones  -----------------------------
      // -------- START value-map path='user.sessions.parameters:43' version=1 ordinal=20 ---------
      if (sweep.skipVisitPath(43) || runtime.lattice_user_sessions_relation.relationIsNull(reader, schema_structure_Session_V1_schematic, 20)) {
        runtime.lattice_user_sessions_parameters_relation_isNull = true;
      } else {
        iterate_valmap_v1_user_sessions_parameters(runtime, sweep, reader);
      } // END value-map path='user.sessions.parameters:43' version=1 ordinal=20
      sweep.referenceScalarSplice(runtime, 43, 8) // user.sessions.parameters ChildMergePlace
      // -------- START reference-vector path='user.sessions.events:32' version=1 ordinal=15 ------
      if (sweep.skipVisitPath(32) || runtime.lattice_user_sessions_relation.relationIsNull(reader, schema_structure_Session_V1_schematic, 15)) {
        runtime.lattice_user_sessions_events_relation_isNull = true;
      } else {
        iterate_refvec_v1_user_sessions_events(runtime, sweep, reader);
      } // END reference-vector path='user.sessions.events:32' version=1 ordinal=15
      sweep.referenceScalarSplice(runtime, 32, 8) // user.sessions.events ChildMergePlace
      // -------- visit dynamic child relation(s) after static ones  ------------------------------
      sweep.dynamicRelationSplices( runtime, 16, 17 ); // 'user.sessions' 'DynamicVisitPlace'
      // -------- END CHILD RELATION VISITS 'user.sessions' --------------------------------------- 
      sweep.referenceScalarSplice(runtime, 16, 2) // user.sessions InstancePostPlace
      // -------- START CHILD RELATION JOINS (AFTER POST) 'user.sessions' -------------------------
      // -------- join static child relation(s) before dynamic ones  ------------------------------
      sweep.referenceScalarSplice(runtime, 43, 9)  // user.sessions.parameters ChildJoinPlace
      sweep.referenceScalarSplice(runtime, 32, 9)  // user.sessions.events ChildJoinPlace
      // -------- join dynamic child relation(s) after static ones  -------------------------------
      sweep.dynamicRelationSplices( runtime, 16, 18  ); // 'user.sessions'  'DynamicJoinPlace'
      // -------- END CHILD RELATION JOINS 'user.sessions' ----------------------------------------
      // -------- DYNAMIC RELATIONS CLEANUP 'user.sessions' ---------------------------------------
      sweep.dynamicRelationSplices( runtime, 16, 19  ); // 'user.sessions'  'DynamicCleanupPlace'
      sweep.referenceScalarSplice(runtime, 16, 7) // user.sessions InstanceFreePlace
    }
    @inline final def search_v2_user_sessions(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      sweep.referenceScalarSplice(runtime, 16, 6) // user.sessions InstanceAllocPlace
      sweep.referenceScalarSplice(runtime, 16, 1) // user.sessions InstancePrePlace
      // -------- START CHILD RELATION VISITS (BEF|E POST) 'user.sessions'  ----------------------
      // -------- visit static child relation(s) before dynamic ones  -----------------------------
      // -------- START value-map path='user.sessions.parameters:43' version=2 ordinal=20 ---------
      if (sweep.skipVisitPath(43) || runtime.lattice_user_sessions_relation.relationIsNull(reader, schema_structure_Session_V2_schematic, 20)) {
        runtime.lattice_user_sessions_parameters_relation_isNull = true;
      } else {
        iterate_valmap_v2_user_sessions_parameters(runtime, sweep, reader);
      } // END value-map path='user.sessions.parameters:43' version=2 ordinal=20
      sweep.referenceScalarSplice(runtime, 43, 8) // user.sessions.parameters ChildMergePlace
      // -------- START reference-vector path='user.sessions.events:32' version=2 ordinal=15 ------
      if (sweep.skipVisitPath(32) || runtime.lattice_user_sessions_relation.relationIsNull(reader, schema_structure_Session_V2_schematic, 15)) {
        runtime.lattice_user_sessions_events_relation_isNull = true;
      } else {
        iterate_refvec_v2_user_sessions_events(runtime, sweep, reader);
      } // END reference-vector path='user.sessions.events:32' version=2 ordinal=15
      sweep.referenceScalarSplice(runtime, 32, 8) // user.sessions.events ChildMergePlace
      // -------- visit dynamic child relation(s) after static ones  ------------------------------
      sweep.dynamicRelationSplices( runtime, 16, 17 ); // 'user.sessions' 'DynamicVisitPlace'
      // -------- END CHILD RELATION VISITS 'user.sessions' --------------------------------------- 
      sweep.referenceScalarSplice(runtime, 16, 2) // user.sessions InstancePostPlace
      // -------- START CHILD RELATION JOINS (AFTER POST) 'user.sessions' -------------------------
      // -------- join static child relation(s) before dynamic ones  ------------------------------
      sweep.referenceScalarSplice(runtime, 43, 9)  // user.sessions.parameters ChildJoinPlace
      sweep.referenceScalarSplice(runtime, 32, 9)  // user.sessions.events ChildJoinPlace
      // -------- join dynamic child relation(s) after static ones  -------------------------------
      sweep.dynamicRelationSplices( runtime, 16, 18  ); // 'user.sessions'  'DynamicJoinPlace'
      // -------- END CHILD RELATION JOINS 'user.sessions' ----------------------------------------
      // -------- DYNAMIC RELATIONS CLEANUP 'user.sessions' ---------------------------------------
      sweep.dynamicRelationSplices( runtime, 16, 19  ); // 'user.sessions'  'DynamicCleanupPlace'
      sweep.referenceScalarSplice(runtime, 16, 7) // user.sessions InstanceFreePlace
    }
    @inline final def search_v3_user_sessions(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      sweep.referenceScalarSplice(runtime, 16, 6) // user.sessions InstanceAllocPlace
      sweep.referenceScalarSplice(runtime, 16, 1) // user.sessions InstancePrePlace
      // -------- START CHILD RELATION VISITS (BEF|E POST) 'user.sessions'  ----------------------
      // -------- visit static child relation(s) before dynamic ones  -----------------------------
      // -------- START value-map path='user.sessions.parameters:43' version=3 ordinal=20 ---------
      if (sweep.skipVisitPath(43) || runtime.lattice_user_sessions_relation.relationIsNull(reader, schema_structure_Session_V3_schematic, 20)) {
        runtime.lattice_user_sessions_parameters_relation_isNull = true;
      } else {
        iterate_valmap_v3_user_sessions_parameters(runtime, sweep, reader);
      } // END value-map path='user.sessions.parameters:43' version=3 ordinal=20
      sweep.referenceScalarSplice(runtime, 43, 8) // user.sessions.parameters ChildMergePlace
      // -------- START reference-vector path='user.sessions.events:32' version=3 ordinal=15 ------
      if (sweep.skipVisitPath(32) || runtime.lattice_user_sessions_relation.relationIsNull(reader, schema_structure_Session_V3_schematic, 15)) {
        runtime.lattice_user_sessions_events_relation_isNull = true;
      } else {
        iterate_refvec_v3_user_sessions_events(runtime, sweep, reader);
      } // END reference-vector path='user.sessions.events:32' version=3 ordinal=15
      sweep.referenceScalarSplice(runtime, 32, 8) // user.sessions.events ChildMergePlace
      // -------- visit dynamic child relation(s) after static ones  ------------------------------
      sweep.dynamicRelationSplices( runtime, 16, 17 ); // 'user.sessions' 'DynamicVisitPlace'
      // -------- END CHILD RELATION VISITS 'user.sessions' --------------------------------------- 
      sweep.referenceScalarSplice(runtime, 16, 2) // user.sessions InstancePostPlace
      // -------- START CHILD RELATION JOINS (AFTER POST) 'user.sessions' -------------------------
      // -------- join static child relation(s) before dynamic ones  ------------------------------
      sweep.referenceScalarSplice(runtime, 43, 9)  // user.sessions.parameters ChildJoinPlace
      sweep.referenceScalarSplice(runtime, 32, 9)  // user.sessions.events ChildJoinPlace
      // -------- join dynamic child relation(s) after static ones  -------------------------------
      sweep.dynamicRelationSplices( runtime, 16, 18  ); // 'user.sessions'  'DynamicJoinPlace'
      // -------- END CHILD RELATION JOINS 'user.sessions' ----------------------------------------
      // -------- DYNAMIC RELATIONS CLEANUP 'user.sessions' ---------------------------------------
      sweep.dynamicRelationSplices( runtime, 16, 19  ); // 'user.sessions'  'DynamicCleanupPlace'
      sweep.referenceScalarSplice(runtime, 16, 7) // user.sessions InstanceFreePlace
    }
    @inline final def search_v1_user_personas(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      sweep.referenceScalarSplice(runtime, 52, 6) // user.personas InstanceAllocPlace
      sweep.referenceScalarSplice(runtime, 52, 1) // user.personas InstancePrePlace
      // -------- START CHILD RELATION VISITS (BEF|E POST) 'user.personas'  ----------------------
      // -------- visit static child relation(s) before dynamic ones  -----------------------------
      // -------- visit dynamic child relation(s) after static ones  ------------------------------
      sweep.dynamicRelationSplices( runtime, 52, 17 ); // 'user.personas' 'DynamicVisitPlace'
      // -------- END CHILD RELATION VISITS 'user.personas' --------------------------------------- 
      sweep.referenceScalarSplice(runtime, 52, 2) // user.personas InstancePostPlace
      // -------- START CHILD RELATION JOINS (AFTER POST) 'user.personas' -------------------------
      // -------- join static child relation(s) before dynamic ones  ------------------------------
      // -------- join dynamic child relation(s) after static ones  -------------------------------
      sweep.dynamicRelationSplices( runtime, 52, 18  ); // 'user.personas'  'DynamicJoinPlace'
      // -------- END CHILD RELATION JOINS 'user.personas' ----------------------------------------
      // -------- DYNAMIC RELATIONS CLEANUP 'user.personas' ---------------------------------------
      sweep.dynamicRelationSplices( runtime, 52, 19  ); // 'user.personas'  'DynamicCleanupPlace'
      sweep.referenceScalarSplice(runtime, 52, 7) // user.personas InstanceFreePlace
    }
    @inline final def search_v2_user_personas(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      sweep.referenceScalarSplice(runtime, 52, 6) // user.personas InstanceAllocPlace
      sweep.referenceScalarSplice(runtime, 52, 1) // user.personas InstancePrePlace
      // -------- START CHILD RELATION VISITS (BEF|E POST) 'user.personas'  ----------------------
      // -------- visit static child relation(s) before dynamic ones  -----------------------------
      // -------- visit dynamic child relation(s) after static ones  ------------------------------
      sweep.dynamicRelationSplices( runtime, 52, 17 ); // 'user.personas' 'DynamicVisitPlace'
      // -------- END CHILD RELATION VISITS 'user.personas' --------------------------------------- 
      sweep.referenceScalarSplice(runtime, 52, 2) // user.personas InstancePostPlace
      // -------- START CHILD RELATION JOINS (AFTER POST) 'user.personas' -------------------------
      // -------- join static child relation(s) before dynamic ones  ------------------------------
      // -------- join dynamic child relation(s) after static ones  -------------------------------
      sweep.dynamicRelationSplices( runtime, 52, 18  ); // 'user.personas'  'DynamicJoinPlace'
      // -------- END CHILD RELATION JOINS 'user.personas' ----------------------------------------
      // -------- DYNAMIC RELATIONS CLEANUP 'user.personas' ---------------------------------------
      sweep.dynamicRelationSplices( runtime, 52, 19  ); // 'user.personas'  'DynamicCleanupPlace'
      sweep.referenceScalarSplice(runtime, 52, 7) // user.personas InstanceFreePlace
    }
    @inline final def search_v3_user_personas(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      sweep.referenceScalarSplice(runtime, 52, 6) // user.personas InstanceAllocPlace
      sweep.referenceScalarSplice(runtime, 52, 1) // user.personas InstancePrePlace
      // -------- START CHILD RELATION VISITS (BEF|E POST) 'user.personas'  ----------------------
      // -------- visit static child relation(s) before dynamic ones  -----------------------------
      // -------- visit dynamic child relation(s) after static ones  ------------------------------
      sweep.dynamicRelationSplices( runtime, 52, 17 ); // 'user.personas' 'DynamicVisitPlace'
      // -------- END CHILD RELATION VISITS 'user.personas' --------------------------------------- 
      sweep.referenceScalarSplice(runtime, 52, 2) // user.personas InstancePostPlace
      // -------- START CHILD RELATION JOINS (AFTER POST) 'user.personas' -------------------------
      // -------- join static child relation(s) before dynamic ones  ------------------------------
      // -------- join dynamic child relation(s) after static ones  -------------------------------
      sweep.dynamicRelationSplices( runtime, 52, 18  ); // 'user.personas'  'DynamicJoinPlace'
      // -------- END CHILD RELATION JOINS 'user.personas' ----------------------------------------
      // -------- DYNAMIC RELATIONS CLEANUP 'user.personas' ---------------------------------------
      sweep.dynamicRelationSplices( runtime, 52, 19  ); // 'user.personas'  'DynamicCleanupPlace'
      sweep.referenceScalarSplice(runtime, 52, 7) // user.personas InstanceFreePlace
    }
    @inline final def search_v1_user_segments(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      sweep.referenceScalarSplice(runtime, 46, 6) // user.segments InstanceAllocPlace
      sweep.referenceScalarSplice(runtime, 46, 1) // user.segments InstancePrePlace
      // -------- START CHILD RELATION VISITS (BEF|E POST) 'user.segments'  ----------------------
      // -------- visit static child relation(s) before dynamic ones  -----------------------------
      // -------- visit dynamic child relation(s) after static ones  ------------------------------
      sweep.dynamicRelationSplices( runtime, 46, 17 ); // 'user.segments' 'DynamicVisitPlace'
      // -------- END CHILD RELATION VISITS 'user.segments' --------------------------------------- 
      sweep.referenceScalarSplice(runtime, 46, 2) // user.segments InstancePostPlace
      // -------- START CHILD RELATION JOINS (AFTER POST) 'user.segments' -------------------------
      // -------- join static child relation(s) before dynamic ones  ------------------------------
      // -------- join dynamic child relation(s) after static ones  -------------------------------
      sweep.dynamicRelationSplices( runtime, 46, 18  ); // 'user.segments'  'DynamicJoinPlace'
      // -------- END CHILD RELATION JOINS 'user.segments' ----------------------------------------
      // -------- DYNAMIC RELATIONS CLEANUP 'user.segments' ---------------------------------------
      sweep.dynamicRelationSplices( runtime, 46, 19  ); // 'user.segments'  'DynamicCleanupPlace'
      sweep.referenceScalarSplice(runtime, 46, 7) // user.segments InstanceFreePlace
    }
    @inline final def search_v2_user_segments(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      sweep.referenceScalarSplice(runtime, 46, 6) // user.segments InstanceAllocPlace
      sweep.referenceScalarSplice(runtime, 46, 1) // user.segments InstancePrePlace
      // -------- START CHILD RELATION VISITS (BEF|E POST) 'user.segments'  ----------------------
      // -------- visit static child relation(s) before dynamic ones  -----------------------------
      // -------- visit dynamic child relation(s) after static ones  ------------------------------
      sweep.dynamicRelationSplices( runtime, 46, 17 ); // 'user.segments' 'DynamicVisitPlace'
      // -------- END CHILD RELATION VISITS 'user.segments' --------------------------------------- 
      sweep.referenceScalarSplice(runtime, 46, 2) // user.segments InstancePostPlace
      // -------- START CHILD RELATION JOINS (AFTER POST) 'user.segments' -------------------------
      // -------- join static child relation(s) before dynamic ones  ------------------------------
      // -------- join dynamic child relation(s) after static ones  -------------------------------
      sweep.dynamicRelationSplices( runtime, 46, 18  ); // 'user.segments'  'DynamicJoinPlace'
      // -------- END CHILD RELATION JOINS 'user.segments' ----------------------------------------
      // -------- DYNAMIC RELATIONS CLEANUP 'user.segments' ---------------------------------------
      sweep.dynamicRelationSplices( runtime, 46, 19  ); // 'user.segments'  'DynamicCleanupPlace'
      sweep.referenceScalarSplice(runtime, 46, 7) // user.segments InstanceFreePlace
    }
    @inline final def search_v3_user_segments(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      sweep.referenceScalarSplice(runtime, 46, 6) // user.segments InstanceAllocPlace
      sweep.referenceScalarSplice(runtime, 46, 1) // user.segments InstancePrePlace
      // -------- START CHILD RELATION VISITS (BEF|E POST) 'user.segments'  ----------------------
      // -------- visit static child relation(s) before dynamic ones  -----------------------------
      // -------- visit dynamic child relation(s) after static ones  ------------------------------
      sweep.dynamicRelationSplices( runtime, 46, 17 ); // 'user.segments' 'DynamicVisitPlace'
      // -------- END CHILD RELATION VISITS 'user.segments' --------------------------------------- 
      sweep.referenceScalarSplice(runtime, 46, 2) // user.segments InstancePostPlace
      // -------- START CHILD RELATION JOINS (AFTER POST) 'user.segments' -------------------------
      // -------- join static child relation(s) before dynamic ones  ------------------------------
      // -------- join dynamic child relation(s) after static ones  -------------------------------
      sweep.dynamicRelationSplices( runtime, 46, 18  ); // 'user.segments'  'DynamicJoinPlace'
      // -------- END CHILD RELATION JOINS 'user.segments' ----------------------------------------
      // -------- DYNAMIC RELATIONS CLEANUP 'user.segments' ---------------------------------------
      sweep.dynamicRelationSplices( runtime, 46, 19  ); // 'user.segments'  'DynamicCleanupPlace'
      sweep.referenceScalarSplice(runtime, 46, 7) // user.segments InstanceFreePlace
    }
    @inline final def iterate_valmap_v1_user_sessions_parameters(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      runtime.lattice_user_sessions_parameters_relation_isNull = false;
      runtime.lattice_user_sessions_parameters_vector_is_first = false;
      runtime.lattice_user_sessions_parameters_vector_is_last = false;
      val memberIterator = runtime.lattice_user_sessions_relation.valueMapIterator(reader, schema_structure_Session_V1_schematic, 20);
      val memberCount = memberIterator.length(reader);
      sweep.valueMapSplice(runtime, 43, 10); // user.sessions.parameters VectorAllocPlace
      sweep.valueMapSplice(runtime, 43, 3); // user.sessions.parameters VectorBeforePlace
      var i = 0;
      while (i < memberCount && !runtime.skipControlMemberPath(43) ) {
        runtime.lattice_user_sessions_parameters_vector_is_first = (i == 0);
        runtime.lattice_user_sessions_parameters_vector_is_last = i == (memberCount - 1);
        sweep.valueMapMemberSplice(runtime, 43, 12); // user.sessions.parameters VectorMemberAllocPlace
        // -------- current member key/value tuple for value map user.sessions.parameters -----------
        runtime.lattice_user_sessions_parameters_value_map_key = memberIterator.readLexiconStringKey(i, reader);
        runtime.lattice_user_sessions_parameters_value_map_value = memberIterator.readLexiconStringStringValue(i, reader);
        sweep.valueMapMemberSplice(runtime, 43, 5); // user.sessions.parameters VectorMemberSituPlace
        sweep.valueMapMemberSplice(runtime, 43, 14); // user.sessions.parameters VectorMemberMergePlace
        sweep.valueMapMemberSplice(runtime, 43, 13); // user.sessions.parameters VectorMemberFreePlace
        i += 1;
      }
      runtime.lattice_user_sessions_parameters_vector_is_first = false; // this should be false for 'after' splices
      runtime.lattice_user_sessions_parameters_vector_is_last = false; // this should be false for 'after' splices
      sweep.valueMapSplice(runtime, 43, 4); // user.sessions.parameters VectorAfterPlace
      sweep.valueMapSplice(runtime, 43, 11); // user.sessions.parameters VectorFreePlace 
    }
    @inline final def iterate_refvec_v1_user_sessions_events(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      runtime.lattice_user_sessions_events_relation_isNull = false;
      runtime.lattice_user_sessions_events_vector_is_first = false;
      runtime.lattice_user_sessions_events_vector_is_last = false;
      val memberIterator = runtime.lattice_user_sessions_relation.referenceVectorIterator(reader, schema_structure_Session_V1_schematic, 15);
      var vectorIndex = memberIterator.start(reader);
      val memberCount = memberIterator.length(reader);
      sweep.referenceVectorSplice(runtime, 32, 10); // user.sessions.events VectorAllocPlace
      sweep.referenceVectorSplice(runtime, 32, 3); // user.sessions.events VectorBeforePlace
      var i = 0;
      while (i < memberCount && !runtime.skipControlMemberPath(32) ) {
        runtime.lattice_user_sessions_events_vector_is_first = (i == 0);
        runtime.lattice_user_sessions_events_vector_is_last = i == (memberCount - 1);
        runtime.lattice_user_sessions_events_relation = memberIterator.member(reader, vectorIndex);
        sweep.referenceVectorMemberSplice(runtime, 32, 12); // user.sessions.events VectorMemberAllocPlace
        { // BEGIN reference scalar relations for 'user.sessions.events'
          runtime.lattice_user_sessions_events_relation.versionKey(reader) match {
            case 1 ⇒ { // schema version 1
              search_v1_user_sessions_events(runtime, sweep, reader);
            }
            case 2 ⇒ { // schema version 2
              search_v2_user_sessions_events(runtime, sweep, reader);
            }
            case 3 ⇒ { // schema version 3
              search_v3_user_sessions_events(runtime, sweep, reader);
            }
          }
        } // END reference scalar relations for 'user.sessions.events' 
        sweep.referenceVectorMemberSplice(runtime, 32, 14); // user.sessions.events VectorMemberMergePlace
        sweep.referenceVectorMemberSplice(runtime, 32, 13); // user.sessions.events VectorMemberFreePlace
        vectorIndex = memberIterator.advance(reader, vectorIndex);
        i += 1;
      }
      runtime.lattice_user_sessions_events_vector_is_first = false; // this should be false for 'after' splices
      runtime.lattice_user_sessions_events_vector_is_last = false; // this should be false for 'after' splices
      sweep.referenceVectorSplice(runtime, 32, 4); // user.sessions.events VectorAfterPlace
      sweep.referenceVectorSplice(runtime, 32, 11); // user.sessions.events VectorFreePlace
    }
    @inline final def iterate_valmap_v2_user_sessions_parameters(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      runtime.lattice_user_sessions_parameters_relation_isNull = false;
      runtime.lattice_user_sessions_parameters_vector_is_first = false;
      runtime.lattice_user_sessions_parameters_vector_is_last = false;
      val memberIterator = runtime.lattice_user_sessions_relation.valueMapIterator(reader, schema_structure_Session_V2_schematic, 20);
      val memberCount = memberIterator.length(reader);
      sweep.valueMapSplice(runtime, 43, 10); // user.sessions.parameters VectorAllocPlace
      sweep.valueMapSplice(runtime, 43, 3); // user.sessions.parameters VectorBeforePlace
      var i = 0;
      while (i < memberCount && !runtime.skipControlMemberPath(43) ) {
        runtime.lattice_user_sessions_parameters_vector_is_first = (i == 0);
        runtime.lattice_user_sessions_parameters_vector_is_last = i == (memberCount - 1);
        sweep.valueMapMemberSplice(runtime, 43, 12); // user.sessions.parameters VectorMemberAllocPlace
        // -------- current member key/value tuple for value map user.sessions.parameters -----------
        runtime.lattice_user_sessions_parameters_value_map_key = memberIterator.readLexiconStringKey(i, reader);
        runtime.lattice_user_sessions_parameters_value_map_value = memberIterator.readLexiconStringStringValue(i, reader);
        sweep.valueMapMemberSplice(runtime, 43, 5); // user.sessions.parameters VectorMemberSituPlace
        sweep.valueMapMemberSplice(runtime, 43, 14); // user.sessions.parameters VectorMemberMergePlace
        sweep.valueMapMemberSplice(runtime, 43, 13); // user.sessions.parameters VectorMemberFreePlace
        i += 1;
      }
      runtime.lattice_user_sessions_parameters_vector_is_first = false; // this should be false for 'after' splices
      runtime.lattice_user_sessions_parameters_vector_is_last = false; // this should be false for 'after' splices
      sweep.valueMapSplice(runtime, 43, 4); // user.sessions.parameters VectorAfterPlace
      sweep.valueMapSplice(runtime, 43, 11); // user.sessions.parameters VectorFreePlace 
    }
    @inline final def iterate_refvec_v2_user_sessions_events(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      runtime.lattice_user_sessions_events_relation_isNull = false;
      runtime.lattice_user_sessions_events_vector_is_first = false;
      runtime.lattice_user_sessions_events_vector_is_last = false;
      val memberIterator = runtime.lattice_user_sessions_relation.referenceVectorIterator(reader, schema_structure_Session_V2_schematic, 15);
      var vectorIndex = memberIterator.start(reader);
      val memberCount = memberIterator.length(reader);
      sweep.referenceVectorSplice(runtime, 32, 10); // user.sessions.events VectorAllocPlace
      sweep.referenceVectorSplice(runtime, 32, 3); // user.sessions.events VectorBeforePlace
      var i = 0;
      while (i < memberCount && !runtime.skipControlMemberPath(32) ) {
        runtime.lattice_user_sessions_events_vector_is_first = (i == 0);
        runtime.lattice_user_sessions_events_vector_is_last = i == (memberCount - 1);
        runtime.lattice_user_sessions_events_relation = memberIterator.member(reader, vectorIndex);
        sweep.referenceVectorMemberSplice(runtime, 32, 12); // user.sessions.events VectorMemberAllocPlace
        { // BEGIN reference scalar relations for 'user.sessions.events'
          runtime.lattice_user_sessions_events_relation.versionKey(reader) match {
            case 1 ⇒ { // schema version 1
              search_v1_user_sessions_events(runtime, sweep, reader);
            }
            case 2 ⇒ { // schema version 2
              search_v2_user_sessions_events(runtime, sweep, reader);
            }
            case 3 ⇒ { // schema version 3
              search_v3_user_sessions_events(runtime, sweep, reader);
            }
          }
        } // END reference scalar relations for 'user.sessions.events' 
        sweep.referenceVectorMemberSplice(runtime, 32, 14); // user.sessions.events VectorMemberMergePlace
        sweep.referenceVectorMemberSplice(runtime, 32, 13); // user.sessions.events VectorMemberFreePlace
        vectorIndex = memberIterator.advance(reader, vectorIndex);
        i += 1;
      }
      runtime.lattice_user_sessions_events_vector_is_first = false; // this should be false for 'after' splices
      runtime.lattice_user_sessions_events_vector_is_last = false; // this should be false for 'after' splices
      sweep.referenceVectorSplice(runtime, 32, 4); // user.sessions.events VectorAfterPlace
      sweep.referenceVectorSplice(runtime, 32, 11); // user.sessions.events VectorFreePlace
    }
    @inline final def iterate_valmap_v3_user_sessions_parameters(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      runtime.lattice_user_sessions_parameters_relation_isNull = false;
      runtime.lattice_user_sessions_parameters_vector_is_first = false;
      runtime.lattice_user_sessions_parameters_vector_is_last = false;
      val memberIterator = runtime.lattice_user_sessions_relation.valueMapIterator(reader, schema_structure_Session_V3_schematic, 20);
      val memberCount = memberIterator.length(reader);
      sweep.valueMapSplice(runtime, 43, 10); // user.sessions.parameters VectorAllocPlace
      sweep.valueMapSplice(runtime, 43, 3); // user.sessions.parameters VectorBeforePlace
      var i = 0;
      while (i < memberCount && !runtime.skipControlMemberPath(43) ) {
        runtime.lattice_user_sessions_parameters_vector_is_first = (i == 0);
        runtime.lattice_user_sessions_parameters_vector_is_last = i == (memberCount - 1);
        sweep.valueMapMemberSplice(runtime, 43, 12); // user.sessions.parameters VectorMemberAllocPlace
        // -------- current member key/value tuple for value map user.sessions.parameters -----------
        runtime.lattice_user_sessions_parameters_value_map_key = memberIterator.readLexiconStringKey(i, reader);
        runtime.lattice_user_sessions_parameters_value_map_value = memberIterator.readLexiconStringStringValue(i, reader);
        sweep.valueMapMemberSplice(runtime, 43, 5); // user.sessions.parameters VectorMemberSituPlace
        sweep.valueMapMemberSplice(runtime, 43, 14); // user.sessions.parameters VectorMemberMergePlace
        sweep.valueMapMemberSplice(runtime, 43, 13); // user.sessions.parameters VectorMemberFreePlace
        i += 1;
      }
      runtime.lattice_user_sessions_parameters_vector_is_first = false; // this should be false for 'after' splices
      runtime.lattice_user_sessions_parameters_vector_is_last = false; // this should be false for 'after' splices
      sweep.valueMapSplice(runtime, 43, 4); // user.sessions.parameters VectorAfterPlace
      sweep.valueMapSplice(runtime, 43, 11); // user.sessions.parameters VectorFreePlace 
    }
    @inline final def iterate_refvec_v3_user_sessions_events(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      runtime.lattice_user_sessions_events_relation_isNull = false;
      runtime.lattice_user_sessions_events_vector_is_first = false;
      runtime.lattice_user_sessions_events_vector_is_last = false;
      val memberIterator = runtime.lattice_user_sessions_relation.referenceVectorIterator(reader, schema_structure_Session_V3_schematic, 15);
      var vectorIndex = memberIterator.start(reader);
      val memberCount = memberIterator.length(reader);
      sweep.referenceVectorSplice(runtime, 32, 10); // user.sessions.events VectorAllocPlace
      sweep.referenceVectorSplice(runtime, 32, 3); // user.sessions.events VectorBeforePlace
      var i = 0;
      while (i < memberCount && !runtime.skipControlMemberPath(32) ) {
        runtime.lattice_user_sessions_events_vector_is_first = (i == 0);
        runtime.lattice_user_sessions_events_vector_is_last = i == (memberCount - 1);
        runtime.lattice_user_sessions_events_relation = memberIterator.member(reader, vectorIndex);
        sweep.referenceVectorMemberSplice(runtime, 32, 12); // user.sessions.events VectorMemberAllocPlace
        { // BEGIN reference scalar relations for 'user.sessions.events'
          runtime.lattice_user_sessions_events_relation.versionKey(reader) match {
            case 1 ⇒ { // schema version 1
              search_v1_user_sessions_events(runtime, sweep, reader);
            }
            case 2 ⇒ { // schema version 2
              search_v2_user_sessions_events(runtime, sweep, reader);
            }
            case 3 ⇒ { // schema version 3
              search_v3_user_sessions_events(runtime, sweep, reader);
            }
          }
        } // END reference scalar relations for 'user.sessions.events' 
        sweep.referenceVectorMemberSplice(runtime, 32, 14); // user.sessions.events VectorMemberMergePlace
        sweep.referenceVectorMemberSplice(runtime, 32, 13); // user.sessions.events VectorMemberFreePlace
        vectorIndex = memberIterator.advance(reader, vectorIndex);
        i += 1;
      }
      runtime.lattice_user_sessions_events_vector_is_first = false; // this should be false for 'after' splices
      runtime.lattice_user_sessions_events_vector_is_last = false; // this should be false for 'after' splices
      sweep.referenceVectorSplice(runtime, 32, 4); // user.sessions.events VectorAfterPlace
      sweep.referenceVectorSplice(runtime, 32, 11); // user.sessions.events VectorFreePlace
    }
    @inline final def search_v1_user_sessions_events(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      sweep.referenceScalarSplice(runtime, 32, 6) // user.sessions.events InstanceAllocPlace
      sweep.referenceScalarSplice(runtime, 32, 1) // user.sessions.events InstancePrePlace
      // -------- START CHILD RELATION VISITS (BEF|E POST) 'user.sessions.events'  ---------------
      // -------- visit static child relation(s) before dynamic ones  -----------------------------
      // -------- START value-map path='user.sessions.events.parameters:38' version=1 ordinal=5 ---
      if (sweep.skipVisitPath(38) || runtime.lattice_user_sessions_events_relation.relationIsNull(reader, schema_structure_Event_V1_schematic, 5)) {
        runtime.lattice_user_sessions_events_parameters_relation_isNull = true;
      } else {
        iterate_valmap_v1_user_sessions_events_parameters(runtime, sweep, reader);
      } // END value-map path='user.sessions.events.parameters:38' version=1 ordinal=5
      sweep.referenceScalarSplice(runtime, 38, 8) // user.sessions.events.parameters ChildMergePlace
      // -------- visit dynamic child relation(s) after static ones  ------------------------------
      sweep.dynamicRelationSplices( runtime, 32, 17 ); // 'user.sessions.events' 'DynamicVisitPlace'
      // -------- END CHILD RELATION VISITS 'user.sessions.events' -------------------------------- 
      sweep.referenceScalarSplice(runtime, 32, 2) // user.sessions.events InstancePostPlace
      // -------- START CHILD RELATION JOINS (AFTER POST) 'user.sessions.events' ------------------
      // -------- join static child relation(s) before dynamic ones  ------------------------------
      sweep.referenceScalarSplice(runtime, 38, 9)  // user.sessions.events.parameters ChildJoinPlace
      // -------- join dynamic child relation(s) after static ones  -------------------------------
      sweep.dynamicRelationSplices( runtime, 32, 18  ); // 'user.sessions.events'  'DynamicJoinPlace'
      // -------- END CHILD RELATION JOINS 'user.sessions.events' ---------------------------------
      // -------- DYNAMIC RELATIONS CLEANUP 'user.sessions.events' --------------------------------
      sweep.dynamicRelationSplices( runtime, 32, 19  ); // 'user.sessions.events'  'DynamicCleanupPlace'
      sweep.referenceScalarSplice(runtime, 32, 7) // user.sessions.events InstanceFreePlace
    }
    @inline final def search_v2_user_sessions_events(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      sweep.referenceScalarSplice(runtime, 32, 6) // user.sessions.events InstanceAllocPlace
      sweep.referenceScalarSplice(runtime, 32, 1) // user.sessions.events InstancePrePlace
      // -------- START CHILD RELATION VISITS (BEF|E POST) 'user.sessions.events'  ---------------
      // -------- visit static child relation(s) before dynamic ones  -----------------------------
      // -------- START value-map path='user.sessions.events.parameters:38' version=2 ordinal=5 ---
      if (sweep.skipVisitPath(38) || runtime.lattice_user_sessions_events_relation.relationIsNull(reader, schema_structure_Event_V2_schematic, 5)) {
        runtime.lattice_user_sessions_events_parameters_relation_isNull = true;
      } else {
        iterate_valmap_v2_user_sessions_events_parameters(runtime, sweep, reader);
      } // END value-map path='user.sessions.events.parameters:38' version=2 ordinal=5
      sweep.referenceScalarSplice(runtime, 38, 8) // user.sessions.events.parameters ChildMergePlace
      // -------- visit dynamic child relation(s) after static ones  ------------------------------
      sweep.dynamicRelationSplices( runtime, 32, 17 ); // 'user.sessions.events' 'DynamicVisitPlace'
      // -------- END CHILD RELATION VISITS 'user.sessions.events' -------------------------------- 
      sweep.referenceScalarSplice(runtime, 32, 2) // user.sessions.events InstancePostPlace
      // -------- START CHILD RELATION JOINS (AFTER POST) 'user.sessions.events' ------------------
      // -------- join static child relation(s) before dynamic ones  ------------------------------
      sweep.referenceScalarSplice(runtime, 38, 9)  // user.sessions.events.parameters ChildJoinPlace
      // -------- join dynamic child relation(s) after static ones  -------------------------------
      sweep.dynamicRelationSplices( runtime, 32, 18  ); // 'user.sessions.events'  'DynamicJoinPlace'
      // -------- END CHILD RELATION JOINS 'user.sessions.events' ---------------------------------
      // -------- DYNAMIC RELATIONS CLEANUP 'user.sessions.events' --------------------------------
      sweep.dynamicRelationSplices( runtime, 32, 19  ); // 'user.sessions.events'  'DynamicCleanupPlace'
      sweep.referenceScalarSplice(runtime, 32, 7) // user.sessions.events InstanceFreePlace
    }
    @inline final def search_v3_user_sessions_events(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      sweep.referenceScalarSplice(runtime, 32, 6) // user.sessions.events InstanceAllocPlace
      sweep.referenceScalarSplice(runtime, 32, 1) // user.sessions.events InstancePrePlace
      // -------- START CHILD RELATION VISITS (BEF|E POST) 'user.sessions.events'  ---------------
      // -------- visit static child relation(s) before dynamic ones  -----------------------------
      // -------- START value-map path='user.sessions.events.parameters:38' version=3 ordinal=5 ---
      if (sweep.skipVisitPath(38) || runtime.lattice_user_sessions_events_relation.relationIsNull(reader, schema_structure_Event_V3_schematic, 5)) {
        runtime.lattice_user_sessions_events_parameters_relation_isNull = true;
      } else {
        iterate_valmap_v3_user_sessions_events_parameters(runtime, sweep, reader);
      } // END value-map path='user.sessions.events.parameters:38' version=3 ordinal=5
      sweep.referenceScalarSplice(runtime, 38, 8) // user.sessions.events.parameters ChildMergePlace
      // -------- visit dynamic child relation(s) after static ones  ------------------------------
      sweep.dynamicRelationSplices( runtime, 32, 17 ); // 'user.sessions.events' 'DynamicVisitPlace'
      // -------- END CHILD RELATION VISITS 'user.sessions.events' -------------------------------- 
      sweep.referenceScalarSplice(runtime, 32, 2) // user.sessions.events InstancePostPlace
      // -------- START CHILD RELATION JOINS (AFTER POST) 'user.sessions.events' ------------------
      // -------- join static child relation(s) before dynamic ones  ------------------------------
      sweep.referenceScalarSplice(runtime, 38, 9)  // user.sessions.events.parameters ChildJoinPlace
      // -------- join dynamic child relation(s) after static ones  -------------------------------
      sweep.dynamicRelationSplices( runtime, 32, 18  ); // 'user.sessions.events'  'DynamicJoinPlace'
      // -------- END CHILD RELATION JOINS 'user.sessions.events' ---------------------------------
      // -------- DYNAMIC RELATIONS CLEANUP 'user.sessions.events' --------------------------------
      sweep.dynamicRelationSplices( runtime, 32, 19  ); // 'user.sessions.events'  'DynamicCleanupPlace'
      sweep.referenceScalarSplice(runtime, 32, 7) // user.sessions.events InstanceFreePlace
    }
    @inline final def iterate_valmap_v1_user_sessions_events_parameters(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      runtime.lattice_user_sessions_events_parameters_relation_isNull = false;
      runtime.lattice_user_sessions_events_parameters_vector_is_first = false;
      runtime.lattice_user_sessions_events_parameters_vector_is_last = false;
      val memberIterator = runtime.lattice_user_sessions_events_relation.valueMapIterator(reader, schema_structure_Event_V1_schematic, 5);
      val memberCount = memberIterator.length(reader);
      sweep.valueMapSplice(runtime, 38, 10); // user.sessions.events.parameters VectorAllocPlace
      sweep.valueMapSplice(runtime, 38, 3); // user.sessions.events.parameters VectorBeforePlace
      var i = 0;
      while (i < memberCount && !runtime.skipControlMemberPath(38) ) {
        runtime.lattice_user_sessions_events_parameters_vector_is_first = (i == 0);
        runtime.lattice_user_sessions_events_parameters_vector_is_last = i == (memberCount - 1);
        sweep.valueMapMemberSplice(runtime, 38, 12); // user.sessions.events.parameters VectorMemberAllocPlace
        // -------- current member key/value tuple for value map user.sessions.events.parameters ----
        runtime.lattice_user_sessions_events_parameters_value_map_key = memberIterator.readLexiconStringKey(i, reader);
        runtime.lattice_user_sessions_events_parameters_value_map_value = memberIterator.readLexiconStringStringValue(i, reader);
        sweep.valueMapMemberSplice(runtime, 38, 5); // user.sessions.events.parameters VectorMemberSituPlace
        sweep.valueMapMemberSplice(runtime, 38, 14); // user.sessions.events.parameters VectorMemberMergePlace
        sweep.valueMapMemberSplice(runtime, 38, 13); // user.sessions.events.parameters VectorMemberFreePlace
        i += 1;
      }
      runtime.lattice_user_sessions_events_parameters_vector_is_first = false; // this should be false for 'after' splices
      runtime.lattice_user_sessions_events_parameters_vector_is_last = false; // this should be false for 'after' splices
      sweep.valueMapSplice(runtime, 38, 4); // user.sessions.events.parameters VectorAfterPlace
      sweep.valueMapSplice(runtime, 38, 11); // user.sessions.events.parameters VectorFreePlace 
    }
    @inline final def iterate_valmap_v2_user_sessions_events_parameters(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      runtime.lattice_user_sessions_events_parameters_relation_isNull = false;
      runtime.lattice_user_sessions_events_parameters_vector_is_first = false;
      runtime.lattice_user_sessions_events_parameters_vector_is_last = false;
      val memberIterator = runtime.lattice_user_sessions_events_relation.valueMapIterator(reader, schema_structure_Event_V2_schematic, 5);
      val memberCount = memberIterator.length(reader);
      sweep.valueMapSplice(runtime, 38, 10); // user.sessions.events.parameters VectorAllocPlace
      sweep.valueMapSplice(runtime, 38, 3); // user.sessions.events.parameters VectorBeforePlace
      var i = 0;
      while (i < memberCount && !runtime.skipControlMemberPath(38) ) {
        runtime.lattice_user_sessions_events_parameters_vector_is_first = (i == 0);
        runtime.lattice_user_sessions_events_parameters_vector_is_last = i == (memberCount - 1);
        sweep.valueMapMemberSplice(runtime, 38, 12); // user.sessions.events.parameters VectorMemberAllocPlace
        // -------- current member key/value tuple for value map user.sessions.events.parameters ----
        runtime.lattice_user_sessions_events_parameters_value_map_key = memberIterator.readLexiconStringKey(i, reader);
        runtime.lattice_user_sessions_events_parameters_value_map_value = memberIterator.readLexiconStringStringValue(i, reader);
        sweep.valueMapMemberSplice(runtime, 38, 5); // user.sessions.events.parameters VectorMemberSituPlace
        sweep.valueMapMemberSplice(runtime, 38, 14); // user.sessions.events.parameters VectorMemberMergePlace
        sweep.valueMapMemberSplice(runtime, 38, 13); // user.sessions.events.parameters VectorMemberFreePlace
        i += 1;
      }
      runtime.lattice_user_sessions_events_parameters_vector_is_first = false; // this should be false for 'after' splices
      runtime.lattice_user_sessions_events_parameters_vector_is_last = false; // this should be false for 'after' splices
      sweep.valueMapSplice(runtime, 38, 4); // user.sessions.events.parameters VectorAfterPlace
      sweep.valueMapSplice(runtime, 38, 11); // user.sessions.events.parameters VectorFreePlace 
    }
    @inline final def iterate_valmap_v3_user_sessions_events_parameters(runtime:QuoTraveler_lexicon_runtime, sweep: org.burstsys.felt.model.sweep.FeltSweep, reader:org.burstsys.tesla.buffer.TeslaBufferReader): Unit = {
      runtime.lattice_user_sessions_events_parameters_relation_isNull = false;
      runtime.lattice_user_sessions_events_parameters_vector_is_first = false;
      runtime.lattice_user_sessions_events_parameters_vector_is_last = false;
      val memberIterator = runtime.lattice_user_sessions_events_relation.valueMapIterator(reader, schema_structure_Event_V3_schematic, 5);
      val memberCount = memberIterator.length(reader);
      sweep.valueMapSplice(runtime, 38, 10); // user.sessions.events.parameters VectorAllocPlace
      sweep.valueMapSplice(runtime, 38, 3); // user.sessions.events.parameters VectorBeforePlace
      var i = 0;
      while (i < memberCount && !runtime.skipControlMemberPath(38) ) {
        runtime.lattice_user_sessions_events_parameters_vector_is_first = (i == 0);
        runtime.lattice_user_sessions_events_parameters_vector_is_last = i == (memberCount - 1);
        sweep.valueMapMemberSplice(runtime, 38, 12); // user.sessions.events.parameters VectorMemberAllocPlace
        // -------- current member key/value tuple for value map user.sessions.events.parameters ----
        runtime.lattice_user_sessions_events_parameters_value_map_key = memberIterator.readLexiconStringKey(i, reader);
        runtime.lattice_user_sessions_events_parameters_value_map_value = memberIterator.readLexiconStringStringValue(i, reader);
        sweep.valueMapMemberSplice(runtime, 38, 5); // user.sessions.events.parameters VectorMemberSituPlace
        sweep.valueMapMemberSplice(runtime, 38, 14); // user.sessions.events.parameters VectorMemberMergePlace
        sweep.valueMapMemberSplice(runtime, 38, 13); // user.sessions.events.parameters VectorMemberFreePlace
        i += 1;
      }
      runtime.lattice_user_sessions_events_parameters_vector_is_first = false; // this should be false for 'after' splices
      runtime.lattice_user_sessions_events_parameters_vector_is_last = false; // this should be false for 'after' splices
      sweep.valueMapSplice(runtime, 38, 4); // user.sessions.events.parameters VectorAfterPlace
      sweep.valueMapSplice(runtime, 38, 11); // user.sessions.events.parameters VectorFreePlace 
    }
  }
  
}
