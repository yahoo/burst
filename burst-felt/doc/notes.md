![Burst](../../documentation/burst_h_small.png "") ![](./felt_small.png "")
--

# Felt/Hydra Extensions

* user defined functions `f(value expr parameter list) => (value or NULL)`
* visit entrance predicate ifFalse skip to next visit iteration
* visit exit predicate ifFalse discard all mutations that level and below
* reference vector member abort (all mutations that level and below are discarded)

#### User Defined Functions
Add the ability to define a function presumably at the global level and thuse
make it possible for the hydra author to factor out common semantics e.g. predicates such
as common filtering expressions. This would:
* reduce redundant source code _and_ generated code
* allow for more efficient JIT optimization
* potentially allow for other optimizations during hydra generation `e.g. exit and entrance conditions`

These functions would be as follows:
    
    def f(value_expr*) : value {
        ...
    }
   
Where the ___domain___ is zero or more value expressions and ___range___ is a single value expression. 
Each domain value expression can be ___null___ and the return can be null.

#### Entrance and Exit Conditions
These are defined within visits

###### entrance conditions
    pre => {
        entrance function(...) // user defined function that returns a boolean value
    }

###### exit conditions
    post => {
        exit function(...) // user defined function that returns a boolean value
    }

#### Aborts 
___Aborts___ clear out the current cube context and terminate the visit
 
#### Skips
___Skips___ terminate the visit without clearing the current cube contents



#### Master Query
TBD

---
------ [UP](../readme.md) ---  [HOME](../../readme.md) --------------------------------------------
