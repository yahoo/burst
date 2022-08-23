![Burst](../../documentation/burst_h_small.png "")

_Motif:_ ```'recurring salient thematic element...'```
    
# Boolean Expressions
    
A boolean expression is one that returns a logical __true__ or __false__. There are only a few
operators on boolean expressions, __AND__, __OR__, and __NOT__.


## Boolean Constant
    booleanExpression
        ...
        | BOOLEAN_LITERAL                                  
        ...  
        ;

## Unary Boolean Expression
    booleanExpression
        ...
        | NOT subexpression=booleanExpression                                  
        ...  
        ;
        
## Binary Boolean Expression
    booleanExpression
        ...
        | booleanExpression AND booleanExpression                                   
        | booleanExpression OR booleanExpression                                   
        ...  
        ;
        
## Value Comparison Expression
    booleanExpression
        ...
        | left=valueExpression LT right=valueExpression                         
        | left=valueExpression GT right=valueExpression                          
        | left=valueExpression LTE right=valueExpression                         
        | left=valueExpression GTE right=valueExpression                          
        | left=valueExpression EQ right=valueExpression                          
        | left=valueExpression NEQ right=valueExpression       
        ...  
        ;


## Membership Test
    booleanExpression
        ...
        | left=valueExpression membershipTestOp '(' expression (',' expression)* ')'          
        | left=valueExpression membershipTestOp path                              
        ...  
        ;
#### Explicit Membership Test
#### Vector Membership Test


## Bounds Test
    booleanExpression
        ...
        | left=valueExpression boundsTestOp lower=valueExpression AND upper=valueExpression  
        ...  
        ;

## Null Test
    booleanExpression
        ...
        | valueExpression nullTestOp                                        
        ...  
        ;
