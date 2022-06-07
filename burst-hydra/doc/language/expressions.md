![Burst](../doc/burst_small.png "") ![](../../doc/hydra_small.png "")
--

![](expressions.png "")
--


Expressions are composable language trees that like a 
function takes zero or more input values i.e. the `domain` and returns a 
single value (or `null`) i.e. the `range`.  

###### Expression Types
* [Data Expressions](data.md)
* [Function Expressions](functions/functions.md)
* [Boolean Expressions](boolean.md)
* [Conditional Expressions](conditionals.md)
* [Match Expressions](matches.md)

###### Grammar
    expression
        // precedence
        : LP expression RP                  #parenthesizedExpressionClause
    
        // literals
        | primitiveLiteral                  #primitiveLiteralExpressionClause
        | complexLiteral                    #complexLiteralExpressionClause
    
        // paths that become references
        | pathExpression                    #pathExpressionClause
    
        // function calls
        | functionExpression                #functionExpressionClause
    
        // cast
        | CAST LP
            expression AS valueTypeDeclaration
            RP                             #castExpressionClause
    
        // posation/negation
        | PLUS expression                   #positiveUnaryExpressionClause
        | MINUS expression                  #negativeUnaryExpressionClause
    
        // mutiply/divide/modulo
        | expression ASTERISK expression    #multiplyBinaryExpressionClause
        | expression SLASH expression       #divideBinaryExpressionClause
        | expression PERCENT expression     #moduloBinaryExpressionClause
    
        // mutiply/divide/modulo
        | expression PLUS expression        #addBinaryExpressionClause
        | expression MINUS expression       #subtractBinaryExpressionClause
    
        // value ranges
        | expression LT expression          #lessThanComparisonBooleanExpressionClause
        | expression LTE expression         #lessThanOrEqualComparisonBooleanExpressionClause
        | expression GT expression          #greaterThanComparisonBooleanExpressionClause
        | expression GTE expression         #greaterThanOrEqualThanComparisonBooleanExpressionClause
    
        // value equality
        | expression EQ expression          #equalThanComparisonBooleanExpressionClause
        | expression NEQ expression         #notEqualThanComparisonBooleanExpressionClause
    
        // boolean algebra
        | expression AND expression         #andBinaryBooleanExpressionClause
        | expression OR expression          #orBinaryBooleanExpressionClause
        | NOT expression                    #notUnaryBooleanExpressionClause
    
        // assignment
        | assignmentExpression              #assignmentExpressionClause
    
        // control flow
        | matchExpression                   #matchExpressionClause
        | conditionalExpression             #conditionalExpressHydraMockDataionClause
    
        // range
        | returnExpression                  #returnExpressionClause
    
        | unImplementedExpression           #unimplementedExpressionClause
        ;

###### Examples
    (3 + 4) * 3
    
    cube.dim1 = if(true) {
        user.sessions.id + 77
    } else { -1 }
    
    user.sessions.id match {
        case 3456 => {
            return true
        }
        case _ => {
            return false
        }
    }
        
#### Expression Blocks
All expressions exist within what is called an `expression block`. This consists of an
optional set of variable declarations followed by zero or more expressions. Blocks
are used throughout the Hydra grammar e.g. `visits`, `conditionals`, `matches`. 
Expression blocks are _name spaces_, enforcing visibility and exposure of names e.g. 
variables.

###### child blocks
You can insert a new `child block` as a purely lexical sub scope where needed or desired.

    { // parent clock
      val someVariable:integer = 33
      
       somevariable = { // child block
            // this someVariable is different from parent one
            val someVariable:integer = 55
            someVariable + 1
       }
       someVariable
    }
    
the return of the parent block would be `56`.


###### return type
All expression blocks have the type of either the last expression in the sequence
of expressions and the type of any `return` statements. These must agree or be
_coercible_ to be equivalent.

###### grammar
    expressionBlock:
        LB
            variableDeclaration*
            expression*
        RB
        ;

###### Examples

    {
      val foo:integer = 33
      cube.dimension1 = foo + user.sessions.id + 34
    }


---
------ [UP](../readme.md) ---  [HOME](../../readme.md) --------------------------------------------
