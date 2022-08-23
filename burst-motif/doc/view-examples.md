![Burst](../../documentation/burst_h_small.png "")

# Motif Views

## Generic Filter Queries

###### Case G1
    VIEW v { INCLUDE user WHERE user.flurryId == '773839303435746B6A6873646662796162636465' }
A view that includes a single user, with a given flurry ID.

###### Case G2
    VIEW v { INCLUDE user WHERE user.project.installTime BETWEEN 1483228800000 AND CAST('2017-02-15' AS DATETIME) }
A view that includes all users with an install time between two epoch dates (`Sun, 01 Jan 2017 00:00:00 GMT` and `2017-02-15`).

###### Case G3
    VIEW v { INCLUDE user WHERE
         (COUNT(user.sessions.events)
            WHERE user.sessions.events.eventId == 9873456) > 10 }
A view that includes users who had more than a given number of a `user.sessions.events.eventId` across all sessions.

###### Case G4
    VIEW v { INCLUDE user WHERE
        (COUNT(user.sessions.events) SCOPE user.sessions WHERE
            user.sessions.events.eventId == 9873456) > 10 }
A view that includes  users who had more than a given number of a `user.sessions.events.eventId` within any single session.

###### Case G5
    VIEW v { INCLUDE user WHERE
        (COUNT(user.sessions.events) WHERE
            user.sessions.events.eventId == 9873456
                AND
            user.sessions.startTime BETWEEN 1483228800000 AND 1486489940000) > 10 }
         // implicit `SCOPE user`
Users who had more than a given number of a `user.sessions.events.eventId` in a specific time range across all sessions.

###### Case G6
    VIEW v { INCLUDE user WHERE
        (COUNT(user.sessions.events) SCOPE user.sessions WHERE
            user.sessions.events.eventId == 9873456
                AND
            user.sessions.startTime BETWEEN 1483228800000 AND 1486489940000) > 10 }
Users who had more than a given number of a `user.sessions.events.eventId` in a specific time range
within any single session.

###### Case G7
    VIEW v { INCLUDE user WHERE
        (COUNT(user.sessions.events) SCOPE ROLLING 30 DAY(user.sessions.startTime) WHERE
            user.sessions.events.eventId == 9873456) > 10 }
Users who had a more than a given number of a `user.sessions.events.eventId` within a rolling 30-day window.

###### Case G8
    VIEW v { INCLUDE user WHERE
        (COUNT(user.sessions.events) SCOPE ROLLING 24 HOUR(user.sessions.startTime) WHERE
            user.sessions.events.eventId == 9873456) > 10 }
Users who had a more than a given number of a `user.sessions.events.eventId` within a rolling 24-hour window.

###### Case G9
    VIEW v { INCLUDE user WHERE SUM(user.sessions.duration) > HOURS(2) }
Users who had a more two hours of total session time.

###### Case G10
    VIEW v { INCLUDE user WHERE (SUM(user.sessions.duration) SCOPE(user.sessions)) > MINUTES(10) }
Users who had a more than ten minutes of total event time in any given session.

###### Case G11
    VIEW v { INCLUDE user WHERE MAX(user.sessions.events.duration) > HOURS(2) }
Users who had a maximum session length greater than 2 hours.

###### Case G12
    VIEW v { INCLUDE user WHERE
        (COUNT(user.sessions) WHERE
            (COUNT(user.sessions.events)
                WHERE user.sessions.events.eventId  IN (9873456, 9873457, 9873458)) > 2) > 10 }
Users who had at least 5 sessions that each had at least two events from a list of three event types.

###### Case G13
    VIEW v { INCLUDE user WHERE NOT (
        COUNT(user.sessions) WHERE NOT (
           COUNT(user.sessions.events) SCOPE user.sessions WHERE
                    user.session.events.eventId IN (9873456, 9873457, 9873458)
        ) == 0
    ) == 0 }
Users that had at least one event from a list of three event types in every session.

## Sample Store Specific Filter Queries

###### Case S1
    SAMPLE ( 0.6 ) user.sessions.events
Remove 40% of all events

###### Case S2
    SAMPLE ( 0.6 ) user.sessions.events
        WHERE user.sessions.events.eventId == 9873456
Remove 40% of all events with a specific eventId

###### Case S3
    SAMPLE ( 0.6 ) user // WHERE TRUE is implicit
Remove 40% of all users

###### Case S4
    EXCLUDE event
        WHERE user.sessions.events.eventId NOT IN (9873456, 9873457, 9873458)
Remove all events in a black-list of event ids.

###### Case S5
    EXCLUDE event
        WHERE user.sessions.events.eventId NOT IN (9873456, 9873457, 9873458)
            AND
        user.project.installTime BETWEEN 1483228800 AND 1486425600
Remove all events in a black-list of event ids for users who installed between two dates.

###### Case S6
    EXCLUDE user.sessions
        WHERE user.sessions.startTime BETWEEN 1483228800 AND 1486489940
Exclude sessions that fall into a startTime window
