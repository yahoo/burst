![Burst](../../../../../../../../../documentation/burst_h_small.png "")
![](../../../../../../../../doc/felt_small.png "")


# Felt Expressions

 This package contains the types that support the construction of expressions within the Felt analysis tree.
 Expressions generally are  used to initialize state and to execute behavior statements within visit actions.

## expression as functions
 Expressions can be thought of as a function with a signature consisting of a range and a domain. The domain
 or input consists of zero or more other expressions. The range or output consists of zero or one value.

## complex expressions
 Since expressions can have other expressions as their domain, and in turn have their range become part of the domain
 of another expression, we have what is called a complex expression. The composition of expressions with other
 expressions is a central part of expression code generation.

## expression statements
 A simple or complex expression can be placed into a Felt tree in such a way that it becomes a statement that
 can be executed. For instance in a [Felt Visit](../visits/decl/FeltVisitDecl.scala) (within a
 [[org.burstsys.felt.model.visits.decl.FeltVisitDecl]] there is an [[FeltExprBlock]] that is a
 sequence of one or more statements that are executed (in order) at a certain point in the scan/traversal.
 The difference between an expression and a statement is that an expression has to be positioned into a
 statement context in order to have a place and time to have its semantics 'executed'.
 ==value and nullity
 Because both the Brio data model that Felt relied on, and Felt itself is quite strict
 in their consistent model for (relentless tracking of) nullity i.e. the known or unknown
 condition of any value, all expressions and their domain and their range always include these
 two somewhat separate concerns: the value and the nullity of that value i.e. is that value known or unknown. This
 complicates the way that functions are composed in the code generated runtime.

## literals
 There is a special type of expression called a literal. These constant values have special properties e.g.
 any complex expression that can be reduced to a literal can be simplified at compile/code generation time.

## value and side effects
 Expressions can be used to operate on their domain and produce a range that is some reasoning on their domain and/or
 they can produce 'side effects' i.e. the execution of the function will have some impact on the semantics of the
  [Analysis](../analysis/readme.md) that is not represented in the range e.g. functions can directly effect the state of
 [Collector](../collectors/readme.md) instances. Functions can also return values or have
 side effects that directly read state that is outside the domain e.g. again functions can read  the
 state of [[org.burstsys.felt.model.collectors.runtime.FeltCollector]] instances and use that state to impact
 the semantics of that function. Generally we don't worry a lot about the difference between pure state functions
 and side effect functions but its worth noting them because they may or may not impact optimization techniques
 for code generation.

## expression types
1. **function calls** ` func(domain) -> range `
 there are many functions built into Felt that can be executed by a call
1. **assignment**  `expr1 = expr2`  Some expressions allow for assignment
 from another expression's range. This assignment operation
 is syntactic sugar for the using that expression in a specialized form of domain
1. **boolean**  `expr -> boolean`  a category of expressions that have a true or false range
1. cast  cast(type1) as type2  a category of expression that takes a value
 of one type in the domain and has a range
 consisting of the same value as another type. 
1. **compare** `cmp(value1, value2) -> boolean`  a category of boolean expression that takes values
 in its domain and produces a true or false (or null)
 return that is some sort of comparison of those input values.
1. **flow**  `flow(boolean_expr...) -> set(statement) `
 a category of operation with very specialized syntax and grammar, that controls what expression statements
 are executed at runtime based on boolean domain inputs.
1. **inclusion**  `inclusion_test(value_expr) -> boolean` 
1. **math**   `value OP value -> value  OP value -> value` 
1. **time and date**  
