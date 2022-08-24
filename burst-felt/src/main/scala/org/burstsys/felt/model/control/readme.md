![Burst](../../../../../../../../../doc/burst_small.png "")
![Felt](../../../../../../../../doc/felt_small.png "")

# Control Verbs
Control verbs provide _control_ of Brio subtree data collection
via [Collector](../collectors/readme.md) instances during a traversal/scan. 
By invoking a control verb
function in an expression, the behavior of the scan mechanics and how
collectors are managed to control collector data semantics 
as well as in some cases performance optimizations. 

The basic idea is that as depth first traversals of the object tree are performed,
the single pass nature of these scans sometimes causes the algorithms
to collect data based on analysis predicates that later 
turn out to be false.  These false predictions then mean data may have been
collected that should not have been, and any future collection of data
based on that false prediction should be ignored moving forward. These
`aggregate functions` are necessary to implement single pass scans. You 
allow an optimistic assumption of the function's positive outcome in
order to get the data in case you need it, but rely on control verbs
 to `abort` that subset of the data if the assumption proves false.

## supported collectors
Right now only [Cube Collectors](../collectors/cube/readme.md) are supported by 
control verbs. It
is likely we will want to extend that support
to  [Route Collectors](../collectors/route/readme.md) 
and  [Tablet Collectors](../collectors/tablet/readme.md) 
and others. Note that support for variables
including mutables makes some sense but is not currently planned.

## verb types
There are four types of control verbs:
- `AbortMember`
- `AbortRelation`
- `CommitMember`
- `CommitRelation`

Each is a combination of [data semantics](data semantics) 
and [attachment semantics](attachment semantics).

## data semantics
Control verbs are either an abort or a commit. The former throws away all data already collected in the current attachment point and
subtree below AND also will stop collecting more information (and hopefully stop processing in general)
in current attachment point and subtree below
 * **Abort Semantics:** for the rest of the processing at the attachment point and below, throw away all data and stop
processing/collecting more. This operation has a strict semantic behavior of causing data collected
in subtrees to no longer impact results. It also has a significant performance impact as some subsequent part of the data
processing no longer needs to be performed.
* **Commit Semantics:** for the rest of the processing at the attachment point and below, keep all current data but stop
processing/collecting more.  This operation has a strict semantic behavior of causing data collected
in subtrees to still impact results. It also has a significant performance</b impact as some subsequent part of the data
processing no longer needs to be performed.
  
## attachment semantics 
A control verb is attached to a certain point in the data schema which then also attaches it to a point
in the traversal of the tree. Note that you can attach a control verb at location at or above yourself in the
traversal. It is not allowed to attach it below your current traversal point.

 * **Member Attachment:** all processing at the given member within a relation (and subtrees below) are impacted by the control verb actions.
This is for a single member in a relation e.g. For a scalar relation, the Member is the same as the Relation.
 * **Relation Attachment:** all processing at the given relation (and subtrees below) are impacted by the control verb actions. A relation
would be all the members in a path i.e. for a scalar reference its the single item, for vectors its all the members.

## toggle semantics 
When a control verb is invoked, it is a one-shot toggled state changed. This means that when
the current relation or member is aborted
or committed, once that relation or member goes out of scope in the traversal, the control state is reset.
