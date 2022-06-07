//////////////////////////////////////////////////////////////////
// Antlr Grammar for Hydra Expressions
//////////////////////////////////////////////////////////////////

grammar HydraExpressionGrammar;

import HydraLexerGrammar, HydraFunctionGrammar;
////////////////////////////////////////////////////////////////////////////////////////////////////////////
// misc
////////////////////////////////////////////////////////////////////////////////////////////////////////////

// an identifier
identifier :  IDENTIFIER ;

// defines a expression block
lambda: LAMBDA ;

// defines a map association
association: ARROW_RIGHT_ASSOC;

//////////////////////////////////////////////////////////////////
// Literal
//////////////////////////////////////////////////////////////////

primitiveLiteral: fixedLiteral | floatLiteral | stringLiteral | booleanLiteral | nullLiteral ;
fixedLiteral: FIXED_LITERAL ;
booleanLiteral : BOOLEAN_LITERAL ;
stringLiteral : STRING_LITERAL ;
nullLiteral : NULL_LITERAL ;
floatLiteral: FLOAT_LITERAL ;

////////////////////////////////////////////////////////////////////////////////////////////////////////////
// paths
////////////////////////////////////////////////////////////////////////////////////////////////////////////

pathExpression : identifier ('.' identifier)* (LSB expression RSB)? ;

////////////////////////////////////////////////////////////////////////////////////////////////////////////
// functions - the built in 'verbs' of FELT/HYDRA
////////////////////////////////////////////////////////////////////////////////////////////////////////////

functionExpression: functionType LP  (expression (SEP expression)* )? RP;

////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Complex Value Literals
////////////////////////////////////////////////////////////////////////////////////////////////////////////

complexLiteral: setLiteral | arrayLiteral | mapLiteral ;

// an 'set' of values
setLiteral:
    SET
    LP
        ( expression (SEP expression)* )*
    RP
    ;

// an 'array' of values
arrayLiteral:
    ARRAY
    LP
        ( expression (SEP expression)* )*
    RP
    ;

// an unordered list of key -> value tuples
mapLiteral:
    MAP
    LP
        ( mapAssociation (SEP mapAssociation)* )*
    RP
    ;

// a key -> value tuple in a map literal
mapAssociation: expression ARROW_RIGHT_ASSOC expression ;

////////////////////////////////////////////////////////////////////////////////////////////////////////////
// if then else
////////////////////////////////////////////////////////////////////////////////////////////////////////////

// top level somewhat complicated if/else control flow construct
conditionalExpression:
    IF ifConditionTest
        ifExpressionBlock
    (ELSE IF elseIfConditionTest
        elseIfExpressionBlock
    )*
    (ELSE
        elseExpressionBlock
    )?
    (ELSENULL
        elseNullExpressionBlock
    )?
    ;

ifConditionTest: conditionTest ;

ifExpressionBlock: expressionBlock ;

elseIfConditionTest: conditionTest ;

elseIfExpressionBlock: expressionBlock ;

elseExpressionBlock: expressionBlock ;

elseNullExpressionBlock: expressionBlock ;

conditionTest: LP expression RP  ;

////////////////////////////////////////////////////////////////////////////////////////////////////////////
// match expression
////////////////////////////////////////////////////////////////////////////////////////////////////////////

// top level somewhat complicated pattern match control flow construct
matchExpression:
    pathExpression MATCH LB
        matchCase*
        matchDefault?
    RB ;

// the default pattern match statement in a match expression
matchDefault:
    CASE USC lambda
        expressionBlock
    ;

// a pattern match case statement in a match expression
matchCase:
    CASE expression lambda
        expressionBlock
    ;

////////////////////////////////////////////////////////////////////////////////////////////////////////////
// expression blocks
////////////////////////////////////////////////////////////////////////////////////////////////////////////

// a list of variables followed by an ordered list of expressions that are to be executed in sequence
expressionBlock:
    LB
        localVariableDeclaration*
        expression*
    RB
    ;

////////////////////////////////////////////////////////////////////////////////////////////////////////////
// assignmentExpression  / updateExpression
////////////////////////////////////////////////////////////////////////////////////////////////////////////

// a write operation that updates an artifact referenced by path to the value of an expression
assignmentExpression: pathExpression ASSIGN expression ;
updateExpression: pathExpression (PLUS_EQ | MINUS_EQ) expression ;

////////////////////////////////////////////////////////////////////////////////////////////////////////////
// inclusion expressions - boolean expressions that test membership of one set within another
////////////////////////////////////////////////////////////////////////////////////////////////////////////

// invertible operation that returns true or false if a right hand expression represents a set that can be tested for set inclusion
setRefInclusionExpression:
       pathExpression      IN      pathExpression                      #refSetInclusionClause
    |   pathExpression  NOT IN      pathExpression                      #invertedRefSetInclusionClause
    ;

////////////////////////////////////////////////////////////////////////////////////////////////////////////
// misc
////////////////////////////////////////////////////////////////////////////////////////////////////////////

// an expression that when executed returns an exception meaning an expression was not defined or control decisions not to be made
unImplementedExpression: UNIMPLEMENTED ;

// return from a 'method'
returnExpression: RETURN expression ;

// return an epoch LONG as defined by an ISO 8601 formatted datetime string
dateTimeExpression: DATETIME LP  STRING_LITERAL RP ;

// convert/coerce one type into another
castExpression: CAST LP expression AS valueTypeDeclaration RP ;

////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Parameters
////////////////////////////////////////////////////////////////////////////////////////////////////////////

// a parameter in the analysis 'signature'
parameterDeclaration:
    pathExpression COLON valueTypeDeclaration (ASSIGN expression )?
 ;

////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Variables
////////////////////////////////////////////////////////////////////////////////////////////////////////////

variableDeclaration:
    (VAL | VAR) pathExpression COLON valueTypeDeclaration (ASSIGN expression )?
 ;

localVariableDeclaration:
    variableDeclaration
    ;

globalVariableDeclaration:
    variableDeclaration
    ;

////////////////////////////////////////////////////////////////////////////////////////////////////////////
// types
////////////////////////////////////////////////////////////////////////////////////////////////////////////

valueArrayTypeDeclaration :
    ARRAY LSB
        valuePrimitiveTypeDeclaration
    RSB
    ;

valueSetTypeDeclaration :
    SET LSB
        valuePrimitiveTypeDeclaration
    RSB
    ;

valueMapTypeDeclaration :
    MAP LSB
        valuePrimitiveTypeDeclaration SEP valuePrimitiveTypeDeclaration
    RSB
    ;

valuePrimitiveTypeDeclaration:
    BOOLEAN_TYPE | DOUBLE_TYPE | BYTE_TYPE | SHORT_TYPE | INTEGER_TYPE | LONG_TYPE | STRING_TYPE
    ;

valueTypeDeclaration: valuePrimitiveTypeDeclaration  | valueArrayTypeDeclaration | valueSetTypeDeclaration | valueMapTypeDeclaration ;

////////////////////////////////////////////////////////////////////////////////////////////////////////////
// expressions
////////////////////////////////////////////////////////////////////////////////////////////////////////////

// the big expression taxonomy - it all comes together here...
expression:
    // composition
    LP expression RP                  #parenthesizedExpressionClause

    // literals
    | primitiveLiteral                  #primitiveLiteralExpressionClause
    | complexLiteral                    #complexLiteralExpressionClause

    // paths that become references
    | pathExpression                    #pathExpressionClause

    // function calls
    | functionExpression                #functionExpressionClause

    // cast
    | castExpression                    #castExpressionClause

    // ref in
    | setRefInclusionExpression          #setInclusionExpressionClause

    // posation/negation
    | PLUS expression                   #positiveUnaryExpressionClause
    | MINUS expression                  #negativeUnaryExpressionClause

    // mutiply/divide/modulo
    | expression ASTERISK expression    #multiplyBinaryExpressionClause
    | expression SLASH expression       #divideBinaryExpressionClause
    | expression PERCENT expression     #moduloBinaryExpressionClause

    // mutiply/divide/modulo
    | expression PLUS expression        #addBinaryExpressionClause
    | expression MINUS expression       #subtractBinaryExpressionClause

    // value ranges
    | expression LT expression          #lessThanComparisonBooleanExpressionClause
    | expression LTE expression         #lessThanOrEqualComparisonBooleanExpressionClause
    | expression GT expression          #greaterThanComparisonBooleanExpressionClause
    | expression GTE expression         #greaterThanOrEqualThanComparisonBooleanExpressionClause

    // value equality
    | expression EQ expression          #equalThanComparisonBooleanExpressionClause
    | expression NEQ expression         #notEqualThanComparisonBooleanExpressionClause

    // between
    | expression BETWEEN     LP (expression SEP expression) RP   #rangeInclusionClause
    | expression NOT  BETWEEN     LP (expression SEP expression) RP   #invertedRangeInclusionClause

    // in
    | expression IN  LP (expression (SEP expression)* )? RP  #inlineSetInclusionClause
    | expression NOT IN  LP (expression (SEP expression)* )? RP  #invertedInlineSetInclusionClause

    // boolean algebra
    | expression AND expression         #andBinaryBooleanExpressionClause
    | expression OR expression          #orBinaryBooleanExpressionClause
    | NOT expression                    #notUnaryBooleanExpressionClause

    // assignment
    | assignmentExpression              #assignmentExpressionClause
    | updateExpression                  #updateExpressionClause

    // control flow
    | matchExpression                   #matchExpressionClause
    | conditionalExpression             #conditionalExpressHydraMockDataionClause

    // range
    | returnExpression                  #returnExpressionClause

    | unImplementedExpression           #unimplementedExpressionClause

    | dateTimeExpression                #dateTimeExpressionClause
    ;

