![Burst](../../documentation/burst_h_small.png "")

_Motif:_ ```'recurring salient thematic element...'```

# Motif Operators

## Basic Operators
Basic operators in Motif expressions have precedence in order according to the rules below from 
highest to lowest.

|  semantic |  operator | alternatives |example |associativity |
|---|---|---|---|---|
| ___unary positive___ | + | | +( 3- 4) | left to right |
| ___unary negative___ | - |  |  -(3 * 4)| left to right |
| ___multiply___ |	* |  |  | left to right |
|___divide___ | / | | |   left to right |
| ___modulo___ | % | |  |  left to right |
|___add___ | 	+ | |  |  left to right |
|___subtract___ | - | |  |  left to right |
|___less than___ | < | |  |  left to right |
|___greater than___ | &gt; | |  |  left to right |
|___less than or equal___ | <= | |  |  left to right |
|___greater than or equal___ | &gt;= | |  |  left to right |
|___equal to___ | == | |  |  left to right |
|___not equal to___ | != | <> |  |  left to right |
|___null test___ | IS NULL | is null |  |  left to right |
|___membership test___ | IN | in |  |  left to right |
|___bounds check___ | BETWEEN | between |  |  left to right |
|___boolean inversion___ | NOT | not |  |  left to right |
|___boolean conjunction___ | AND | and, && |  |  left to right |
|___boolean disjunction___ | OR   |or, &#124;&#124; |  TRUE OR FALSE|  left to right |


## Time Operators
Below are a series of time conversion/bucketing operators. These are all equal
precedence.

|  semantic |  operator |
|---|---|
| ___HOUROFDAY___ |  _convert an epoch to an hour ordinal within a day_|
| ___DAYOFWEEK___ |  _convert an epoch to a day ordinal within a week_|
| ___DAYOFMONTH___ |  _convert an epoch to a day ordinal within a month_|
| ___DAYOFYEAR___ |  _convert an epoch to a day ordinal within a year_|
| ___WEEKOFYEAR___ |  _convert an epoch to a week ordinal within a year_|
| ___MONTHOFYEAR___ | _convert an epoch to a month ordinal within a year_ |
| ___YEAR___ | _truncate an epoch to a year quantum_  |
| ___HALF___ |  _truncate an epoch to a half year quantum_|
| ___QUARTER___ |  _truncate an epoch to a quarter year quantum_|
| ___MONTH___ |  _truncate an epoch to a month quantum_|
| ___WEEK___ |  _truncate an epoch to a week quantum_|
| ___DAY___ |  _truncate an epoch to a day quantum_|
| ___HOUR___ |  _truncate an epoch to an hour quantum_|
| ___SECOND___ |  _truncate an epoch to a second quantum_|
