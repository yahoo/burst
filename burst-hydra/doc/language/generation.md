![Burst](../../../doc/burst_small.png "") ![](../hydra_small.png "")
--


# Code Generation
--

###### Expression -> Closure

Felt generates the minimal amount of facade on top of normal Scala and our internal APIs to effect
the required runtime scan semantics. This includes all the 


    { // caller
        var cl_1_value:integer = 0
        var cl_1_isNull:boolean = true
        { // callee
            cl_1_value = ???
            cl_1_isNull = false
        
        }
    }

---
------ [UP](../readme.md) ---  [HOME](../../readme.md) --------------------------------------------
