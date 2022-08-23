![Burst](../../../documentation/burst_h_small.png "") ![](../../doc/hydra_small.png "")
--
![](parameters.png "")
--


###### Grammar
    parameterDeclaration:
        pathExpression COLON valueTypeDeclaration 
            (ASSIGN (expression | expression) )?
     ;
    

###### Examples

     p1:boolean = true,
     p2:long = 1,
     p3:map[string, string] = map("hello" -> "goodbye", "red" → "blue"),
     p4:array[double] = array(1.0, 2.3),
     p5:string = null




---
------ [UP](../readme.md) ---  [HOME](../../readme.md) --------------------------------------------
