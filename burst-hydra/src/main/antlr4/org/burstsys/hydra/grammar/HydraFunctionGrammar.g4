//////////////////////////////////////////////////////////////////
// Antlr Grammar for Hydra Expressions
//////////////////////////////////////////////////////////////////

grammar HydraFunctionGrammar;

import HydraLexerGrammar;

// high level taxonomy of function types/sources
functionType:
        datetimeGrainType
    |   datetimeOrdinalType
    |   datetimeDurationType
    |   datetimeTicksType
    |   groupingFunctions
    |   nowTime
    |   brioFunctions
    |   cubeFunctions
    |   controlVerbs
    |   routeFunctions
    |   tabletFunctions
    ;

// returns a LONG representing the EPOCH time the scan was started
nowTime: NOW ;

//////////////////////////////////////////////////////////////////
// Brio - various brio data model specific verbs
//////////////////////////////////////////////////////////////////

brioFunctions:
    SIZE | CONTAINS | KEY | VALUE | KEYS | VALUES | IS_FIRST | IS_LAST
    ;

//////////////////////////////////////////////////////////////////
// control - verbs to control traversal and data collection semantics in scans
//////////////////////////////////////////////////////////////////

controlVerbs:
    ABORT_MEMBER | COMMIT_MEMBER | ABORT_RELATION | COMMIT_RELATION
     ;

//////////////////////////////////////////////////////////////////
// Cube Functions - zap cube ops
//////////////////////////////////////////////////////////////////

cubeFunctions: INSERT ;

//////////////////////////////////////////////////////////////////
// Grouping
//////////////////////////////////////////////////////////////////

groupingFunctions: ENUM | SPLIT ;

//////////////////////////////////////////////////////////////////
// Time Functions
//////////////////////////////////////////////////////////////////

datetimeGrainType:
    QUARTER_GRAIN | HALF_GRAIN | MONTH_GRAIN | DAY_GRAIN | WEEK_GRAIN
    | HOUR_GRAIN | YEAR_GRAIN | MINUTE_GRAIN | SECOND_GRAIN
    ;

datetimeOrdinalType:
    WEEK_OF_YEAR_ORDINAL | HOUR_OF_DAY_ORDINAL | YEAR_OF_ERA_ORDINAL | DAY_OF_WEEK_ORDINAL | DAY_OF_MONTH_ORDINAL
    | MONTH_OF_YEAR_ORDINAL | DAY_OF_YEAR_ORDINAL | MINUTE_OF_HOUR_ORDINAL | SECOND_OF_MINUTE_ORDINAL
    ;

datetimeDurationType:
    WEEK_DURATION | DAY_DURATION | HOUR_DURATION | MINUTE_DURATION | SECOND_DURATION
    ;

datetimeTicksType:
    WEEK_TICKS | DAY_TICKS | HOUR_TICKS | MINUTE_TICKS | SECOND_TICKS
    ;

//////////////////////////////////////////////////////////////////
// Route Functions
//////////////////////////////////////////////////////////////////

routeFunctions:
    ROUTE_COMPLETE_PATHS | ROUTE_LAST_PATH_IS_COMPLETE | ROUTE_LAST_PATH_ORDINAL
    | ROUTE_LAST_STEP_ORDINAL | ROUTE_LAST_STEP_KEY | ROUTE_LAST_STEP_TAG |  ROUTE_LAST_STEP_TIME

    | ROUTE_FSM_ASSERT_STEP | ROUTE_FSM_ASSERT_TIME | ROUTE_FSM_END_PATH |  ROUTE_FSM_IN_PATH
    | ROUTE_FSM_IN_STEP | ROUTE_FSM_IS_EMPTY | ROUTE_FSM_BACK_FILL

    | ROUTE_SCOPE_ABORT | ROUTE_SCOPE_COMMIT | ROUTE_SCOPE_CURRENT_PATH | ROUTE_SCOPE_CURRENT_STEP
    | ROUTE_SCOPE_PATH_CHANGED | ROUTE_SCOPE_PRIOR_PATH | ROUTE_SCOPE_PRIOR_STEP | ROUTE_SCOPE_START
    | ROUTE_SCOPE_STEP_CHANGED

    | ROUTE_VISIT_PATH_ORDINAL  | ROUTE_VISIT_PATH_IS_COMPLETE | ROUTE_VISIT_PATH_IS_FIRST | ROUTE_VISIT_PATH_IS_LAST

    | ROUTE_VISIT_STEP_ORDINAL | ROUTE_VISIT_STEP_KEY | ROUTE_VISIT_STEP_TAG | ROUTE_VISIT_STEP_TIME
    | ROUTE_VISIT_STEP_IS_FIRST | ROUTE_VISIT_STEP_IS_LAST | ROUTE_VISIT_STEP_IS_LAST_IN_PATH

    ;

 tabletFunctions:
    TABLET_MEMBER_VALUE | TABLET_MEMBER_ADD | TABLET_MEMBER_IS_FIRST | TABLET_MEMBER_IS_LAST
    ;
