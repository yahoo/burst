//////////////////////////////////////////////////////////////////
// Antlr Grammar for Hydra Analysis Top Level Clause
//////////////////////////////////////////////////////////////////

grammar HydraAnalysisGrammar;

import HydraLexerGrammar, HydraExpressionGrammar, HydraCubeGrammar, HydraRouteGrammar, HydraTabletGrammar, HydraShrubGrammar;

////////////////////////////////////////////////////////////////////////////////////////////////////////////
// analysis - top level declaration in a hydra translation unit
// an analysis consists of multiple named parallel queries all executed within a single analytic scan
////////////////////////////////////////////////////////////////////////////////////////////////////////////

analysisDeclaration:
    HYDRA identifier LP (parameterDeclaration (SEP parameterDeclaration)*)? RP LB
        schemaDeclaration
        analysisProperty*
        globalVariableDeclaration*
        frameDeclaration*
        methodDeclaration*
    RB
    EOF ;

analysisProperty:
    TZ ASSIGN expression
    ;

////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Schema Declaration
////////////////////////////////////////////////////////////////////////////////////////////////////////////

schemaDeclaration: SCHEMA pathExpression ( LB schemaExtension* RB )? ;

schemaExtension: pathExpression ARROW_LEFT_ASSOC pathExpression ;

////////////////////////////////////////////////////////////////////////////////////////////////////////////
// frames
// an analysis is one or more frames executed in parallel
////////////////////////////////////////////////////////////////////////////////////////////////////////////

frameDeclaration:
    FRAME identifier LB
        frameProperty*
        globalVariableDeclaration*
        ( tabletDeclaration | cubeDeclaration | routeDeclaration | shrubDeclaration)
        (staticVisitDeclaration | dynamicVisitDeclaration)*
    RB
    ;

frameProperty:
    TZ ASSIGN expression
    ;

////////////////////////////////////////////////////////////////////////////////////////////////////////////
// analysis `methods` (currently not implemented)
////////////////////////////////////////////////////////////////////////////////////////////////////////////

methodDeclaration:
    DEF identifier LP (parameterDeclaration (SEP parameterDeclaration)*)? RP COLON valueTypeDeclaration ASSIGN
    expressionBlock
    ;

////////////////////////////////////////////////////////////////////////////////////////////////////////////
// visits - actions to be executed within a specific query at a specified relation in the object tree traversal
////////////////////////////////////////////////////////////////////////////////////////////////////////////

staticVisitDeclaration:
    pathExpression (LP expression RP)? lambda LB
        globalVariableDeclaration*
        actionDeclaration*
    RB ;

dynamicVisitDeclaration:
    pathExpression pathExpression  (LP expression RP)? lambda LB
        globalVariableDeclaration*
        actionDeclaration*
    RB ;

// the type of action i.e. where in the depth first search to execute an action
actionType: (PRE | POST | SITU | BEFORE | AFTER) ;

// the action declaration within a visit
actionDeclaration: actionType lambda expressionBlock   ;

