![Burst](../../doc/burst_small.png "")
--

![](../doc/eql.png "")


___EQL___ is a high level query language for exploring data in the BURST system.

A more formal definition of EQL is [here](./readme.md)

You can play along by starting a stand-alone burst instance as 
described [here](./doc/playing.md)


## Analytics
### Simple Counts and Aggregates

        select count(user.sessions) as "sessions", 
               day(user.sessions.startTime) as "day"
        from schema unity
        where user.sessions.startTime > NOW - DAYS(90)
        limit 50

### Simple restriction

### Dimensioned Aggregates

### Funnels

### Segments

### Parallel Queries

------ [HOME](../readme.md) --------------------------------------------

