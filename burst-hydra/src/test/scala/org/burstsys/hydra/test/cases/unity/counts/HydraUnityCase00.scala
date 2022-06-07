/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.hydra.test.cases.unity.counts

import org.burstsys.fabric.execution.model.result.group.FabricResultGroup
import org.burstsys.fabric.execution.model.result.row.FabricResultRow
import org.burstsys.hydra.sweep.HydraSweep
import org.burstsys.hydra.test.cases.support.HydraUseCase
import org.burstsys.hydra.test.support.GeneratedUnitySchema.{UnityTraveler_lexicon, UnityTraveler_lexicon_runtime}

object HydraUnityCase00 extends HydraUseCase(666, 666, "unity") {

    override lazy val sweep: HydraSweep = new BCA4E9F234994458CA9BC01262E59F4DB

  override val frameSource: String =
    s"""
         frame myCube {
           cube user {
             limit = 1
             aggregates {
               userCount:sum[long]
               sessionCount:sum[long]
               eventCount:sum[long]
             }
           }
           user ⇒ {
             pre ⇒ {
               myCube.userCount = 1
             }
           }
           user.sessions ⇒ {
             pre ⇒ {
               myCube.sessionCount = 1
             }
           }
           user.sessions.events ⇒ {
             pre ⇒ {
               myCube.eventCount = 1
             }
           }
         }
  """.stripMargin

  override def validate(implicit result: FabricResultGroup): Unit = {

  }

  // -------- begin generated felt 'sweep' with features[lexicon] -----------------------------
  final class BCA4E9F234994458CA9BC01262E59F4DB extends org.burstsys.hydra.sweep.HydraSweep {
    override val feltTraveler = new UnityTraveler_lexicon
    override val sweepName: String = "HydraUnityCase00"
    override val sweepClassName: java.lang.String = "BCA4E9F234994458CA9BC01262E59F4DB"
    // -------- generated runtime class ---------------------------------------------------------
    private class BE4D20E7D69504601A9175766B826D4F8(call:org.burstsys.fabric.execution.model.execute.invoke.FabricInvocation)
      extends org.burstsys.hydra.sweep.HydraRuntime(call) with UnityTraveler_lexicon_runtime {
      // -------- analysis parameter declarations -------------------------------------------------
      // -------- global variables declarations ---------------------------------------------------
      // -------- lexicon declarations ------------------------------------------------------------
      // -------- control verbs declarations... ---------------------------------------------------
      var control_myCube_relation_scope : Int = -1; // inactive
      var control_myCube_member_scope : Int = -1; // inactive
      var control_myCube_discard_scope : Boolean = false;
      // -------- prepare method (called before each sweep) ---------------------------------------
      @inline override
      def generatedPrepare(blob:  org.burstsys.brio.blob.BrioBlob): org.burstsys.felt.model.runtime.FeltRuntime = {
        val rt = this; // needed for var initialization
        val dictionary = blob.dictionary;
        implicit val text:org.burstsys.vitals.text.VitalsTextCodec = org.burstsys.vitals.text.VitalsTextCodec();
        // -------- lexicon initializers - do before parameter/global variable initializations ------
        // -------- parameter vals prepare ----------------------------------------------------------
        // -------- global vars prepare -------------------------------------------------------------
        // -------- control verbs prepare... --------------------------------------------------------
        rt.control_myCube_relation_scope = -1;    // NO RELATION CONTROL VERB PATH CURRENTLY ACTIVE
        rt.control_myCube_member_scope = -1;      // NO MEMBER CONTROL VERB PATH CURRENTLY ACTIVE
        rt.control_myCube_discard_scope = false;  // NO CONTROL VERB DISCARD CURRENTLY ACTIVE
        this
      }
      // -------- release method (called after each sweep) ----------------------------------------
      @inline override
      def generatedRelease: org.burstsys.felt.model.runtime.FeltRuntime = {
        // -------- parameter vals release ----------------------------------------------------------
        this
      }
      // -------- read dictionary from frame to write to plane ------------------------------------
      @inline override
      def frameDictionary(frameId: Int): org.burstsys.brio.dictionary.mutable.BrioMutableDictionary = {
        frameId match {
          case 0 => cube_myCube_dictionary;  // 'myCube'
          case _ => ??? ;
        }
      }
      // -------- write dictionary to frame from plane --------------------------------------------
      @inline override
      def frameDictionary(frameId: Int, dictionary: org.burstsys.brio.dictionary.mutable.BrioMutableDictionary): Unit = {
        frameId match {
          case 0 => cube_myCube_dictionary = dictionary ;  // 'myCube'
          case _ => ??? ;
        }
      }
      // -------- read root collector out of frame for plane --------------------------------------
      @inline override
      def frameCollector(frameId: Int):org.burstsys.felt.model.collectors.runtime.FeltCollector = {
        frameId match {
          case 0 => cube_myCube_root ;  // 'myCube'
          case _ => ??? ;
        }
      }
      // -------- write root collector into frame from plane --------------------------------------
      @inline override
      def frameCollector(frameId: Int, collector:org.burstsys.felt.model.collectors.runtime.FeltCollector):Unit = {
        frameId match {
          case 0 => cube_myCube_root = collector.asInstanceOf[org.burstsys.zap.cube.ZapCube] ;  // myCube
          case _ => ??? ;
        }
      }
      // -------- root variables for 'myCube' -----------------------------------------------------
      var cube_myCube_root:org.burstsys.zap.cube.ZapCube = _ ;
      var cube_myCube_dictionary:org.burstsys.brio.dictionary.mutable.BrioMutableDictionary = _ ;
      // -------- path/cube exchange variables for 'myCube' ---------------------------------------
      // -------- cube static variables for 'myCube' -> 'user'  -----------------------------------
      var cube_myCube_user_instance:org.burstsys.zap.cube.ZapCube = _ ;
      var cube_myCube_user_relation:org.burstsys.zap.cube.ZapCube = _ ;
      // -------- cube static variables for 'myCube' -> 'user.sessions'  --------------------------
      var cube_myCube_user_sessions_instance:org.burstsys.zap.cube.ZapCube = _ ;
      var cube_myCube_user_sessions_relation:org.burstsys.zap.cube.ZapCube = _ ;
      // -------- cube static variables for 'myCube' -> 'user.sessions.events'  -------------------
      var cube_myCube_user_sessions_events_instance:org.burstsys.zap.cube.ZapCube = _ ;
      var cube_myCube_user_sessions_events_relation:org.burstsys.zap.cube.ZapCube = _ ;
      // -------- no dynamic relation visit variables ---------------------------------------------
      // -------- no control verb runtime methods... ----------------------------------------------
    }
    // -------- generated runtime constructor ---------------------------------------------------
    override def newRuntime(call:org.burstsys.fabric.execution.model.execute.invoke.FabricInvocation):org.burstsys.felt.model.runtime.FeltRuntime  = {
      new BE4D20E7D69504601A9175766B826D4F8(call)
    }
    // -------- top level traversal entry point -------------------------------------------------
    @inline override
    def apply(runtime: org.burstsys.felt.model.runtime.FeltRuntime): Unit = {
      feltTraveler(runtime.asInstanceOf[BE4D20E7D69504601A9175766B826D4F8], this)
    }
    // -------- which paths to visit and which to skip ------------------------------------------
    @inline override
    def skipVisitPath(pathKey: Int): Boolean = {
      pathKey match {
        case 1 => false  // 'user'
        case 74 => false  // 'user.sessions'
        case 76 => false  // 'user.sessions.events'
        case _ => true
      }
    }
    // -------- which paths to tunnel and which to skip -----------------------------------------
    @inline override
    def skipTunnelPath(pathKey: Int): Boolean = {
      pathKey match {
        case 1 => false  // 'user'
        case 74 => false  // 'user.sessions'
        case 76 => false  // 'user.sessions.events'
        case _ => true
      }
    }
    // -------- cube builder for 'myCube' -------------------------------------------------------
    val cube_myCube_builder:org.burstsys.zap.cube.ZapCubeBuilder =
      org.burstsys.zap.cube.ZapCubeBuilder(
        1,   // row limit
        // -------- field names ---------------------------------------------------------------------
        scala.Array[String](
          "userCount",
          "sessionCount",
          "eventCount"
        ),
        0, // dimension count
        // -------- dimension semantics -------------------------------------------------------------
        scala.Array[org.burstsys.felt.model.collectors.cube.decl.column.dimension.FeltCubeDimSemRt](
        ),
        // -------- dimension field types -----------------------------------------------------------
        // -------- () ------------------------------------------------------------------------------
        scala.Array[Int](
        ),
        // -------- dimension join mask -------------------------------------------------------------
        org.burstsys.felt.model.collectors.cube.runtime.FeltCubeTreeMask(
          scala.Array[Long](
            0
          )
        ),
        3,   // aggregation count
        // -------- aggregation semantics -----------------------------------------------------------
        scala.Array[org.burstsys.felt.model.collectors.cube.decl.column.aggregation.FeltCubeAggSemRt](
          org.burstsys.felt.model.collectors.cube.decl.column.aggregation.primitive.FeltCubeAggSumSemRt(),
          org.burstsys.felt.model.collectors.cube.decl.column.aggregation.primitive.FeltCubeAggSumSemRt(),
          org.burstsys.felt.model.collectors.cube.decl.column.aggregation.primitive.FeltCubeAggSumSemRt()
        ),
        // -------- aggregation field types ---------------------------------------------------------
        // -------- (Long, Long, Long) --------------------------------------------------------------
        scala.Array[Int](
          6,
          6,
          6
        ),
        // -------- aggregation join mask -----------------------------------------------------------
        org.burstsys.felt.model.collectors.cube.runtime.FeltCubeTreeMask(
          scala.Array[Long](
            7
          )
        )
      ).init( 0, "myCube", feltBinding )
    // -------- tablet globals ------------------------------------------------------------------
    // -------- collector builder list ----------------------------------------------------------
    @inline override
    val collectorBuilders:Array[org.burstsys.felt.model.collectors.runtime.FeltCollectorBuilder] = Array(
      cube_myCube_builder
    )
    // -------- root splices --------------------------------------------------------------------
    @inline override
    def rootSplice(runtime: org.burstsys.felt.model.runtime.FeltRuntime, path:Int, placement: Int): Unit = {
      val rt:BE4D20E7D69504601A9175766B826D4F8 = runtime.asInstanceOf[BE4D20E7D69504601A9175766B826D4F8]
      placement match {
        case 15 => // TraverseCommencePlace
          path match {
            case  1 => // user
              splice_cube_my_cube_user_traverse_commence_place(runtime, rt, path, placement);
            case _ =>
          }
        case 16 => // TraverseCompletePlace
          path match {
            case  1 => // user
              splice_cube_my_cube_user_traverse_complete_place(runtime, rt, path, placement);
            case _ =>
          }
        case _ =>
      }
    }
    // -------- reference scalar static splices -------------------------------------------------
    @inline override
    def referenceScalarSplice(runtime: org.burstsys.felt.model.runtime.FeltRuntime, path: Int, placement: Int): Unit = {
      val rt:BE4D20E7D69504601A9175766B826D4F8 = runtime.asInstanceOf[BE4D20E7D69504601A9175766B826D4F8]
      placement match {
        case 6 => // InstanceAllocPlace
        case 1 => // InstancePrePlace
          path match {
            case  1 => // user
              splice_my_cube_user_instance_pre_place(runtime, rt, path, placement);
            case  74 => // user.sessions
              splice_my_cube_user_sessions_instance_pre_place(runtime, rt, path, placement);
            case  76 => // user.sessions.events
              splice_my_cube_user_sessions_events_instance_pre_place(runtime, rt, path, placement);
            case _ =>
          }
        case 8 => // ChildMergePlace
          path match {
            case  74 => // user.sessions
              splice_cube_my_cube_user_sessions_child_merge_place(runtime, rt, path, placement);
            case  76 => // user.sessions.events
              splice_cube_my_cube_user_sessions_events_child_merge_place(runtime, rt, path, placement);
            case _ =>
          }
        case 2 => // InstancePostPlace
        case 9 => // ChildJoinPlace
        case 7 => // InstanceFreePlace
        case _ =>
      }
    }
    // -------- reference vector static splices -------------------------------------------------
    @inline override
    def referenceVectorSplice(runtime: org.burstsys.felt.model.runtime.FeltRuntime, path: Int, placement: Int): Unit = {
      val rt:BE4D20E7D69504601A9175766B826D4F8 = runtime.asInstanceOf[BE4D20E7D69504601A9175766B826D4F8]
      placement match {
        case 10 =>  // VectorMemberAllocPlace
          path match {
            case  74 => // user.sessions
              splice_cube_my_cube_user_sessions_vector_alloc_place(runtime, rt, path, placement);
            case  76 => // user.sessions.events
              splice_cube_my_cube_user_sessions_events_vector_alloc_place(runtime, rt, path, placement);
            case _ =>
          }
        case 3 =>  // VectorBeforePlace
        case 4 =>  // VectorAfterPlace
        case 11 =>  // VectorFreePlace
          path match {
            case  74 => // user.sessions
              splice_cube_my_cube_user_sessions_vector_free_place(runtime, rt, path, placement);
            case  76 => // user.sessions.events
              splice_cube_my_cube_user_sessions_events_vector_free_place(runtime, rt, path, placement);
            case _ =>
          }
        case _ =>
      }
    }
    @inline override
    def referenceVectorMemberSplice(runtime: org.burstsys.felt.model.runtime.FeltRuntime, path: Int, placement: Int): Unit = {
      val rt:BE4D20E7D69504601A9175766B826D4F8 = runtime.asInstanceOf[BE4D20E7D69504601A9175766B826D4F8]
      placement match {
        case 12 =>  // VectorMemberAllocPlace
          path match {
            case  74 => // user.sessions
              splice_cube_my_cube_user_sessions_vector_member_alloc_place(runtime, rt, path, placement);
            case  76 => // user.sessions.events
              splice_cube_my_cube_user_sessions_events_vector_member_alloc_place(runtime, rt, path, placement);
            case _ =>
          }
        case 14 => // VectorMemberMergePlace
          path match {
            case  74 => // user.sessions
              splice_cube_my_cube_user_sessions_vector_member_merge_place(runtime, rt, path, placement);
            case  76 => // user.sessions.events
              splice_cube_my_cube_user_sessions_events_vector_member_merge_place(runtime, rt, path, placement);
            case _ =>
          }
        case 13 => // VectorMemberFreePlace
        case _ =>
      }
    }
    // -------- no value map static splices -----------------------------------------------------
    // -------- no value vector static splices --------------------------------------------------
    @inline
    def splice_cube_my_cube_user_traverse_commence_place(runtime: org.burstsys.felt.model.runtime.FeltRuntime, rt:BE4D20E7D69504601A9175766B826D4F8, path: Int, placement: Int) : Unit = {
      // -------- cube_my_cube_user_traverse_commence_place header --------------------------------
      val reader = runtime.reader;
      { // cube_my_cube_user_traverse_commence_place callee
        rt.cube_myCube_user_instance = rt.cube_myCube_root;
      }
    }
    @inline
    def splice_cube_my_cube_user_traverse_complete_place(runtime: org.burstsys.felt.model.runtime.FeltRuntime, rt:BE4D20E7D69504601A9175766B826D4F8, path: Int, placement: Int) : Unit = {
      // -------- cube_my_cube_user_traverse_complete_place header --------------------------------
      val reader = runtime.reader;
      { // cube_my_cube_user_traverse_complete_place callee true
        rt.cube_myCube_root = rt.cube_myCube_user_instance;
      }
    }
    @inline
    def splice_my_cube_user_instance_pre_place(runtime: org.burstsys.felt.model.runtime.FeltRuntime, rt:BE4D20E7D69504601A9175766B826D4F8, path: Int, placement: Int) : Unit = {
      // -------- my_cube_user_instance_pre_place header ------------------------------------------
      val reader = runtime.reader;
      var s_0_ScpNull:Boolean = false; var s_0_ScpVal:Long = 0; // my_cube_user_instance_pre_place-caller-range-decl
      { // my_cube_user_instance_pre_place callee
        { // CALLER(s_0) felt-eblk [{ myCube.userCount = 1 } -> long]
          { // felt-eblk-stat#1 [myCube.userCount = 1]
            var s_7_ScpNull:Boolean = false; var s_7_ScpVal:Long = 0; // felt-eblk-stat#1-caller-range-decl
            // -------- CALLER(s_7) felt-assign-expr [myCube.userCount = 1 -> long] ---------------------
            {
              var s_8_ScpNull:Boolean = false; var s_8_ScpVal:Long = 0; // felt-assign-expr-caller-range-decl
              s_8_ScpNull = false; s_8_ScpVal = 1; // FELT-BYTE-ATOM
              // -------- CALLER(s_8) felt-cube-agg-ref-agg-write [myCube.userCount -> long] --------------
              if(s_8_ScpNull) {
                rt.cube_myCube_user_instance.writeAggregationNull(cube_myCube_builder, rt.cube_myCube_user_instance, 0) // ROOT.HydraUnityCase00.myCube.userCount
              } else {
                val semantic = cube_myCube_builder.aggregationSemantics(0); // ROOT.HydraUnityCase00.myCube.userCount
                val wasNull = rt.cube_myCube_user_instance.readAggregationNull(cube_myCube_builder, rt.cube_myCube_user_instance, 0); // ROOT.HydraUnityCase00.myCube.userCount
                val oldValue: Long = if (wasNull) {
                  semantic.doLongInit();
                } else {
                  rt.cube_myCube_user_instance.readAggregationPrimitive(cube_myCube_builder, rt.cube_myCube_user_instance, 0) // ROOT.HydraUnityCase00.myCube.userCount
                }
                val newValue = semantic.doLong(oldValue, s_8_ScpVal);
                rt.cube_myCube_user_instance.writeAggregationPrimitive(cube_myCube_builder, rt.cube_myCube_user_instance, 0, newValue) // ROOT.HydraUnityCase00.myCube.userCount
              }
              if ( s_8_ScpNull ) s_7_ScpNull = true; else { s_7_ScpNull = false;s_7_ScpVal = s_8_ScpVal; } // felt-assign-expr-callee-range-return
            }
            if ( s_7_ScpNull ) s_0_ScpNull = true; else { s_0_ScpNull = false;s_0_ScpVal = s_7_ScpVal; } // felt-eblk-callee-range-return
          }
        }
      }
    }
    @inline
    def splice_my_cube_user_sessions_instance_pre_place(runtime: org.burstsys.felt.model.runtime.FeltRuntime, rt:BE4D20E7D69504601A9175766B826D4F8, path: Int, placement: Int) : Unit = {
      // -------- my_cube_user_sessions_instance_pre_place header ---------------------------------
      val reader = runtime.reader;
      var s_0_ScpNull:Boolean = false; var s_0_ScpVal:Long = 0; // my_cube_user_sessions_instance_pre_place-caller-range-decl
      { // my_cube_user_sessions_instance_pre_place callee
        { // CALLER(s_0) felt-eblk [{ myCube.sessionCount = 1 } -> long]
          { // felt-eblk-stat#1 [myCube.sessionCount = 1]
            var s_9_ScpNull:Boolean = false; var s_9_ScpVal:Long = 0; // felt-eblk-stat#1-caller-range-decl
            // -------- CALLER(s_9) felt-assign-expr [myCube.sessionCount = 1 -> long] ------------------
            {
              var s_10_ScpNull:Boolean = false; var s_10_ScpVal:Long = 0; // felt-assign-expr-caller-range-decl
              s_10_ScpNull = false; s_10_ScpVal = 1; // FELT-BYTE-ATOM
              // -------- CALLER(s_10) felt-cube-agg-ref-agg-write [myCube.sessionCount -> long] ----------
              if(s_10_ScpNull) {
                rt.cube_myCube_user_sessions_instance.writeAggregationNull(cube_myCube_builder, rt.cube_myCube_user_sessions_instance, 1) // ROOT.HydraUnityCase00.myCube.sessionCount
              } else {
                val semantic = cube_myCube_builder.aggregationSemantics(1); // ROOT.HydraUnityCase00.myCube.sessionCount
                val wasNull = rt.cube_myCube_user_sessions_instance.readAggregationNull(cube_myCube_builder, rt.cube_myCube_user_sessions_instance, 1); // ROOT.HydraUnityCase00.myCube.sessionCount
                val oldValue: Long = if (wasNull) {
                  semantic.doLongInit();
                } else {
                  rt.cube_myCube_user_sessions_instance.readAggregationPrimitive(cube_myCube_builder, rt.cube_myCube_user_sessions_instance, 1) // ROOT.HydraUnityCase00.myCube.sessionCount
                }
                val newValue = semantic.doLong(oldValue, s_10_ScpVal);
                rt.cube_myCube_user_sessions_instance.writeAggregationPrimitive(cube_myCube_builder, rt.cube_myCube_user_sessions_instance, 1, newValue) // ROOT.HydraUnityCase00.myCube.sessionCount
              }
              if ( s_10_ScpNull ) s_9_ScpNull = true; else { s_9_ScpNull = false;s_9_ScpVal = s_10_ScpVal; } // felt-assign-expr-callee-range-return
            }
            if ( s_9_ScpNull ) s_0_ScpNull = true; else { s_0_ScpNull = false;s_0_ScpVal = s_9_ScpVal; } // felt-eblk-callee-range-return
          }
        }
      }
    }
    @inline
    def splice_my_cube_user_sessions_events_instance_pre_place(runtime: org.burstsys.felt.model.runtime.FeltRuntime, rt:BE4D20E7D69504601A9175766B826D4F8, path: Int, placement: Int) : Unit = {
      // -------- my_cube_user_sessions_events_instance_pre_place header --------------------------
      val reader = runtime.reader;
      var s_0_ScpNull:Boolean = false; var s_0_ScpVal:Long = 0; // my_cube_user_sessions_events_instance_pre_place-caller-range-decl
      { // my_cube_user_sessions_events_instance_pre_place callee
        { // CALLER(s_0) felt-eblk [{ myCube.eventCount = 1 } -> long]
          { // felt-eblk-stat#1 [myCube.eventCount = 1]
            var s_11_ScpNull:Boolean = false; var s_11_ScpVal:Long = 0; // felt-eblk-stat#1-caller-range-decl
            // -------- CALLER(s_11) felt-assign-expr [myCube.eventCount = 1 -> long] -------------------
            {
              var s_12_ScpNull:Boolean = false; var s_12_ScpVal:Long = 0; // felt-assign-expr-caller-range-decl
              s_12_ScpNull = false; s_12_ScpVal = 1; // FELT-BYTE-ATOM
              // -------- CALLER(s_12) felt-cube-agg-ref-agg-write [myCube.eventCount -> long] ------------
              if(s_12_ScpNull) {
                rt.cube_myCube_user_sessions_events_instance.writeAggregationNull(cube_myCube_builder, rt.cube_myCube_user_sessions_events_instance, 2) // ROOT.HydraUnityCase00.myCube.eventCount
              } else {
                val semantic = cube_myCube_builder.aggregationSemantics(2); // ROOT.HydraUnityCase00.myCube.eventCount
                val wasNull = rt.cube_myCube_user_sessions_events_instance.readAggregationNull(cube_myCube_builder, rt.cube_myCube_user_sessions_events_instance, 2); // ROOT.HydraUnityCase00.myCube.eventCount
                val oldValue: Long = if (wasNull) {
                  semantic.doLongInit();
                } else {
                  rt.cube_myCube_user_sessions_events_instance.readAggregationPrimitive(cube_myCube_builder, rt.cube_myCube_user_sessions_events_instance, 2) // ROOT.HydraUnityCase00.myCube.eventCount
                }
                val newValue = semantic.doLong(oldValue, s_12_ScpVal);
                rt.cube_myCube_user_sessions_events_instance.writeAggregationPrimitive(cube_myCube_builder, rt.cube_myCube_user_sessions_events_instance, 2, newValue) // ROOT.HydraUnityCase00.myCube.eventCount
              }
              if ( s_12_ScpNull ) s_11_ScpNull = true; else { s_11_ScpNull = false;s_11_ScpVal = s_12_ScpVal; } // felt-assign-expr-callee-range-return
            }
            if ( s_11_ScpNull ) s_0_ScpNull = true; else { s_0_ScpNull = false;s_0_ScpVal = s_11_ScpVal; } // felt-eblk-callee-range-return
          }
        }
      }
    }
    @inline
    def splice_cube_my_cube_user_sessions_child_merge_place(runtime: org.burstsys.felt.model.runtime.FeltRuntime, rt:BE4D20E7D69504601A9175766B826D4F8, path: Int, placement: Int) : Unit = {
      // -------- cube_my_cube_user_sessions_child_merge_place header -----------------------------
      val reader = runtime.reader;
      { // cube_my_cube_user_sessions_child_merge_place callee
        if ( rt.cube_myCube_user_sessions_relation != null ) {
          if ( !rt.cube_myCube_user_sessions_relation.isEmpty ) {
            rt.cube_myCube_user_instance.intraMerge(
              cube_myCube_builder,
              rt.cube_myCube_user_instance, // this cube (destination)
              rt.cube_myCube_dictionary,
              rt.cube_myCube_user_sessions_relation,   // that cube (source)
              rt.cube_myCube_dictionary,
              org.burstsys.vitals.bitmap.VitalsBitMapAnyVal( 0 ),  org.burstsys.vitals.bitmap.VitalsBitMapAnyVal( 7 )
            );
          }
          feltBinding.collectors.cubes.releaseCollector( rt.cube_myCube_user_sessions_relation );
          rt.cube_myCube_user_sessions_relation = null;
        }
      }
    }
    @inline
    def splice_cube_my_cube_user_sessions_events_child_merge_place(runtime: org.burstsys.felt.model.runtime.FeltRuntime, rt:BE4D20E7D69504601A9175766B826D4F8, path: Int, placement: Int) : Unit = {
      // -------- cube_my_cube_user_sessions_events_child_merge_place header ----------------------
      val reader = runtime.reader;
      { // cube_my_cube_user_sessions_events_child_merge_place callee
        if ( rt.cube_myCube_user_sessions_events_relation != null ) {
          if ( !rt.cube_myCube_user_sessions_events_relation.isEmpty ) {
            rt.cube_myCube_user_sessions_instance.intraMerge(
              cube_myCube_builder,
              rt.cube_myCube_user_sessions_instance, // this cube (destination)
              rt.cube_myCube_dictionary,
              rt.cube_myCube_user_sessions_events_relation,   // that cube (source)
              rt.cube_myCube_dictionary,
              org.burstsys.vitals.bitmap.VitalsBitMapAnyVal( 0 ),  org.burstsys.vitals.bitmap.VitalsBitMapAnyVal( 7 )
            );
          }
          feltBinding.collectors.cubes.releaseCollector( rt.cube_myCube_user_sessions_events_relation );
          rt.cube_myCube_user_sessions_events_relation = null;
        }
      }
    }
    @inline
    def splice_cube_my_cube_user_sessions_vector_alloc_place(runtime: org.burstsys.felt.model.runtime.FeltRuntime, rt:BE4D20E7D69504601A9175766B826D4F8, path: Int, placement: Int) : Unit = {
      // -------- cube_my_cube_user_sessions_vector_alloc_place header ----------------------------
      val reader = runtime.reader;
      { // cube_my_cube_user_sessions_vector_alloc_place callee
        rt.cube_myCube_user_sessions_relation  = feltBinding.collectors.cubes.grabCollector(cube_myCube_builder).asInstanceOf[org.burstsys.zap.cube.ZapCube];
      }
    }
    @inline
    def splice_cube_my_cube_user_sessions_events_vector_alloc_place(runtime: org.burstsys.felt.model.runtime.FeltRuntime, rt:BE4D20E7D69504601A9175766B826D4F8, path: Int, placement: Int) : Unit = {
      // -------- cube_my_cube_user_sessions_events_vector_alloc_place header ---------------------
      val reader = runtime.reader;
      { // cube_my_cube_user_sessions_events_vector_alloc_place callee
        rt.cube_myCube_user_sessions_events_relation  = feltBinding.collectors.cubes.grabCollector(cube_myCube_builder).asInstanceOf[org.burstsys.zap.cube.ZapCube];
      }
    }
    @inline
    def splice_cube_my_cube_user_sessions_vector_free_place(runtime: org.burstsys.felt.model.runtime.FeltRuntime, rt:BE4D20E7D69504601A9175766B826D4F8, path: Int, placement: Int) : Unit = {
      // -------- cube_my_cube_user_sessions_vector_free_place header -----------------------------
      val reader = runtime.reader;
      { // cube_my_cube_user_sessions_vector_free_place callee
        if ( rt.cube_myCube_user_sessions_instance != null ) feltBinding.collectors.cubes.releaseCollector( rt.cube_myCube_user_sessions_instance );
        rt.cube_myCube_user_sessions_instance = null;
      }
    }
    @inline
    def splice_cube_my_cube_user_sessions_events_vector_free_place(runtime: org.burstsys.felt.model.runtime.FeltRuntime, rt:BE4D20E7D69504601A9175766B826D4F8, path: Int, placement: Int) : Unit = {
      // -------- cube_my_cube_user_sessions_events_vector_free_place header ----------------------
      val reader = runtime.reader;
      { // cube_my_cube_user_sessions_events_vector_free_place callee
        if ( rt.cube_myCube_user_sessions_events_instance != null ) feltBinding.collectors.cubes.releaseCollector( rt.cube_myCube_user_sessions_events_instance );
        rt.cube_myCube_user_sessions_events_instance = null;
      }
    }
    @inline
    def splice_cube_my_cube_user_sessions_vector_member_alloc_place(runtime: org.burstsys.felt.model.runtime.FeltRuntime, rt:BE4D20E7D69504601A9175766B826D4F8, path: Int, placement: Int) : Unit = {
      // -------- cube_my_cube_user_sessions_vector_member_alloc_place header ---------------------
      val reader = runtime.reader;
      { // cube_my_cube_user_sessions_vector_member_alloc_place callee
        rt.cube_myCube_user_sessions_instance = if ( rt.cube_myCube_user_sessions_instance == null ) {
          feltBinding.collectors.cubes.grabCollector( cube_myCube_builder ).asInstanceOf[ org.burstsys.zap.cube.ZapCube ]
        } else {
          rt.cube_myCube_user_sessions_instance.clear();
          rt.cube_myCube_user_sessions_instance.initCursor( cube_myCube_builder, rt.cube_myCube_user_sessions_instance );
          rt.cube_myCube_user_sessions_instance
        }
        rt.cube_myCube_user_sessions_instance.inheritCursor( cube_myCube_builder, rt.cube_myCube_user_sessions_instance, rt.cube_myCube_user_instance );
      }
    }
    @inline
    def splice_cube_my_cube_user_sessions_events_vector_member_alloc_place(runtime: org.burstsys.felt.model.runtime.FeltRuntime, rt:BE4D20E7D69504601A9175766B826D4F8, path: Int, placement: Int) : Unit = {
      // -------- cube_my_cube_user_sessions_events_vector_member_alloc_place header --------------
      val reader = runtime.reader;
      { // cube_my_cube_user_sessions_events_vector_member_alloc_place callee
        rt.cube_myCube_user_sessions_events_instance = if ( rt.cube_myCube_user_sessions_events_instance == null ) {
          feltBinding.collectors.cubes.grabCollector( cube_myCube_builder ).asInstanceOf[ org.burstsys.zap.cube.ZapCube ]
        } else {
          rt.cube_myCube_user_sessions_events_instance.clear();
          rt.cube_myCube_user_sessions_events_instance.initCursor( cube_myCube_builder, rt.cube_myCube_user_sessions_events_instance );
          rt.cube_myCube_user_sessions_events_instance
        }
        rt.cube_myCube_user_sessions_events_instance.inheritCursor( cube_myCube_builder, rt.cube_myCube_user_sessions_events_instance, rt.cube_myCube_user_sessions_instance );
      }
    }
    @inline
    def splice_cube_my_cube_user_sessions_vector_member_merge_place(runtime: org.burstsys.felt.model.runtime.FeltRuntime, rt:BE4D20E7D69504601A9175766B826D4F8, path: Int, placement: Int) : Unit = {
      // -------- cube_my_cube_user_sessions_vector_member_merge_place header ---------------------
      val reader = runtime.reader;
      { // cube_my_cube_user_sessions_vector_member_merge_place callee
        rt.cube_myCube_user_sessions_relation.intraMerge(
          cube_myCube_builder,
          rt.cube_myCube_user_sessions_relation, rt.cube_myCube_dictionary,
          rt.cube_myCube_user_sessions_instance, rt.cube_myCube_dictionary,
          org.burstsys.vitals.bitmap.VitalsBitMapAnyVal( 0 ), org.burstsys.vitals.bitmap.VitalsBitMapAnyVal( 7 )
        );
      }
    }
    @inline
    def splice_cube_my_cube_user_sessions_events_vector_member_merge_place(runtime: org.burstsys.felt.model.runtime.FeltRuntime, rt:BE4D20E7D69504601A9175766B826D4F8, path: Int, placement: Int) : Unit = {
      // -------- cube_my_cube_user_sessions_events_vector_member_merge_place header --------------
      val reader = runtime.reader;
      { // cube_my_cube_user_sessions_events_vector_member_merge_place callee
        rt.cube_myCube_user_sessions_events_relation.intraMerge(
          cube_myCube_builder,
          rt.cube_myCube_user_sessions_events_relation, rt.cube_myCube_dictionary,
          rt.cube_myCube_user_sessions_events_instance, rt.cube_myCube_dictionary,
          org.burstsys.vitals.bitmap.VitalsBitMapAnyVal( 0 ), org.burstsys.vitals.bitmap.VitalsBitMapAnyVal( 7 )
        );
      }
    }
    // -------- dynamic relations splices -------------------------------------------------------
    @inline override
    def dynamicRelationSplices(runtime: org.burstsys.felt.model.runtime.FeltRuntime, path: Int, placement: Int): Unit = {
      val rt:BE4D20E7D69504601A9175766B826D4F8 = runtime.asInstanceOf[BE4D20E7D69504601A9175766B826D4F8];
      placement match {
        case _ =>
      }
    }
    // -------- no dynamic splices... -----------------------------------------------------------
  }
  // -------- end generated hydra felt sweep BCA4E9F234994458CA9BC01262E59F4DB ----------------


}
