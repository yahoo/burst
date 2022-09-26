![Burst](burst_h.png "") 

# `Playing with Burst`

There are a number of ways to get started with burst, but the most basic build is to burst locally and
start a standalone instance.
with a single worker and some sample data.  This is a good way to play around the the administration UI 
including running some EQL queries.


#### Install Java
Install Java 8.  Burst uses [AdoptOpenJdk 8](https://adoptopenjdk.net/).

#### Clone Burst Locally
Clone the burst repo and build [build the Burst source tree](doc/building.md).  

#### Start Burst Standalone
From the root of the project run the following command:
    
        
        java -cp ./burst-services/target/burst-services-3.15.0-SNAPSHOT.jar 
             -Dburst.supervisor.jsonfilemanager.watchdirectory=./doc/playground/json 
             -Dburst.liaison.port=4443
             org.burstsys.supervisor.server.container.BurstSupervisorMain 
             -s -w 1 --jsonSampleStore

The administration console is accessable at [using your local web browser](https://localhost:4443/waves).

##### Run A Query

1. Select a view using the catalog tab.  Click on `Search` and select the view in `default_json`. 
2. Switch to the query tab. Clock on `Open` and select `Load` for `EQL count of users, sessions, events` to load
   a simple query.  Change the schema name from `unity` to `quo`
3. Press `Execute` and after a few seconds a result pane will open with the results.
4. Switch to the waves tab to see what queries the server has run.  The latest one you ran above is at the top.
   Clicking the `GUID` link opens a detail pane of the execution.

---
------ [HOME](../readme.md) --------------------------------------------
