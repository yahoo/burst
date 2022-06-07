![Burst](../../doc/burst_small.png "") 

_Motif:_ ```'recurring salient thematic element...'```

#Scoped Expressions

Scoping is used with aggregations in order to control when an aggregation is reset.  The most obvious and perhaps common
scope is on an object boundary.  For example, when counting the number of events in a session,  the scope is the session 
object:  when the session changes,  the aggregate must be reset.  In addition to the object boundary,  the aggregation can 
reset on some arbitrary value such as a date or country code.

By default aggregates have a single value that is reset on a scope boundary.  However,  it is possible to have a rolling 
aggregate that keeps a set number of previous values that are summed together.  The values are "rolling" because as a new
boundary value is calculated it is added as the newest value and the oldest is dropped.  A very common rolling boundary is
the last 30 days.  
        
### Object Boundary Scoped
Object boundary scopes are specified using a simple reference path to the object:

    expression :
        ...      
        | expression SCOPE referencePath  
        ...                                                            
        ;
        
An example of an object boundary is the count of events ever seen by a user:

    count(user.sessions.events) scope user
    
If the scope is not specified then the enclosing object of the aggregation target is assumed.  So while the count 
of events in a session the scope can be explicitly specified:

    count(user.sessions.events) scope user.sessions
    
The same scope would be inferred as the default in this example:

    count(user.sessions.events)

### Value Change Boundary Scoped
Value bounded scopes use value expressions for defining the boundary.

    expression :
        ...      
        | expression SCOPE valueExpression  
        ...                                                            
        ;
        
For example, in order to calculate the number events per day use a value scope with the ```Day``` function:

    count(user.sessions.events) scope DAY(user.sessions.events.startTime)
  
Another, non-date, example would be if each session has a country code then we could aggregate the ammount of time a user
spends in each country:

    sum(user.sessions.duration) scope user.sessions.countryId
        
### Window Scoped
Aggregations can be rolling.  A rolling aggregation is a sum over a sliding window of scoped sub-aggregations.

    expression :
        ...                                                              
        | expression SCOPE ROLLING quantumConstant sliceFunction '(' path ')' 
        ...                                                         
        ;
        
The canonical rolling aggregation example is a sliding window of 30 days. For example, the total number of ```123``` events 
for a user in a sliding 30 day window is:

    count(user.sessions.events) scope rolling 30 DAY(user.sessions.events.startTime) where user.sessions.events.eventId = 123
    
So at every change of day for the user, the aggregate is the total number of ```123``` events of the previous 30 days.  The 
aggregation is rolling because the aggreggation returned at day `n` analyzed events from days `n-29` to `n` while the aggregation
returned at day `n+1` analyzed events from days `n-28` to `n+1`:  the 30 day window dropped day `n-29` and added day `n+1` to 
calculate the new value.

    
