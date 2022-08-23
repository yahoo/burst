![Burst](../../documentation/burst_h_small.png "") ![](./felt_small.png "")
--

# Code Generation
Felt is meant to support a _code generation_ model. This means that the Felt
semantic
model is inherently compatible with a subset of the Scala model and as
such can be compiled into equivalent Scala code by the Scala compiler
at runtime. Felt builds curated enhanced semantics on top of that subset
that allows these code generated closures to implement a flexible
extensible and high performance metrics analysis.

###### Expression -> Closures
Felt based software components are designed to emit scala code that is integrated into
the ultimate scan-sweep object. The output of these functions is a composition of 
what can be called Felt `closures`. A ___closure___ is a  code structure that can be used
to capture Felt expression trees.


Felt generates the minimal amount of facade on top of normal Scala and our internal APIs to effect
the required runtime closure semantics. This includes all the 


###### nulls
 Any null encountered anywhere in an expression subtree are propogated up to the top
expression tree node that _does not explicitly test for null_. This means that an _expression
that gets a_ __null__ _as part of its domain, always returns_ __null__ _for its range_.
There is a significant cost to this null semantic in the Felt code generation world i.e. we
must use non stack based return methods since in java/scala atomic primitives cannot be
null.

The basic structure for this is as follows:

    { // caller
        var cl_1_value:integer = 0      // 'receive' the callee value
        var cl_1_isNull:boolean = true  // 'receive' the callee value
        {   // callee
            var cl_2_value:integer = 0      // 'receive' the callee value
            var cl_2_isNull:boolean = true  // 'receive' the callee value
            {   // callee
                ... code that potentially generates side effects and returns value Or Null
                cl_1_value = ???        // 'return' the value to the caller
                cl_1_isNull = false     // 'return' the null status to the caller
            }
            cl_1_value = ???        // 'return' the value to the caller
            cl_1_isNull = false     // 'return' the null status to the caller
        }
    }

Note that this structure declares two variables one for the return value, the other for the
null test.

---
------ [UP](../readme.md) ---  [HOME](../../readme.md) --------------------------------------------
