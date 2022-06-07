grammar Tokens;

identifier
    : IDENTIFIER            #unquotedIdentifier
    | STRING_LITERAL        #quotedIdentifierAlternative
    | DIGIT_IDENTIFIER      #digitIdentifier
    ;


FUNNEL: ('funnel' | 'FUNNEL');
SEGMENT: ('segment' | 'SEGMENT');
STEP: ('step' | 'STEP');
TRANSACTION: ('transaction' | 'TRANSACTION');
CONVERSION: ('conversion' | 'CONVERSION');
TIMING: ('timing' | 'TIMING');
ON: ('on' | 'ON');
VIEW: ('view' | 'VIEW');
INCLUDE: ('include' | 'INCLUDE');
EXCLUDE: ('exclude' | 'EXCLUDE');
WHERE: ('where' | 'WHERE');
WHEN: ('when' | 'WHEN');
PRESAMPLE: ('presample' | 'PRESAMPLE');
POSTSAMPLE: ('postsample' | 'POSTSAMPLE');
SELECT: ('select' | 'SELECT');
FROM: ('from' | 'FROM');
BESIDE: ('beside' | 'BESIDE');
SCHEMA: ('schema' | 'SCHEMA');

//////////////////////////////////////////////////////////////////
// Data types
//////////////////////////////////////////////////////////////////
BOOLEAN_TYPE: ('boolean' | 'BOOLEAN');
BYTE_TYPE: ('byte' | 'BYTE');
SHORT_TYPE: ('short' | 'SHORT');
INTEGER_TYPE: ('integer' | 'INTEGER');
LONG_TYPE: ('long' | 'LONG');
DOUBLE_TYPE: ('double' | 'DOUBLE');
STRING_TYPE: ('string' | 'STRING');

//////////////////////////////////////////////////////////////////
//
//////////////////////////////////////////////////////////////////
COUNT: ('count' | 'COUNT');
SUM: ('sum' | 'SUM');
MIN: ('min' | 'MIN');
MAX: ('max' | 'MAX');
UNIQUE: ('unique' | 'UNIQUE' | 'uniques' | 'UNIQUES' );
TOP: ('top' | 'TOP');

//////////////////////////////////////////////////////////////////
// MISC OPERATORS
//////////////////////////////////////////////////////////////////
ABORT: ('abort' | 'ABORT');
AS: ('AS' | 'as');
OF: ('OF' | 'of');
OR: ('||' | 'or' | 'OR');
AND: ('&&' | 'and' | 'AND');
IN: ('IN' | 'in');
NOT: ('NOT' | 'not');
BETWEEN: ('BETWEEN' | 'between');
CAST: ('CAST' | 'cast');
IS: ('IS' | 'is');
NULL: ('NULL' | 'null');
AFTER: ('after' | 'AFTER');
WITHIN: ('within' | 'WITHIN');
END: ('end' | 'END');
ALL: ('all' | 'ALL');
FIRST: ('first' | 'FIRST');
START: ('start' | 'START');
LIMIT: ('limit' | 'LIMIT');

//////////////////////////////////////////////////////////////////
//
//////////////////////////////////////////////////////////////////
SCOPE: ('SCOPE' | 'scope');
ROLLING: ('ROLLING' | 'rolling');

//////////////////////////////////////////////////////////////////
// returns ordinals based on calendar offsets for given epoch time
//////////////////////////////////////////////////////////////////
SECONDOFMINUTE: ('SECONDOFMINUTE' | 'secondofminute');
MINUTEOFHOUR: ('MINUTEOFHOUR' | 'minuteofhour');
HOUROFDAY: ('HOUROFDAY' | 'hourofday');
DAYOFWEEK: ('DAYOFWEEK' | 'dayofweek');
DAYOFMONTH: ('DAYOFMONTH' | 'dayofmonth');
DAYOFYEAR: ('DAYOFYEAR' | 'dayofyear');
WEEKOFYEAR: ('WEEKOFYEAR' | 'weekofyear');
MONTHOFYEAR: ('MONTHOFYEAR' | 'monthofyear');
THEYEAR: ('THEYEAR' | 'theyear');

//////////////////////////////////////////////////////////////////
// return epoch times truncated to time quantum granularity
//////////////////////////////////////////////////////////////////
YEAR: ('YEAR' | 'year');
HALF: ('HALF' | 'half');
QUARTER: ('QUARTER' | 'quarter');
MONTH: ('MONTH' | 'month');
WEEK: ('WEEK' | 'week');
DAY: ('DAY' | 'day');
HOUR: ('HOUR' | 'hour');
MINUTE: ('MINUTE' | 'minute');
SECOND: ('SECOND' | 'second');

//////////////////////////////////////////////////////////////////
//
//////////////////////////////////////////////////////////////////
WEEKDURATION: ('WEEKDURATION' | 'weekduration');
DAYDURATION: ('DAYDURATION' | 'dayduration');
HOURDURATION: ('HOURDURATION' | 'hourduration');
MINUTEDURATION: ('MINUTEDURATION' | 'minuteduration');
SECONDDURATION: ('SECONDDURATION' | 'secondduration');

//////////////////////////////////////////////////////////////////
//
//////////////////////////////////////////////////////////////////
WEEKS: ('WEEKS' | 'weeks');
DAYS: ('DAYS' | 'days');
HOURS: ('HOURS' | 'hours');
MINUTES: ('MINUTES' | 'minutes');
SECONDS: ('SECONDS' | 'seconds');

//////////////////////////////////////////////////////////////////
// Special Time Constants
//////////////////////////////////////////////////////////////////
NOW: ('NOW' | 'now');

//////////////////////////////////////////////////////////////////
// Comparison Operators
//////////////////////////////////////////////////////////////////
EQ  : '==';
NEQ : '<>' | '!=';
LT  : '<';
LTE : '<=';
GT  : '>';
GTE : '>=';

//////////////////////////////////////////////////////////////////
//  Math operators
//////////////////////////////////////////////////////////////////
PLUS: '+';
MINUS: '-';
ASTERISK: '*';
SLASH: '/';
PERCENT: '%';
QUESTION: '?';

//////////////////////////////////////////////////////////////////
//
//////////////////////////////////////////////////////////////////
BOOLEAN_LITERAL
    : ('TRUE' | 'true')
    | ('FALSE' | 'false')
    ;

STRING_LITERAL :
    ('"' ( ~'"' | '""' | '\\"' )* '"')
    |  ('\'' ( ~'\'' | '\'\'' | '\\\'' )* '\'')
    ;

HEX_LITERAL
    :   '0' [xX] HexDigits
    ;

LONG_LITERAL
    : DIGIT+ 'L'
    ;

INTEGER_LITERAL
    : DIGIT+
    ;

DOUBLE_LITERAL
    : DIGIT+ '.' DIGIT*
    | '.' DIGIT+
    | DIGIT+ ('.' DIGIT*)? EXPONENT
    | '.' DIGIT+ EXPONENT
    ;

IDENTIFIER
    : (LETTER | '_' | UNICODE) (LETTER | UNICODE | DIGIT | '_')*
    ;

DIGIT_IDENTIFIER
    : DIGIT (LETTER | DIGIT | '_')+
    ;

fragment EXPONENT
    : 'E' [+-]? DIGIT+
    ;

fragment DIGIT
    : [0-9]
    ;

fragment LETTER
    : [A-Z]
    | [a-z]
    ;

fragment UNICODE
    : '\u0080'..'\uFFFE'
    ;

fragment
HexDigits
    :   HexDigit (HexDigitOrUnderscore* HexDigit)?
    ;

fragment
HexDigit
    :   [0-9a-fA-F]
    ;

fragment
HexDigitOrUnderscore
    :   HexDigit
    |   '_'
    ;

//////////////////////////////////////////////////////////////////
// White Space
//////////////////////////////////////////////////////////////////
SIMPLE_COMMENT
    : '--' ~[\r\n]* '\r'? '\n'? -> channel(HIDDEN)
    ;

LINE_COMMENT :   '//' ~[\r\n]* -> channel(HIDDEN)
    ;

BRACKETED_COMMENT
    : '/*' .*? '*/' -> channel(HIDDEN)
    ;

WS
    : [ \r\n\t\u000C]+ -> channel(HIDDEN)
    ;

COMMENT :   '/*' .*? '*/' -> channel(HIDDEN) ;


UNRECOGNIZED
    : .
    ;
