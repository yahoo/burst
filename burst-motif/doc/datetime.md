![Burst](../../doc/burst_small.png "")

_Motif:_ ```'recurring salient thematic element...'```

# DateTime Expressions
    valueExpression
        ...
        | datetimeConversionOperators '(' valueExpression ')'  
        | datetimeOrdinalOperators '(' valueExpression ')'      
        | datetimeQuantumOperators '(' valueExpression ')'      
        ...
        ;
These are specialized value expressions for manipulating date/time values.

### DateTime Ordinals
    valueExpression
        ...
        | datetimeOrdinalOperators '(' valueExpression ')'      
        ...
        ;
        
    datetimeOrdinalOperators : HOUROFDAY | DAYOFWEEK | DAYOFMONTH | DAYOFYEAR | WEEKOFYEAR | MONTHOFYEAR ;

These expressions take a long/datetime value and return a truncated/quantized version at some
date/time resolution.

| operator | semantic | ordinal range |
|---|---|---|
| __HOUROFDAY__ | _hours of day_ | 0-23?? |
| __DAYOFWEEK__ | _day of week_ | 1-7?? |
| __DAYOFMONTH__ | _day of month_ | 1- |
| __DAYOFYEAR__ | _day of year_ | 1-365???|
| __WEEKOFYEAR__ | _week of year_ | 1-54???|
| __MONTHOFYEAR__ | _month of year_ | 1-12|

### DateTime Quantization
    valueExpression
        ...
        | datetimeQuantumOperators '(' valueExpression ')'      
        ...
        ;
        
    datetimeQuantumOperators :  YEAR | HALF | QUARTER | MONTH | WEEK | DAY | HOUR | SECOND ;

These expressions take a long/datetime value and return a truncated/quantized version at some
date/time resolution.

| operator | semantic | 
|---|---|
| __YEAR__ | _truncate to year quantum_ |  
| __HALF__ | _truncate to half quantum_ |  
| __QUARTER__ | _truncate to quarter quantum_ |  
| __MONTH__ | _truncate to month quantum_ |  
| __WEEK__ | _truncate to week quantum_ |  
| __DAY__ | _truncate to day quantum_ |  
| __HOUR__ | _truncate to hour quantum_ |  
| __SECOND__ | _truncate to second quantum_ | 

### DateTime Conversions
    valueExpression
        ...
        | datetimeConversionOperators '(' valueExpression ')'  
        ...            
        ;

    datetimeConversionOperators : SECONDS | MINUTES | HOURS | DAYS | WEEKS | MONTHS | YEARS ;
Take a value expression in some time quantum and convert it to an equivalent number
of ticks (milliseconds)

| operator | semantic | 
|---|---|
| __YEARS__ | _convert a year number to equivalent ms ticks_ |  
| __MONTHS__ | _convert a months number to equivalent ms ticks__ |  
| __WEEKS__ | _convert a weeks number to equivalent ms ticks__ |  
| __DAYS__ | _convert a days number to equivalent ms ticks__ |  
| __HOURS__ | _convert an hours number to equivalent ms ticks__ |  
| __MINUTES__ | _convert a year number to equivalent ms ticks__ |  
| __SECONDS__ | _convert a seconds number to equivalent ms ticks__ | 
