# Synthetic Unity Configuration

The if the following properties are found in the stream properties then the value from the stream will be used instead
of the default value listed below.

| property | type | description | default value |
| -------- | ---- | ----------- | ------------- |
| `synthetic.unity.applicationId` | `Long` | the user.applicationId value | `12345` |
| `synthetic.unity.installDateStart` | `String` | the install date of the earliest user | `2022-01-01T08:45:00Z` |
| `synthetic.unity.installDateInterval` | `Duration` | the amount of time the install date is offset for each user | `1 day` |
| `synthetic.unity.deviceModelIds` | `Array[Long]` | a list of device model ids | `555666`, `666777`, `888999` |
| `synthetic.unity.appVersionIds` | `Array[Long]` | a list of app version ids | `101010101`, `12121212`, `13131313`, `1414141414`, `1515151515`, `1616161616` |
| `synthetic.unity.osVersionIds` | `Array[Long]` | a list of os version ids | `232323`, `454545`, `676767`, `898989` |
| `synthetic.unity.localeCountryIds` | `Array[Long]` | a list of locale country ids | `10000`, `20000`, `30000`, `40000`, `50000`, `60000` |
| `synthetic.unity.languageIds` | `Array[Long]` | a list of language ids | `111222`, `333444`, `555666`, `777888`, `888999` |
| `synthetic.unity.channelIds` | `Array[Long]` | a list of channel ids | `22`, `33`, `44` |
| `synthetic.unity.sessionCount` | `Int` | the number of sessions to generate for each user | `10` |
| `synthetic.unity.sessionInterval` | `Duration` | the amount of time between each of a user's sessions | `1 day` |
| `synthetic.unity.sessionDuration` | `Duration` | how long a session lasts | `10 minutes` |
| `synthetic.unity.providedOrigins` | `Array[String]` | the origin names | `"origin1", "origin2", "origin3", "origin4", "origin5"` |
| `synthetic.unity.mappedOrigins` | `Array[Long]` | the origin ids | `9876`, `54321`, `54329` |
| `synthetic.unity.originSourceIds` | `Array[Long]` | the origin source ids | `987`, `986`, `985`, `984`, `983` |
| `synthetic.unity.originMethodIds` | `Array[Long]` | the origin method ids | `12`, `13`, `14`, `15`, `16`, `17`, `18`, `19L` |
| `synthetic.unity.crashPercent` | `Int` | the percentage of sessions that should be marked as crashed should be in the range 0 to 100 inclusive | `10` |
| `synthetic.unity.sessionParameterCount` | `Int` | the number of parameters in the session | `7` |
| `synthetic.unity.sessionParametersPerSession` | `Array[Int]` | the number of parameters to for each event | `1`, `2`, `3`, `4`, `5`, `6`, `7` |
| `synthetic.unity.eventCount` | `Int` | the number of events in the session | `10` |
| `synthetic.unity.eventIds` | `Array[Long]` | the event ids | `1`, `2`, `3`, `4`, `5`, `6`, `7`, `8`, `9`, `10` |
| `synthetic.unity.eventInterval` | `Duration` | the interval between events in a session | `3 minutes` |
| `synthetic.unity.eventDuration` | `Duration` | the duration of events in the session | `1 minute` |
| `synthetic.unity.eventParametersCount` | `Int` | the number of event parameters that exist | `5` |
| `synthetic.unity.eventParametersPerEvent` | `Array[Int]` | the number of parameters to for each event | `1`, `2`, `3`, `4`, `5` |
