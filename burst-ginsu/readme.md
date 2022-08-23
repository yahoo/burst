![Burst](../documentation/burst_h_small.png "")
--

![](./doc/ginsu.png "")

___Ginsu___ is a generalized __FELT__ compatible metrics calculus library - a set of types and operations for the
time and space performant counting and grouping (_dice_ and _slice_).


# Calendar Grains
Given a LONG _epoch_ time value, __truncate__ (zero out) the lower order bits of
the value to return a coarser grain (lower resolution) LONG _epoch_ time value as
defined by the  [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601) standard.
Put another way place times into buckets defined by calendar quantums.
__NOTE:__ time grains are available both as as __FELT__ function call and as a __FELT__ dimensional semantic.

#### Second Grain
	secondGrain(epoch:long):long
slice/truncate a given __milliseconds__ epoch value into __calendar second__ granularity/resolution

#### Minute Grain
	minuteGrain(epoch:long):long
slice/truncate a given __milliseconds__ epoch value into __calendar minute__ granularity/resolution

#### Hour Grain
	hourGrain(epoch:long):long
slice/truncate a given __milliseconds__ epoch value into __calendar hour__ granularity/resolution

#### Day Grain
	dayGrain(epoch:long):long
slice/truncate a given __milliseconds__ epoch value into __calendar day__ granularity/resolution

#### Week Grain
	weekGrain(epoch:long):long
slice/truncate a given __milliseconds__ epoch value into __calendar week__ granularity/resolution

#### Month Grain
	monthGrain(epoch:long):long
slice/truncate a given __milliseconds__ epoch value into __calendar month__ granularity/resolution
calendar

#### Quarter Grain
	quarterGrain(epoch:long):long
slice/truncate a given __milliseconds__ epoch value into __calendar quarter__
(__Q1__, __Q2__, __Q3__, __Q4__)
granularity/resolution

#### Half Grain
	halfGrain(epoch:long):long
slice/truncate a given __milliseconds__ epoch value into __calendar halves__
(__H1__, __H2__) granularity/resolution

#### Year Grain
	yearGrain(epoch:long):long
slice/truncate a given __milliseconds__ epoch value into __calendar year__ granularity/resolution

# Calendar Ordinals
return the index (_ordinal_) of a calendar __quantum__ within a different coarser grained
calendar  __quantum__ for a given __milliseconds__ epoch value. Ordinals are based
on the  [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601) standard.

#### Second Of Minute
	secondOfMinuteOrdinal(epoch:long):long
return the index (_ordinal_) of a calendar __second__ into a calendar __minute__
for a given __milliseconds__ epoch value

#### Minute Of Hour
	minuteOfHourOrdinal(epoch:long):long
return the index (_ordinal_) of a calendar __minute__ into a calendar __hour__
for a given __milliseconds__ epoch value

#### Hour Of Day
	hourOfDayOrdinal(epoch:long):long
return the index (_ordinal_) of a calendar __hour__ into a calendar __day__
for a given __milliseconds__ epoch value

###### examples
	hourOfDayOrdinal( datetime("01:20") ) returns 1 (1AM)

#### Day Of Week
	dayOfWeekOrdinal(epoch:long):long
return the index (_ordinal_) of a calendar __day__ into a calendar __week__
for a given __milliseconds__ epoch value

###### examples
	dayOfWeekOrdinal( datetime("1-1-2018") ) returns 1 (Monday)

#### Day Of Month
	dayOfMonthOrdinal(epoch:long):long
return the index (_ordinal_) of a calendar __day__ into a calendar __month__
for a given __milliseconds__ epoch value

###### examples
	dayOfMonthOrdinal( datetime("1-1-2018") ) returns 1

#### Day Of Year
	dayOfYearOrdinal(epoch:long):long
return the index (_ordinal_) of a calendar __day__ into a calendar __year__
for a given __milliseconds__ epoch value

###### examples
	dayOfYearOrdinal( datetime("1-1-2018") ) returns 1

#### Month Of Year
	monthOfYearOrdinal(epoch:long):long`
return the index (_ordinal_) of a calendar __month__ into a calendar __year__
for a given __milliseconds__ epoch value.

###### examples
	monthOfYearOrdinal( datetime("1-1-2018") ) returns ???

#### Week Of Year
	weekOfYearOrdinal(epoch:long):long
return the index (_ordinal_) of a calendar __week__ into a calendar __year__
for a given __milliseconds__ epoch value. The value is an
[ISO week date](https://en.wikipedia.org/wiki/ISO_week_date).

###### examples
	weekOfYearOrdinal( datetime("1-1-2018") ) returns 1
__NOTE:__ Per ISO -- If January 1 is on a Monday, Tuesday, Wednesday or Thursday, it is in week 1.

#### Year Of Era
	yearOfEraOrdinal(epoch:long):long
return the index (_ordinal_) of a calendar __year__ into the __AD__ (_anno domini_) common era calendar
for a given __milliseconds__ epoch value. __NOTE:__ Per ISO -- year 0 is equal to 1 __B.C.__ and
all yearts after 0 are __A.D.__

###### examples
	yearOfEraOrdinal( datetime("1-1-2018") ) returns 2018


# Splits
	split(boundaries:byte*):byte
	split(boundaries:short*):short
	split(boundaries:integer*):integer
	split(boundaries:long*):long
	split(boundaries:double*):double
Slice a given numeric value and ___split___  or ___bucket___  the value
based on _boundary_ values provided to the split function.
Value are placed into buckets based on the implied ranges. Values below the splits are placed
in the first bucket, values above the splits are placed into the last bucket.
__NOTE:__ time grains are available both as as __FELT__ function call and as a __FELT__ dimensional semantic.

# Enums
	enum(matchList:byte*, value:byte):byte
	enum(matchList:short*, value:short):short
	enum(matchList:integer*, value:integer):integer
	enum(matchList:long*, value:long):long
	enum(matchList:double*, value:double):double
Slice a given numeric `value` int a series of buckets
as defined by a match to a provided set of values called a `matchList`.
The last value in the  `matchList` is considered a grab bag called
'other' and all values that do not match are placed there.
__NOTE:__ time grains are available both as as __FELT__ function call and as a __FELT__ dimensional semantic.

# String Coercion
	stringBooleanCoerce(representation: string): boolean
	stringByteCoerce(representation: string): byte
	stringShortCoerce(representation: string): short
	stringIntegerCoerce(representation: string): integer
	stringLongCoerce(representation: string): long
	stringLongDouble(representation: string): double
Convert a string representation of a numeric type into a number.
__NOTE:__ time grains are available both as as __FELT__ function call and as a __FELT__ dimensional semantic.

###### examples
	stringBooleanCoerce( "true" ) returns true
	stringByteCoerce( "0x45" ) returns 0x45
	stringShortCoerce( "21345" ) returns 21345
	stringIntegerCoerce( "34" ) returns 34
	stringLongCoerce( "12356987" ) returns 12356987
	stringLongDouble( "45.1" ) returns 45.1

# Tick Conversions
return the number of milliseconds for a given count of a
courser grained time quantum. Ticks are __not__ based
on / require any  [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601) calendar conversions.
They are mainly for convenience, code uniformity etc.  These are _not_ available as __Felt__ dimensions.

#### Second To Ms Ticks
	secondTicks(count:long):long
return __milliseconds__ for a `count` of __seconds__

###### examples
	secondTicks( 1 ) returns 1000

#### Minutes To Ms Ticks
	minuteTicks(count:long):long
return __milliseconds__ for a `count` of __minutes__

#### Hours To Ms Ticks
	hourTicks(count:long):long
return __milliseconds__ for a `count` of __hours__

#### Days To Ms Ticks
	dayTicks(count:long):long
return __milliseconds__ for a `count` of __days__

#### Weeks To Ms Ticks
	weekTicks(count:long):long
return __milliseconds__ for a `count` of __weeks__

# Time Durations
Given a number of __milliseconds__ return an associated number of courser grained time quantums.
Durations are __not__ based on / require
any  [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601) calendar conversions. They are mainly
for convenience, code uniformity etc. These are _not_ available as __Felt__ dimensions.

#### Second Duration
	secondDuration(ms:long):long
Given a number of __milliseconds__ return the associated number of __seconds__.

#### Minute Duration
	minuteDuration(ms:long):long
Given a number of __milliseconds__ return the associated number of __minutes__.

#### Hour Duration
	hourDuration(ms:long):long
Given a number of __milliseconds__ return the associated number of __hours__.

#### Day Duration
	dayDuration(ms:long):long
Given a number of __milliseconds__ return the associated number of __days__.

#### Week Duration
	weekDuration(ms:long):long
Given a number of __milliseconds__ return the associated number of __weeks__.

## Misc
#### `now():long `
Return the 'now' epoch time in MS


---
------ [HOME](../readme.md) --------------------------------------------
