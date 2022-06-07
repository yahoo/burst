
//////////////////////////////////////////////////////////////////
// Subpart of Motif that provides non EQL filter syntax
//////////////////////////////////////////////////////////////////
grammar ViewGrammar;

import Tokens, EqlExpressionGrammar;

view:
    VIEW name=identifier '{' filterClause '}'
    ;

filterClause
    : (ruleClause (';')?)+
    ;

ruleClause
    : INCLUDE target=path (WHERE where=booleanExpression)?        #editRuleExpression
    ;

