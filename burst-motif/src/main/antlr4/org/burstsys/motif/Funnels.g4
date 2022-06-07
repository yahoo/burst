
//////////////////////////////////////////////////////////////////
// Subpart of Motif that provides segment definitions
//////////////////////////////////////////////////////////////////
grammar Funnels;

import Tokens, EqlExpressionGrammar, MotifCommonGrammar;

motifFunnel:
    FUNNEL name=identifier rangeArguments? (TRANSACTION | CONVERSION) (WITHIN within=valueExpression)? limitDeclaration? tagList?
        '{' (stepDefinition ';'?)+ funnelDefinition '}' FROM sourceList
    ;

stepDefinition:
    STEP id=stepId WHEN when=stepWhen FIRST?
       (TIMING ON timing=valueExpression)? (AFTER after=valueExpression)? (WITHIN within=valueExpression)?
    ;

stepWhen:
    expression |
    (START | END) (OF boundary=path)?
    ;

funnelDefinition:
    '(' nonCapture? funnelDefinition ')'            #parensFunnelDefinition |
    '[' negating? stepId ( stepId )* ']'            #bracketFunnelDefinition |
    funnelDefinition (PLUS | ASTERISK | QUESTION | '{' min=(INTEGER_LITERAL | ASTERISK) ',' max=(INTEGER_LITERAL | ASTERISK)'}') #repeatFunnelDefinition |
    funnelDefinition ( ':' funnelDefinition )+      #andListFunnelDefinition |
    funnelDefinition ( '|' funnelDefinition )+      #orListFunnelDefinition |
    stepId                                          #stepIdFunnelDefinition
    ;

negating:
    '^'
    ;

nonCapture:
    '?' ':'
    ;


stepId:
    (LONG_LITERAL | INTEGER_LITERAL);

