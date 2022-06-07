//////////////////////////////////////////////////////////////////
// Antlr Grammar for Hydra Tokens
//////////////////////////////////////////////////////////////////

lexer grammar HydraLexerGrammar;

//////////////////////////////////////////////////////////////////
// PUNCTUATION
//////////////////////////////////////////////////////////////////
UNIMPLEMENTED: '???' ;
LP: '(' ;
RP: ')' ;
SEP: ',' ;
COLON: ':' ;
LB: '{' ;
RB: '}' ;
LSB: '[' ;
RSB: ']' ;
USC: '_' ;

//////////////////////////////////////////////////////////////////
// higher level comparison/set expressions
//////////////////////////////////////////////////////////////////
IN:         'in' ;           // set inclusion
BETWEEN:    'between' ;     // value ranges

//////////////////////////////////////////////////////////////////
// Analysis/Query
//////////////////////////////////////////////////////////////////
HYDRA:      'hydra' ;
SCHEMA:     'schema' ;
FRAME:      'frame' ;

//////////////////////////////////////////////////////////////////
// Method
//////////////////////////////////////////////////////////////////
DEF:    'def' ;
RETURN: 'return' ;

//////////////////////////////////////////////////////////////////
// Visit Actions
//////////////////////////////////////////////////////////////////
PRE:        'pre' ;
POST:       'post' ;
SITU:       'situ' ;
BEFORE:     'before' ;
AFTER:      'after' ;

//////////////////////////////////////////////////////////////////
// Brio Functions
//////////////////////////////////////////////////////////////////
SIZE:       'size' ;
CONTAINS:   'contains' ;
KEY:        'key' ;
VALUE:      'value' ;
KEYS:       'keys' ;
VALUES:     'values' ;
IS_FIRST:     'isFirst' ;
IS_LAST:     'isLast' ;

//////////////////////////////////////////////////////////////////
// Control Verbs
//////////////////////////////////////////////////////////////////
ABORT_MEMBER:  'abortMember' ;
COMMIT_MEMBER: 'commitMember' ;
ABORT_RELATION:    'abortRelation' ;
COMMIT_RELATION:   'commitRelation' ;

//////////////////////////////////////////////////////////////////
// Cubes
//////////////////////////////////////////////////////////////////
SUM:        'sum' ;
UNIQUE:     'unique' ;
MAX:        'max' ;
MIN:        'min' ;
PROJECT:    'project' ;

TOP:        'top' ;
BOTTOM:     'bottom' ;
LIMIT:      'limit' ;

VERBATIM:   'verbatim'  ;
ENUM:       'enum'  ;
SPLIT:      'split' ;

INSERT:     'insert'  ;

//////////////////////////////////////////////////////////////////
// time utilities
//////////////////////////////////////////////////////////////////

NOW:  'now';
DATETIME:  'datetime';
TZ:  'timezone';

//////////////////////////////////////////////////////////////////
// datetime ordinals - return calendar unit ordinal  for given epoch time
//////////////////////////////////////////////////////////////////
SECOND_OF_MINUTE_ORDINAL: 'secondOfMinuteOrdinal';
HOUR_OF_DAY_ORDINAL:      'hourOfDayOrdinal';
MINUTE_OF_HOUR_ORDINAL:   'minuteOfHourOrdinal';
DAY_OF_WEEK_ORDINAL:      'dayOfWeekOrdinal';
DAY_OF_MONTH_ORDINAL:     'dayOfMonthOrdinal';
DAY_OF_YEAR_ORDINAL:      'dayOfYearOrdinal';
WEEK_OF_YEAR_ORDINAL:     'weekOfYearOrdinal';
MONTH_OF_YEAR_ORDINAL:    'monthOfYearOrdinal';
YEAR_OF_ERA_ORDINAL:      'yearOfEraOrdinal';

//////////////////////////////////////////////////////////////////
// datetime grains - truncate given epoch time to calendar unit
//////////////////////////////////////////////////////////////////
YEAR_GRAIN:       'yearGrain' ;
QUARTER_GRAIN:    'quarterGrain' ;
HALF_GRAIN:       'halfGrain' ;
MONTH_GRAIN:      'monthGrain' ;
DAY_GRAIN:        'dayGrain' ;
WEEK_GRAIN:       'weekGrain' ;
HOUR_GRAIN:       'hourGrain' ;
MINUTE_GRAIN:     'minuteGrain' ;
SECOND_GRAIN:     'secondGrain' ;

//////////////////////////////////////////////////////////////////
// datetime duration - truncate given epoch time to time unit
//////////////////////////////////////////////////////////////////
SECOND_DURATION:    'secondDuration';
MINUTE_DURATION:    'minuteDuration';
HOUR_DURATION:      'hourDuration';
DAY_DURATION:       'dayDuration';
WEEK_DURATION:      'weekDuration';

//////////////////////////////////////////////////////////////////
// datetime ticks - return ms ticks for a given time unity quantity
//////////////////////////////////////////////////////////////////
SECOND_TICKS:    'secondTicks';
MINUTE_TICKS:    'minuteTicks';
HOUR_TICKS:      'hourTicks';
DAY_TICKS:       'dayTicks';
WEEK_TICKS:      'weekTicks';

//////////////////////////////////////////////////////////////////
// Types/Initializers
//////////////////////////////////////////////////////////////////
SET:        'set' ;
ARRAY:     'array' ;
MAP:        'map' ;

//////////////////////////////////////////////////////////////////
// Cubes
//////////////////////////////////////////////////////////////////
CUBE:           'cube' ;
DIMENSIONS:      'dimensions' ;
AGGREGATES:      'aggregates' ;

//////////////////////////////////////////////////////////////////
// Tablet
//////////////////////////////////////////////////////////////////
TABLET: 'tablet' ;

//////////////////////////////////////////////////////////////////
// Shrub
//////////////////////////////////////////////////////////////////
SHRUB: 'shrub' ;

//////////////////////////////////////////////////////////////////
// Routes
//////////////////////////////////////////////////////////////////
ROUTE:      'route' ;
GRAPH:      'graph' ;

// route parameters
MAX_STEPS_PER_ROUTE:    'maxSteps' ;
MAX_PARTIAL_PATHS:      'maxPartialPaths' ;
MAX_COMPLETE_PATHS:     'maxCompletePaths' ;
MAX_PATH_TIME:          'maxPathTime' ;

// step traits
ENTER_TRAIT:        'enter' ;
TACIT_TRAIT:         'tacit' ;
EXIT_TRAIT:         'exit' ;
BEGIN_TRAIT:        'begin' ;
END_TRAIT:          'end' ;
COMPLETE_TRAIT:     'complete' ;

// destination step for edge
TO:         'to' ;

ROUTE_COMPLETE_PATHS:           'routeCompletePaths';
ROUTE_LAST_PATH_IS_COMPLETE:    'routeLastPathIsComplete';
ROUTE_LAST_PATH_ORDINAL:        'routeLastPathOrdinal';
ROUTE_LAST_STEP_ORDINAL:        'routeLastStepOrdinal';
ROUTE_LAST_STEP_KEY:            'routeLastStepKey';
ROUTE_LAST_STEP_TAG:            'routeLastStepTag';
ROUTE_LAST_STEP_TIME:           'routeLastStepTime';

// FSM FUNCTIONS
ROUTE_FSM_ASSERT_STEP:          'routeFsmStepAssert';
ROUTE_FSM_ASSERT_TIME:          'routeFsmAssertTime';
ROUTE_FSM_END_PATH:             'routeFsmEndPath';
ROUTE_FSM_BACK_FILL:            'routeFsmBackFill';
ROUTE_FSM_IN_PATH:              'routeFsmInPath';
ROUTE_FSM_IN_STEP:              'routeFsmInStep';
ROUTE_FSM_IS_EMPTY:             'routeFsmIsEmpty';

// SCOPE FUNCTIONS
ROUTE_SCOPE_ABORT:              'routeScopeAbort';
ROUTE_SCOPE_COMMIT:             'routeScopeCommit';
ROUTE_SCOPE_CURRENT_PATH:       'routeScopeCurrentPath';
ROUTE_SCOPE_CURRENT_STEP:       'routeScopeCurrentStep';
ROUTE_SCOPE_PATH_CHANGED:       'routeScopePathChanged';
ROUTE_SCOPE_PRIOR_PATH:         'routeScopePriorPath';
ROUTE_SCOPE_PRIOR_STEP:         'routeScopePriorStep';
ROUTE_SCOPE_START:              'routeScopeStart';
ROUTE_SCOPE_STEP_CHANGED:       'routeScopeStepChanged';

// VISIT FUNCTIONS
ROUTE_VISIT_PATH_ORDINAL:           'routeVisitPathOrdinal';
ROUTE_VISIT_STEP_ORDINAL:           'routeVisitStepOrdinal';
ROUTE_VISIT_STEP_KEY:               'routeVisitStepKey';
ROUTE_VISIT_STEP_TAG:               'routeVisitStepTag';
ROUTE_VISIT_STEP_TIME:              'routeVisitStepTime';
ROUTE_VISIT_STEP_IS_FIRST:          'routeVisitStepIsFirst';
ROUTE_VISIT_STEP_IS_LAST:           'routeVisitStepIsLast';
ROUTE_VISIT_STEP_IS_LAST_IN_PATH:   'routeVisitStepIsLastInPath';
ROUTE_VISIT_PATH_IS_COMPLETE:       'routeVisitPathIsComplete';
ROUTE_VISIT_PATH_IS_FIRST:          'routeVisitPathIsFirst';
ROUTE_VISIT_PATH_IS_LAST:           'routeVisitPathIsLast';


// tablet visit
TABLET_MEMBER_VALUE:            'tabletMemberValue';
TABLET_MEMBER_ADD:              'tabletMemberAdd';
TABLET_MEMBER_IS_FIRST:         'tabletMemberIsFirst';
TABLET_MEMBER_IS_LAST:          'tabletMemberIsLast';

//////////////////////////////////////////////////////////////////
// Control Flow
//////////////////////////////////////////////////////////////////
IF:         'if' ;
ELSE:       'else' ;
ELSENULL:   'elseNull' ;
MATCH:      'match' ;
CASE:       'case' ;

//////////////////////////////////////////////////////////////////
// VALUE -> BOOLEAN Operators
//////////////////////////////////////////////////////////////////
EQ  : '==';
NEQ : '<>' | '!=';
LT  : '<';
LTE : '<=';
GT  : '>';
GTE : '>=';

//////////////////////////////////////////////////////////////////
// BOOLEAN Operators
//////////////////////////////////////////////////////////////////
AND  : '&&';
OR  : '||';
NOT  : '!' | 'not';

//////////////////////////////////////////////////////////////////
//  Math
//////////////////////////////////////////////////////////////////
PLUS:       '+';
PLUS_EQ:    '+=';
MINUS:      '-';
MINUS_EQ:   '-=';
ASTERISK:   '*';
SLASH:      '/';
PERCENT:    '%';

//////////////////////////////////////////////////////////////////
// cast/assignment
//////////////////////////////////////////////////////////////////
ASSIGN: '=' ;
CAST:   'cast'  ;
AS:     'as'  ;



//////////////////////////////////////////////////////////////////
// Variables
//////////////////////////////////////////////////////////////////
VAL:    'val' ;
VAR:    'var' ;

//////////////////////////////////////////////////////////////////
// Data types Tokens
//////////////////////////////////////////////////////////////////

BOOLEAN_TYPE:   'boolean' ;
BYTE_TYPE:      'byte' ;
SHORT_TYPE:     'short' ;
INTEGER_TYPE:   'integer' ;
LONG_TYPE:      'long' ;
DOUBLE_TYPE:    'double' ;
STRING_TYPE:    'string' ;

//////////////////////////////////////////////////////////////////
// Literal Tokens
//////////////////////////////////////////////////////////////////

NULL_LITERAL: 'null' ;

BOOLEAN_LITERAL : 'true' | 'false' ;

FIXED_LITERAL:  ((DIGIT)+) 'L'?  ;

FLOAT_LITERAL
    : DIGIT+ '.' DIGIT*
    | '.' DIGIT+
    | DIGIT+ ('.' DIGIT*)? EXPONENT
    | '.' DIGIT+ EXPONENT
    ;

//////////////////////////////////////////////////////////////////
// Identifier Tokens
//////////////////////////////////////////////////////////////////

IDENTIFIER :
    RAW_IDENTIFER
    |  '\'' RAW_IDENTIFER '\''
    ;
RAW_IDENTIFER :
    (LETTER | '_') (LETTER | DIGIT | '_')*
    ;

STRING_LITERAL :
    ('"' ( ~'"' | '""' | '\\"' )* '"')
    ;

//////////////////////////////////////////////////////////////////
// FANCY
//////////////////////////////////////////////////////////////////

LAMBDA: [\u21d2] | '=>' ;
ARROW_RIGHT_ASSOC: [\u2192] | '->' ;
ARROW_LEFT_ASSOC: [\u2190] | '<-' ;

//////////////////////////////////////////////////////////////////
// NUMBER HELPERS
//////////////////////////////////////////////////////////////////

fragment EXPONENT : 'E' [+-]? DIGIT+ ;

fragment DIGIT : [0-9] ;

fragment LETTER : [A-Z] | [a-z] ;

//////////////////////////////////////////////////////////////////
// White Space
//////////////////////////////////////////////////////////////////

WS  :  [ \t\r\n\u000C]+ -> skip ;

COMMENT :   '/*' .*? '*/' -> channel(HIDDEN) ;

LINE_COMMENT :   '//' ~[\r\n]* -> channel(HIDDEN) ;

UNRECOGNIZED : . ;
