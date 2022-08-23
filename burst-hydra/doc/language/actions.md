![Burst](../../../documentation/burst_h_small.png "") ![](../../doc/hydra_small.png "")
--
     
![](actions.png "")
--

___Actions___ are `expression-block` instances that are executed as a phase
 within a [Visit](visits.md).

###### Types 
There is an `action-type` for each possible phase of action execution.

|  type | description |  valid for visits of relation-form |
|---:|---|:---|
|  __pre__ |  executed _before_ the children of a structure |  reference-scalar, reference-vector  |
|  __post__ |  executed _after_ the children of a structure |  reference-scalar, reference-vector  |
|  __situ__ |  executed _for each_ member of a value collection |  value-vector, value-map  |
|  __before__ |  executed _before all_ members of a collection |  reference-vector, value-vector, value-map  |
|  __after__ |  executed _after all_ members of a collection |  reference-vector, value-vector, value-map  |

###### Grammar
    actionType: (PRE | POST | SITU | BEFORE | AFTER) ;
    
    actionDeclaration: actionType lambda expressionBlock   ;

###### Examples
    pre => {
        // expression-block 
    }
    post => {
        // expression-block 
    }
    situ => {
        // expression-block 
    }
    before => {
        // expression-block  
    }
    after => {
        // expression-block  
    }

---
------ [UP](../readme.md) ---  [HOME](../../readme.md) --------------------------------------------
