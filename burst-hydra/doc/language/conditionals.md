![Burst](../../../doc/burst_small.png "") ![](../hydra_small.png "")
--

![](conditionals.png "")
--

___Conditionals___ are if, then, else type flow control constructs with special features that
allow them to fit into the overall Hydra processing model such as special handling for Nulls.

###### Grammar

    conditionalExpression:
        IF ifConditionTest
            ifExpressionBlock
        (ELSE IF elseIfConditionTest
            elseIfExpressionBlock
        )*
        (ELSE
            elseExpressionBlock
        )?
        (ELSENULL
            elseNullExpressionBlock
        )?
        ;
    
    ifConditionTest: conditionTest ;
    
    ifExpressionBlock: expressionBlock ;
    
    elseIfConditionTest: conditionTest ;
    
    elseIfExpressionBlock: expressionBlock ;
    
    elseExpressionBlock: expressionBlock ;
    
    elseNullExpressionBlock: expressionBlock ;
    
    conditionTest: LP expression RP  ;


###### Examples
    if(user.sessions.id == 345) {
      ... // expression block
    } else if (user.sessions.id == 1238 | 127) {
      ... // expression block
    } else if (user.sessions.event.startTime > 1239873456) {
      ... // expression block
    } else {
      ... // expression block
    }
    
    // conditional expressions return a value
    val conditional_result:short = 
        if(user.sessions.id == 1987345) 45 else 46 elseIfNull -1


---
------ [UP](../readme.md) ---  [HOME](../../readme.md) --------------------------------------------
