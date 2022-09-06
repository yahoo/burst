![Burst](../../../documentation/burst_h_small.png "") ![](../../doc/hydra_small.png "")
--
     
![](visits.png "")
--

__Visits__ are a construct used to specify one or more [Actions](actions.md) to be executed
when the scan/traversal _visits_ a particular _relation_ within the _object-tree_. You can optionally
specify visit [variables](variables.md) that have a lifetime/visibility scope limited to a
 single Visit.

###### Grammar
    visitDeclaration:
        pathExpression lambda LB
            variableDeclaration*
            actionDeclaration*
        RB ;

###### Examples

    user => {
    
      val visit_variable:boolean = true
      
      pre => {
      
      } // end pre action
      
    } // end user visit


---
------ [UP](../readme.md) ---  [HOME](../../readme.md) --------------------------------------------
