![Burst](../../../../../../../../../../../doc/burst_small.png "")
![](../../../../../../../../../../doc/felt_small.png "")

![](route.png)

# Routes

Routes are a [Collector](../../collectors/readme.md) used to perform _temporal/causal reasoning_ as part
of a _behavioral analysis_. Routes consist of a [Graph](#Route-Graph)
and a [Journal](#Route-Journal).
The graph is
metadata that defines all possible sequences of step to step transitions
based on runtime step assertions,
the journal is data that records each successful step transition. A number of
questions can be asked of the route state via a set of [route functions](#Route-Functions)
and its journal and the contents
can be [visited](../../visits/readme.md) by attaching the route to a a dynamic (schema extended) path.

### route semantics
The general idea behind routes is that they allow the analysis creator to define a complex
model of possible `sequences of steps` that define a set of interesting
`causal/temporal behavioral signals`
in the form of a _directed graph_.  These sequences are `paths` through the graph. The
graph is transformed into a state machine that the defines  a runtime `decision tree` of step to
step transitions as defined by the specified graph edges between specified graph step nodes.

#### transition assertions
As we perform the depth first scan/traversal of a Brio data entity, somewhere
in a visit a set of Felt/Hydra predicates (expressions that returns a boolean) are evaluated
and as a result attempt to assert step transition where there exists an edge from the current step/state
of the route to the step where the tested assertion wants to go.
This step to step deliberative movement is then recorded at runtime in a
[Journal](#Route-Journal)  which captures
all those interested step sequences as what is now a setof
causal/temporal behavioral signals that have been discovered in the real world and can not be used
to influence further reasoning.

#### time ordered traversals
It's important to introduce something from the Brio modeling world for behavioral entities i.e.
that Brio object trees generally have their collections sorted by some causal or temporal gradient
i.e. there is some monotonically increasing (or decreasing) value that drives the order and timing
of how data is discovered during a scan. Most commonly this is a 'time value' that collections
within the Brio data are sorted by during pressing (encoding). This is a critical element of
route reasoning (and many other salient aspects of Felt analysis trees that discover data or truth).


### Route declaration

    frame <frame_name> {
        ...
        route {
            <route_parameters>
            ...
            graph {  
                ...
            }
        }
    }

The Route declaration is _hosted_ in a suitable
[Frame](../../../../felt/model/frame/readme.md) with a suitable
`<frame_name>`. Like all other
collectors there is a `one-to-one` relationship between the
Collector and the hosting Frame.
The name of the Route is
the same as the name of the Frame.


### Route Graph
        graph {  
            1 { to(2)   }   // node with outgoing edge (no traits)
            2 {         }   // node with no outgoing edge(s) (no traits)
        }
The graph is a declaration that sets up a directed graph
model  for the route. The graph defines a set of
possible conforming step sequences ([Paths](#Graph-Paths)).
A Graph consists of one or more nodes ([Steps](#Graph-Nodes))
each with zero or more edges ([Tos](#Graph-Edges)). Additionally, each
Step can have zero or more ([Traits](#Graph-Traits)) which augment/modify
the semantics for a particular step.

###### Example

##### Graph Nodes
The **Step** construct defines a `node` in the **Graph**.
The `<step_key>` is a fixed positive integer.

            <step_key> { 
                ... 
            } 

### Graph Edges
The **To** construct defines an `edge` between nodes in the Graph definition.
There are zero or more edges in a given Step construct. Each represents
a possible _transition_ from that step to a different step.

                to( <step_key> ) 

### Graph Paths


### Graph Traits
The **Trait** construct defines a additional semantic for the associated graph node.

        <trait1>, <trait2>, ... 1 { 
            to(2) 
        }

###### enter trait
For path through a graph to get started, it must transition to a step key
marked by an `enter` trait.
enter 1 {
to(2)
}

**Q:** does an `enter` end any pre-existing path? it must...

###### exit trait
        exit 3 { 
        }
###### complete trait
        complete 3 { 
        }

###### tacit trait
     ...
      tacit 2 { ...  }
    ...
The **Tacit** trait is used to prevent the Journal recording of a particular
**Step**. This allows the route to move through graph paths without
wasting space or time recording redundant or non-essential data.

####### tacit example
    begin 1 { to(2) }
    tacit 2 { to(3) } // do not record this step in the journal
    exit 3 { }

If a transition from step key 1 to step key 2 is successful,
the Route will be IN step key 2 but not RECORD step key 2.

#### Route Parameters

###### max complete paths
        maxCompletePaths = <fixed_number>

###### max partial paths
        maxPartialPaths = <fixed_number>

###### max path time
        maxPathTime = <fixed_number>
A per route specified maximum allowed time (_timeout in milliseconds_).

**Semantic rules**:
1. IF time from the start of any
   path to any given current step in that path is exceeded then
   the timeout is invoked.
1. IF  current step:
    1. IS without `exit` or `complete` trait THEN  the path is marked as partial
    1. IS `exit` trait: THEN the path is marked as complete
    1. IS `complete` trait: THEN the path is marked as complete
1. After this timeout, the FSM is set to state '_not in any path_'

###### max steps in route
        maxSteps = <fixed_number>

### Route Journal

### Journal Recording

### Journal Visits

## Route Functions
Routes have a set of built-in functions that can be used to update or access current state. These functions
fall into three categories: [Common](#Common-Functions), [FSM](#FSM-Functions),
[Scope](#Scope-Functions), and [Visit](#Visit-Functions).

### Common Functions

###### routeCompletePaths
    routeCompletePaths( )

###### routeLastPathIsComplete
    routeLastPathIsComplete( )

###### routeLastPathOrdinal
    routeLastPathOrdinal( )

###### routeLastPathIsComplete
    routeLastPathIsComplete( )

###### routeLastPathOrdinal
    routeLastPathOrdinal( )

###### routeLastStepKey
    routeLastStepKey( )

###### routeLastStepTag
    routeLastStepTag( )

###### routeLastStepTime
    routeLastStepTime( )

### FSM Functions

###### routeFsmAssertStep
    routeFsmAssertStep( )

###### routeFsmAssertTime
    routeFsmAssertTime( )

###### routeFsmEndPath
    routeFsmEndPath( )

###### routeFsmInPath
    routeFsmInPath( )

###### routeFsmInStep
    routeFsmInStep( )

###### routeFsmIsEmpty
    routeFsmIsEmpty( )

### Scope Functions

###### routeScopeStart
    routeScopeStart( )

###### routeScopeAbort
    routeScopeAbort( )

###### routeScopeCommit
    routeScopeCommit( )

###### routeScopeCurrentPath
    routeScopeCurrentPath( )

###### routeScopeCurrentStep
    routeScopeCurrentStep( )

###### routeScopePathChanged
    routeScopePathChanged( )

###### routeScopePriorPath
    routeScopePriorPath( )

###### routeScopePriorStep
    routeScopePriorStep( )

### Visit Functions

###### routeVisitPathIsComplete
    routeVisitPathIsComplete( ) -> boolean

###### routeVisitPathIsFirst
    routeVisitPathIsFirst( ) -> boolean

###### routeVisitPathIsLast
    routeVisitPathIsLast( ) -> boolean

###### routeVisitPathOrdinal
    routeVisitPathOrdinal( )

###### routeVisitStepIsFirst
    routeVisitStepIsFirst( ) -> boolean

###### routeVisitStepIsLast
    routeVisitStepIsLast( ) -> boolean

###### routeVisitStepIsLastInPath
    routeVisitStepIsLastInPath( ) -> boolean

###### routeVisitStepKey
    routeVisitStepKey( )

###### routeVisitStepOrdinal
    routeVisitStepOrdinal( )

###### routeVisitStepTag
    routeVisitStepTag( )

###### routeVisitStepTime
    routeVisitStepTime( ) -> long



## Results
