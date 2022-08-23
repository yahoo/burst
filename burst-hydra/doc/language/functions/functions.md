![Burst](../../../../documentation/burst_h_small.png "") ![](../../hydra_small.png "")
--

![](functions.png "")
--

___Functions___ are the way that Hydra exposes various and sundry semantics outside of 
standard expressions.

###### Grammar

    functionExpression: functionType LP  (expression (SEP expression)* )? RP;
    
    functionType:
        calendarGrainType
        | DatetimeOrdinalType
        | datetimeGrainType
        | groupingFunctions
        | nowTime
        | brioFunctions
        | cubeFunctions
        | xactionFunctions
        ;

    nowTime: NOW ;


###### Examples


---
------ [UP](../readme.md) ---  [HOME](../../../readme.md) --------------------------------------------
