![Burst](../doc/burst_small.png "") ![](../../doc/hydra_small.png "")
--

![](methods.png "")
--

___Methods___ are methods much like standard Scala. They consist
of a name, zero or more parameters, return type, 
and an expression scope. They are designed to capture common
code within analysis instances for:
* Reduced Hydra source redundancy/complexity
* Increased JIT effectiveness
* Support for features where methods can be executed once and
the results shared across analysis queries.


###### Grammar
    methodDeclaration: DEF identifier
        LP (parameterDeclaration (SEP parameterDeclaration)*)? RP
        COLON valueTypeDeclaration ASSIGN
        expressionBlock ;
    

###### Examples

---
------ [UP](../readme.md) ---  [HOME](../../readme.md) --------------------------------------------
