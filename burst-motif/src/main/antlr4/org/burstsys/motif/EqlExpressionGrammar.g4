
//////////////////////////////////////////////////////////////////
// Subpart of Motif that matches EQL expression syntax/semantics
//////////////////////////////////////////////////////////////////

grammar EqlExpressionGrammar;

import Tokens;

expression
    : booleanExpression                                                             #genericBooleanExpression
    ;

booleanExpression
    : left=valueExpression LT right=valueExpression                             #ltCompareBooleanExpression
    | left=valueExpression GT right=valueExpression                             #gtCompareBooleanExpression
    | left=valueExpression LTE right=valueExpression                            #lteCompareBooleanExpression
    | left=valueExpression GTE right=valueExpression                            #gteCompareBooleanExpression
    | left=valueExpression EQ right=valueExpression                             #eqCompareBooleanExpression
    | left=valueExpression NEQ right=valueExpression                            #neqCompareBooleanExpression
    | valueExpression nullTestOp                                                #nullTestBooleanExpression
    | left=valueExpression boundsTestOp lower=valueExpression AND upper=valueExpression     #boundsTestBooleanExpression
    | left=valueExpression membershipTestOp  valueExpressionList                #explicitMembershipTestBooleanExpression
    | left=valueExpression membershipTestOp path                                #vectorMembershipTestBooleanExpression
    | NOT subexpression=booleanExpression                                       #unaryBooleanExpression
    | booleanExpression AND booleanExpression                                   #binaryBooleanExpression
    | booleanExpression OR booleanExpression                                    #binaryBooleanExpression
    | valueExpression                                                           #upwardValueExpression
    ;

valueExpressionList
    : '(' valueExpression (',' valueExpression)* ')'
    ;


nullTestOp : IS NOT NULL | IS NULL ;

membershipTestOp : NOT IN | IN ;

boundsTestOp : NOT BETWEEN | BETWEEN ;

valueExpression
    : PLUS valueExpression                                  #unaryPlusValueExpression
    | MINUS valueExpression                                 #unaryMinusExpression
    | left=valueExpression ASTERISK right=valueExpression   #multiplyValueExpression
    | left=valueExpression SLASH right=valueExpression      #divideValueExpression
    | left=valueExpression PERCENT right=valueExpression    #moduloValueExpression
    | left=valueExpression PLUS right=valueExpression       #addValueExpression
    | left=valueExpression MINUS right=valueExpression      #subtractValueExpression
    | CAST '(' valueExpression AS dataType ')'              #castValueExpression
    | aggregateExpression                                   #aggregateValueExpression
    | '(' child=expression ')'                              #parenthesizedValueExpression
    | dimensionExpression                                   #dimensionValueExpression
    | functionExpression                                    #functionalExpression
    | constant                                              #constantValueExpression
    | path                                                  #pathValueExpression
    | parameter                                             #parameterAccessor
    | NOW                                                   #nowValueExpression
    ;

aggregateExpression
    : aggregateFunction '(' target=expression ')' #basicAggregateFunctionExpression
    | '(' aggregateFunction '(' target=expression ')'
    (SCOPE (ROLLING quanta=expression)? scope=expression)?
    (WHERE where=expression)? ')' #aggregateFunctionExpression
    ;

dimensionExpression
    : datetimeConversionOperators '(' valueExpression ')'   #datetimeConversionValueExpression
    | datetimeOrdinalOperators '(' valueExpression timeZoneArgument? ')'      #datetimeOrdinalValueExpression
    | datetimeQuantumOperators '(' valueExpression  timeZoneArgument? ')'      #datetimeQuantumValueExpression
    ;

functionExpression
    : identifier valueExpressionList
    ;

timeZoneArgument
    : ',' valueExpression
    ;

dataType : BOOLEAN_TYPE | BYTE_TYPE | SHORT_TYPE | INTEGER_TYPE | LONG_TYPE | DOUBLE_TYPE | STRING_TYPE ;

aggregateFunction : COUNT | SUM | MIN | MAX | UNIQUE ;

path : identifier ('.' identifier)* mapKey? ;

parameter:  '$' identifier;

mapKey :'[' valueExpression ']' ;

constant
    : STRING_LITERAL        #stringLiteral
    | BOOLEAN_LITERAL       #booleanLiteral
    | INTEGER_LITERAL       #integerLiteral
    | DOUBLE_LITERAL        #doubleLiteral
    | NULL                  #nullLiteral
    | LONG_LITERAL          #longLiteral
    | HEX_LITERAL           #hexLiteral
    ;

// Operations that take a long value and emit the number of millis for the specified quanta
// "Where are the MONTHS and YEARS operators?" you ask?  You can't know the exact number of ticks in
// a month or year without specifying a point in time
datetimeConversionOperators : SECONDS | MINUTES | HOURS | DAYS | WEEKS ;

// Opration take a millis-long and returns a long value referencing the calendar
datetimeOrdinalOperators : SECONDOFMINUTE | MINUTEOFHOUR | HOUROFDAY | DAYOFWEEK | DAYOFMONTH | DAYOFYEAR | WEEKOFYEAR | MONTHOFYEAR | THEYEAR;

// Operations take a millis-long and rounds millis to the boundary
datetimeQuantumOperators :  YEAR | HALF | QUARTER | MONTH | WEEK | DAY | HOUR | MINUTE | SECOND ;

