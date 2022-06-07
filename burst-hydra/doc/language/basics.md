![Burst](../doc/burst_small.png "") ![](../../doc/hydra_small.png "")
--
![](basics.png "")
--
The Hydra language basics should be mostly familiar to a Java or Scala programmer.
There are important changed and missing semantics but since ultimately
Hydra is code-generated into Scala differences are fairly easy to identify and understand.

* [White Space](#white-space)
* [Punctuation](#punctuation)
* [Identifiers](#identifiers)

###   White Space
Pretty much the same as Java/Scala.
###### Grammar
    WS  :  [ \t\r\n\u000C]+  ;
    
    COMMENT :   '/*' .*? '*/'  ;
    
    LINE_COMMENT :   '//' ~[\r\n]*   ;

###### Examples
    /* regular comment */
    /*
     * another regular comment
     */
     // line comment
     /////////////////////////// another line comment //////////////

### Punctuation
Again should be very familiar to Scala initiates.
###### Grammar
    UNIMPLEMENTED:  '???' ;
    LP:   '(' ;
    RP:   ')' ;
    SEP:   ',' ;
    COLON:   ':' ;
    LB:   '{' ;
    RB:   '}' ;
    LSB:   '[' ;
    RSB:   ']' ;
    LAMBDA: [\u21d2] | '=>' ;
    ARROW_ASSOC: [\u2192] | '->' ;
Note that we accept unicode characters for `LAMBDA` and `ARROW_ASSOC` just like Scala.


###### Examples
    () {
        []
        =>
        ->
    }

### Identifiers
Identifiers are compatible with Scala and Java equivalents.

###### Grammar
    identifier : IDENTIFIER | stringLiteral ;
    
    IDENTIFIER : (LETTER | '_') (LETTER | DIGIT | '_')* ;

###### Examples
    identifier_34_1 // identifier 
    'foo'           // simple string literal
    'schema'        // string literal used to create identifier matching keyword

---
------ [UP](../readme.md) ---  [HOME](../../readme.md) --------------------------------------------
