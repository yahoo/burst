![Burst](../../../documentation/burst_h_small.png "") ![](../../doc/hydra_small.png "")
--

![](matches.png "")
--

___Matches___ are pattern matching type flow control constructs with special features that
allow them to fit into the overall Hydra processing model such as special handling for Nulls.

### Matching semantics
Unlike the full featured Scala version of matches, matches in Hydra are limited to 
simple  equality comparison of a source value expression to a target value expressions. Like
the rest of Hydra, _nulls_ are fully supported. Simple deterministic unambiguous
type conversions are supported.

###### Grammar
    matchExpression:
        pathExpression MATCH LB
            matchCase*
            matchDefault?
        RB ;
    
    matchDefault:
        CASE USC lambda
            expressionBlock
        ;
    
    matchCase:
        CASE expression lambda
            expressionBlock
        ;


###### Examples

    user.sessions.startTime match {
      case "hello" ⇒ {
        ... // expression block
      }
      case 4 + 5 ⇒ {
        ... // expression block
      }
      case null ⇒ {
        ... // expression block
      }
      case _ ⇒ {
        ... // expression block
      }
    }



---
------ [UP](../readme.md) ---  [HOME](../../readme.md) --------------------------------------------
