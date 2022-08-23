![Burst](../../documentation/burst_h_small.png "")

_Motif:_ ```'recurring salient thematic element...'```

# Views
This subsystem is the support library for a view definition language library, meant to provide a simple
universal common filter-query semantic across various subsystems. The output of this module is a single jar
meant to be imported into other subsystems to support authoring, validation, and error checking of view filters
as well as a pre processor creating an intermediate model form that can be input to various and sundry back ends
performing direct execution of queries or translation to other query specificaiton model.

* [View Antlr Grammar](../src/main/antlr4/org/burstsys/motif/viewGrammar.g4)

# View Filter Rule Semantics
A view specification consists of a unique name and a set of ordered rules that describe when to include or exclude 
elements of the processed object-tree. While a Motif view does not specify a back end client model, or even how 
its output is utilized,  it is designed to support and be consistent with an efficient and simple to 
implement two-pass evaluate/transform execution engine at least conceptually. This will be described a 
bit more later. Note all collections in the object-tree are ordered/sorted, generally causally or by time, as specified by 
the Motif schema. This is important for the correctness and efficiency of the two pass algorithm for 
various aggregation and causal reasoning filter semantics.

#### View
A View is named and consistes of a collection of filters. 
A view is created based on a view specification in the Motify view language. This view is returned by
the view parser and is available to back ends to use as it sees fit. Generally some sort of plan is created 
that is an interpretation of each of the indivual view filters as appropriate for for some back end evaluation/execution engine..

#### Filter
A Filter is created based on a filter specification in the Motify view language. This filter is returned by
the view parser and is available to back ends to use as it sees fit. Generally some sort of plan is created 
that is an interpretation of the filter as appropriate for for some back end evaluation/execution engine..

#### Rule
Each Rule is defined as a tuple `R<M, T, E>` where `M` is the _mode_ of the Rule, `T` is the _target_ of the rule, and `E` is 
the _expression_ associated with the rule.

#### Mode
The mode of a rule is either ___include___  or ___sample___. An include is where the Rule target is to be retained in the
 edit transformation. A sample rule is to remove elements following a sampling formula

#### Target
The target of the rule is in the form of a schema Path. e.g. `user.sessions.events`. This is element in the object-tree
where the rule is in force.  There can at most one rule per unique target path in a view.

#### Where Clause Expression
The expression is a boolean expression in the __EQL__ subset predicate language. If the expression returns true during
 a traversal of the object-tree, then the corresponding Rule becomes active in terms of including or sample 
the Rule’s target object-tree element.

# View Language
The Motif view language is very simple, most of the complexity being in the boolean expression ___where___ clause.

### View 
    view:  'view' name '{' filter '}' EOF
 A Motif view consists of a name and __filter__. This is the root or top level semantic.
    
### Filter Clause
    filter:  rule+
 A Motif view filter consists of one or more __rule__ clauses.
    
#### Rule Clause
    rule:  ruleType targetPath 'where' booleanExpression
There is more than one `ruleType` but they all have a `targetPath` and a boolean `where` clause expression.

##### Rule Types
    ruleType: 'include' | ( 'sample' '(' expression ')' ) ;
There are two types of rules, the `include`  __edit__ rules, and the `sample` rule.
        
##### Target Path
    targetPath: path // semantic rule to restrict to 'instance' references and at most one rule per unique path
    
A target path refers to the point in the schema defined object-tree where the rule is take force.
 The path must refer to an 'instance' i.e. a structure in the Motif Schema parlance. There is at most one rule per
 unique path in the view.
