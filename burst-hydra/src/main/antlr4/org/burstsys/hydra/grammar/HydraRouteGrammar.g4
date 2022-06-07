//////////////////////////////////////////////////////////////////
// Antlr Grammar for Hydra Routes
//////////////////////////////////////////////////////////////////

grammar HydraRouteGrammar;

import HydraLexerGrammar, HydraExpressionGrammar;

////////////////////////////////////////////////////////////////////////////////////////////////////////////
// routes - causal/temporal reasoning
////////////////////////////////////////////////////////////////////////////////////////////////////////////

// the top level 'route' declaration
routeDeclaration:
    ROUTE LB
        routeParameter*
        routeGraph?
    RB
    ;

// routes take parameters to fine tune...
routeParameter:
        (MAX_STEPS_PER_ROUTE ASSIGN expression)
    |   (MAX_PARTIAL_PATHS ASSIGN expression)
    |   (MAX_COMPLETE_PATHS ASSIGN expression)
    |   (MAX_PATH_TIME ASSIGN expression)
    ;

// central to routes is the 'graph' specification - literally a graph with cycles and stuff...
routeGraph:
    GRAPH LB
        routeStep*
    RB
    ;

// a single 'step' within a 'graph
routeStep:
    ( routeTrait (SEP routeTrait)* )?  fixedLiteral LB
        routeTo*
    RB ;

routeTrait:  BEGIN_TRAIT | END_TRAIT | ENTER_TRAIT | EXIT_TRAIT | TACIT_TRAIT | COMPLETE_TRAIT ;

// the 'edge' from 'step' to 'step' within a 'graph'
routeTo:
    TO LP expression (SEP expression (SEP expression )? )?  RP ;


