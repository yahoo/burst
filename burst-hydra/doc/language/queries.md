![Burst](../../../documentation/burst_h_small.png "") ![](../../doc/hydra_small.png "")
--
     
![](queries.png "")
--

###### Grammar
    queryDeclaration:
        QUERY identifier LB
            (variableDeclaration |  tabletDeclaration | cubeDeclaration | routeDeclaration)*
            visitDeclaration*
        RB
        ;

###### Examples
    query myQuery1 { 
         // myQuery1 collector clauses...
         // myQuery1 visit clauses...
    }


---
------ [UP](../readme.md) ---  [HOME](../../readme.md) --------------------------------------------
