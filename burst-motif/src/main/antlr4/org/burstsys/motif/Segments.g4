
//////////////////////////////////////////////////////////////////
// Subpart of Motif that provides segment definitions
//////////////////////////////////////////////////////////////////
grammar Segments;

import Tokens, EqlExpressionGrammar, MotifCommonGrammar ;

motifSegment:
    SEGMENT name=identifier rangeArguments? '{' (motifSegmentDefinition ';'?)+ '}' FROM sourceList
    ;

motifSegmentDefinition:
    SEGMENT name=(LONG_LITERAL | INTEGER_LITERAL) WHEN where=expression
    ;

