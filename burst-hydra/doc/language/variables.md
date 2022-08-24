![Burst](../../../doc/burst_small.png "") ![](../hydra_small.png "")
--
![](variables.png "")
--

Variables are simple registers for Brio values (_value scalars, value vectors, value maps_). They
can be initialized to any expression as well as set to `null`. They can be an immutable `val` or
mutable `var` style. 

__QUESTION__: how can value vectors and value maps be mutated?

###### Analysis Scope Variables:

    hydra theAnalysis () {
      schema unity
    
      val global_variable1:double = 1.0 + 2.0
    
      query mock {
        ...


These variables are visible anywhere within the `analysis`. They are initialized at
the beginning of the analysis.
    
###### Query Scope Variables

      query mock {
    
        val query_variable1:boolean = true
    
        cube user {
            ...


These variables are visible anywhere within an individual `query`. They are initialized at
the beginning of the query.
    
###### Visit Scope Variables

    user ⇒ {
    
      var visit_variable1:string = "hello"
      
      pre ⇒ {
        ...


These variables are visible anywhere within an individual `visit`. They are initialized at
the beginning of __each scalar or vector member iteration__ in the visit.
    
###### Action Scope Variables
          pre ⇒ {
          
            val action_variable1:byte = 0x5
            
            ...

These variables are visible anywhere within an individual `action`. They are initialized at
the beginning of the action. 
    
###### Expression Block Scope Variables

        if(user.sessions.id != null) {
        
            val block_variable1:long = 10000222
            
        }


These variables are visible anywhere within an individual `expression block`. They are initialized at
the beginning of the block.

---
------ [UP](../readme.md) ---  [HOME](../../readme.md) --------------------------------------------
