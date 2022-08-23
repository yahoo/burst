![Burst](../../../documentation/burst_h_small.png "") ![](../../doc/hydra_small.png "")
--

![](../../../burst-zap/doc/routes.png "")
--

see [Zap Routes](../burst-zap/doc/routes.md)

###### Grammar
    routeDeclaration:
        ROUTE identifier COLON path LB
            routeParameter*
            routeGraph?
        RB
        ;
    
    routeParameter: (MAXSTEPS ASSIGN valueExpression) | (MAXPATHS ASSIGN valueExpression) ;
    
    routeGraph:
        GRAPH LB
            routeStep*
        RB
        ;
    
    routeStep:
        routeStepType? COLON fixedLiteral LB
            routeTo*
        RB ;
    
    routeTo:
        TO LP valueExpression (SEP valueExpression (SEP valueExpression )? )?  RP ;
    
    routeStepType: ENTRANCE | EXIT | STEP;
    

###### Examples
---
------ [UP](../readme.md) ---  [HOME](../../readme.md) --------------------------------------------
