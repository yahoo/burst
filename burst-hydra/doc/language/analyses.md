![Burst](../../../documentation/burst_h_small.png "") ![](../../doc/hydra_small.png "")
--
![](analyses.png "")
--

The ___Analysis___ clause is the root clause of a Hydra specification. All Hydra 
 [queries](queries.md) are executed
within the language and execution scope of a single Analysis. Think of an Analysis 
as a set of related metrics executed against a single datasource
 that are logically combined into a single set of measurements/dimensionings.
 
An Analysis clause is a single language translation unit and has special processing throughout
the system. The Analysis is parsed and processed on the master for validation purposes and
then sent in a _normalized_ form to the workers. This normalized form is further processed
on the workers, where it is _code-generated_ into Scala and _instantiated_ as a Scala single 
class. This class is then _cached_ on the workers where it can be re-used repeatedly without
redundant code-generation and instantiation and can benefit from long term JIT optimizations.

Since an Analysis is _parameterizable_, this cached Scala class can be used across many different
datasources, different computation inputs, different filtering, dimensioning, time zones etc.

This combination of features allows for metric analytics applications that benefit from complex
suites of queries such as _dashboards_ especially where there is heavy concurrency or great complexity.


##### Root Clause
An Analysis clause contains:
* a __name__ `identifier` e.g. _myAnalysis_ which is visible during execution in logs etc
* a set of optional
[parameters](parameters.md)
* a schema binding
* optional global [variable](variables.md) declarations
* optional [method](method.md) declarations
* zero or more [queries](queries.md) to be run in parallel during the analysis sweep/scan. 


###### Grammar
    analysisDeclaration:
        HYDRA identifier LP (parameterDeclaration (SEP parameterDeclaration)*)? RP LB
            SCHEMA pathExpression
            variableDeclaration*
            queryDeclaration*
            methodDeclaration*
        RB
        EOF ;

###### Examples
    hydra myAnalysis (myParameter1, myParameter2) { 
         schema 'mySchema'
         // one or more query clauses...
    }


---
------ [UP](../readme.md) ---  [HOME](../../readme.md) --------------------------------------------
