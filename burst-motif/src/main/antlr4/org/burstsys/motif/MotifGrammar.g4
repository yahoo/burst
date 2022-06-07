
//////////////////////////////////////////////////////////////////
// Subpart of Motif that provides non EQL filter syntax
//////////////////////////////////////////////////////////////////
grammar MotifGrammar;

import Tokens, EqlExpressionGrammar, MotifCommonGrammar, ViewGrammar, Funnels, Segments, TypeGrammar;

tokens {
    DELIMITER
}

motifStatements:
    ( motifStatement ';'? )+ EOF
;

motifStatement:
    view |
    motifSegment |
    motifFunnel |
    eqlQuery
    ;

eqlQuery:
    eqlParallelQuery ( BESIDE eqlParallelQuery )* (FROM sourceList (WHERE where=booleanExpression)?)+
    limitDeclaration?
     ;

eqlParallelQuery:
    SELECT rangeArguments? ( AS identifier ) ? targetList (WHERE where=booleanExpression)? limitDeclaration?
    ;

targetList:
    targetItem ( ',' targetItem )*
    ;

targetItem:
    ( aggregateTarget | dimensionTarget | expression) (AS identifier)?
    ;

aggregateTarget
    : aggregateTargetFunction '(' target=expression ')'
    ;

dimensionTarget
    : dimensionExpression
    ;

aggregateTargetFunction : TOP '[' limit=constant ']' | aggregateFunction;


