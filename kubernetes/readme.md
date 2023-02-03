# Running locally

## Prerequisites

To run locally you will need to generate certificates because much of the dash communication uses websockets, which
require HTTPS. One way to generate an appropriate keystore is to use [mkcert](https://github.com/FiloSottile/mkcert):

```shell
# from repo root
cp "$(mkcert -CAROOT)/rootCA.pem" kubernetes/local-cluster/certs/rootCA.pem
mkcert -cert-file kubernetes/local-cluster/certs/cert.pem -key-file kubernetes/local-cluster/certs/key.pem localhost 127.0.0.1 ::1
```

The docker images for the burst cell and the synthetic sample source must be uploaded to the local docker repository.
This can happen during a build cycle by adding `-P build-local-docker` and
`-D dockerRepo="<a repo with a corretto image>"`
## Starting a Local Cell

Once you have generated certs in the `kubernetes/local-cluster/certs` directory you can generate and deploy the
kubernetes configuration. The generated file is sufficient to initialize an empty kubernetes cluster and start the burst
cell.

```shell
# from repo root
java kubernetes/local-cluster/Generate.java $BURST_VERSION
kubectl apply -f kubernetes/local-cluster/generated/Burst.yaml
```

A full description of the arguments to `Generate.java` are available by running `java Generate.java --help`.

## Inserting a Domain and View

In order to explore a synthetic dataset you will need to configure a domain and view.

```shell
# create domain
curl -k 'https://localhost:4443/api/supervisor/catalog/newDomain' \
  -H 'Authorization: Basic YnVyc3Q6YnVyc3RvbWF0aWM=' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  --data-raw 'moniker=Synthetic%20Data%20-%20Synthetic%20Unity' \
  --compressed
# DOMAIN_PK=PK_IN_JSON_FROM_CURL

# update domain, replacing DOMAIN_PK with the appropriate value
curl -k 'https://localhost:4443/api/supervisor/catalog/updateDomain' \
  -H 'Authorization: Basic YnVyc3Q6YnVyc3RvbWF0aWM=' \
  -H 'Content-Type: application/json' \
  -H 'Origin: https://localhost:4443' \
  --data-raw '{"pk":DOMAIN_PK,"moniker":"Synthetic Data - Synthetic Unity","labels":{},"udk":"","domainProperties":{"synthetic.samplestore.use-localhost":"true","synthetic.samplestore.loci.count":"1","synthetic.samplestore.press.dataset":"simple-unity"}}' \
  --compressed

# create view, replacing DOMAIN_PK with the appropriate value
curl -k 'https://localhost:4443/api/supervisor/catalog/newView' \
  -H 'Authorization: Basic YnVyc3Q6YnVyc3RvbWF0aWM=' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  --data-raw 'domainPk=DOMAIN_PK&moniker=Default%20Synthetic%20View&schemaName=unity' \
  --compressed \
  --insecure
# VIEW_PK=PK_IN_JSON_FROM_CURL

# update view, replacing DOMAIN_PK and VIEW_PK with the appropriate values
curl -k 'https://localhost:4443/api/supervisor/catalog/updateView' \
  -H 'Authorization: Basic YnVyc3Q6YnVyc3RvbWF0aWM=' \
  -H 'Content-Type: application/json' \
  -H 'Origin: https://localhost:4443' \
  --data-raw '{"pk":VIEW_PK,"moniker":"Default Synthetic View","labels":{},"domainFk":DOMAIN_PK,"generationClock":1663811402638,"schemaName":"unity","storeProperties":{"synthetic.samplestore.press.item.count":"5","burst.samplestore.source.version":"0.0","burst.samplestore.source.name":"synthetic-samplesource","burst.store.name":"sample"},"viewMotif":"VIEW template {\n  INCLUDE user WHERE user.application.firstUse.sessionTime >= (NOW - DAYS(30))\n  INCLUDE user.sessions.events where false\n}\n        ","viewProperties":{"burst.view.next.load.stale":"86400000","burst.view.earliest.load.at":"1663811402638"},"udk":" "}' \
  --compressed
```

### Querying Synthetic Dat and Changing Parameters

**Execute your first count query**

1. Go to the `Catalog` tab, type `Synthetic` in the `View` search box and click `Search`
2. Go the the `Query` tab, ensure that `Synthetic Data - Synthetic Unity` and `Default Synthetic View` are selected in
   the `Domain` and `View` selection boxes
3. Enter the following query text and click `Execute`
    ```
    select as Basic_Count
      count(user) as users,                 // count of all users
      count(user.sessions) as sessions,     // count of sessions across all users user
      count(user.sessions.events) as events // count of all events in all sessions across all users
    from schema unity
    ```

After a few seconds you should see the results of the query below the query text. There should be a single row with
columns `users`, `sessions`, and `events`. The value for `users` should be 5, with 50 under `sessions` and 500
under `events`

**Execute your first query with dimensions**

If you just ran a count query, skip to step 3.

1. Go to the `Catalog` tab, type `Synthetic` in the `View` search box and click `Search`
2. Go the the `Query` tab, ensure that `Synthetic Data - Synthetic Unity` and `Default Synthetic View` are selected in
   the `Domain` and `View` selection boxes
3. Go to the query tab, enter the following query text and hit `Execute`
    ```
   select as Count_By_Event
       user.id as userId,                        // record the user's id
       user.sessions.events.id as eventId,       // record the event id for each even in every session
       count(user.sessions.events) as eventCount // count the number of times we see each combination of (userId, eventId) 
   beside select as Count_By_Session_By_Event    // run a second query during the same scan of the data
       user.id as userId,                        // record the user's id
       user.sessions.id as sessionId,            // record the session id
       user.sessions.events.id as eventId,       // record the event id for each even in every session
       count(user.sessions.events) as eventCount // count the number of times we see each combination of (userId, sessionId, eventId)
   from schema unity
   where user.id == "User#1"                     // only collect results for User#1
    ```

The results of `Count_By_Event` should have 10 rows. Every row should have `User#1` an event id from 1 to 10, with
the `eventCount` column equal to 10. This is because the default parameters for the synthetic dataset creates 10
sessions for each user, each containing 10 events–one each with ids 1 to 10.

The results of `Count_By_Session_By_Event` should have 100 rows. There should be 10 sets of 10 rows with session ids
from 1 to 10. In each set of 10 rows there should be one row for each of the event ids 1 to 10. This is because each
session only has 1 occurrence of each event and our counts are dimensioning by `user.sessions.id`
and `user.sessions.events.id`

**Changing synthetic data parameters**

Many of the characteristics of the synthetic data can be tuned by setting properties in the view. Let's investigate how
these parameters play out in our queries.

1. Go to the query tab, and change the query to just the second query from the last example.
    ```
   select as Count_By_Session_By_Event           // run a second query during the same scan of the data
       user.id as userId,                        // record the user's id
       user.sessions.id as sessionId,            // record the session id
       user.sessions.events.id as eventId,       // record the event id for each even in every session
       count(user.sessions.events) as eventCount // count the number of times we see each combination of (userId, sessionId, eventId)
   from schema unity
   where user.id == "User#1"                     // only collect results for User#1
   ```
2. Go to the `Catalog` tab and select the `CLI Test Domain`
3. Click the `+` under `Store Properties` and enter `synthetic.unity.eventCount` for the property and `11` for the value
4. Go back to the `Query` tab and run the query. Notice that in `Count_By_Event` the row (userId=User#1, eventId=1) now
   has an eventCount of 20 Also notice that for each row in `Count_By_Session_By_Event` where eventId=1 the eventCount
   is now 2. This is because every session now has 11 events, and the event ids cycle from 1 to 10 within each session.
   If you continue to increment the `synthetic.unity.eventCount` property by one you can watch as the eventCount values
   go up by 10 (one for each session) in the `Count_By_Event` query and the rows for the next event id
   in `Count_By_Session_By_Event` also go up by one for the next event id in each session.

You can see the full [documentation][datasource] for the synthetic data source to see which parameters you can
manipulate and what the default values are. There is also [documentation][syntheitc-samplesource] for the synthetic
samplesource.

[datasource]: ../burst-brio/src/main/scala/org/burstsys/brio/flurry/provider/unity/readme.md

[syntheitc-samplesource]: ../burst-samplestore/burst-json-samplestore/readme.md
