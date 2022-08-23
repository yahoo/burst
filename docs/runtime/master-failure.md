---
    title: Dealing with a Cell Master Failure
---

# Failure Scenario

The master host for a burst cell cluster has failed outright or is having 
unacceptable number of soft failures such as memory crc failures,  failed disk writes, etc.  You
need to set up another host in the cluster as the master and restart
the cell.

# Prerequisites

- chefdk is installed locally
- you have yubuikey access to the cell machines
- ssh is setup
- you have permissions to `sudo su - ` to `root` and `burst` users.

# Steps

Assume that the master name is `bu399.flurry.bf2.yahoo.com` and we are on the `bf2_c2` cell.  We want to make
`bu398.flurry.bf2.yahoo.com` the new cell master.  In a burst cluster,  the cell master is also the spark master and
the hdfs master.

- Mark the failing master
	- `knife node edit bu399.flurry.bf2.yahoo.com`, and put it into the dead zone,
	  by appending `_dead` to it's `chef_environment` value to become `bf2_c2_dead`.
- Change the new host from a worker to a master and stop it's worker processes
	- `knife node edit bu398.flurry.bf2.yahoo.com`, and mark it as a master
	  by changing its `tags` entry from `worker` to `master`.
	-  Login into the new master and switch to root,  `ssh bu399.flurry.bf2.yahoo.com` followed by `sudo su - `
	-  Make sure there is no cron entry that automatically runs a chef-converge.  Remove entries running `chef-client`
	-  Logout
- Stop the failing master
	- If the failing master is still reachable, log into it, `ssh bu399.flurry.bf2.yahoo.com` , and switch to
	the `burst` user, `sudo su - burst`, and kill all the java processes across all cluster machines by doing `sbin/kill-javas.sh`.
	If it isn't reachable you will need OPS to make sure it is halted.
	- Log out
- Converge the workers to use the new master
	- `knife ssh "chef_environment:bf2_2 AND tags:worker" -C 100 "sudo chef-client"`.  This takes a while and you need
	to watch for the slow blinking light on your yubikey dongle:  you most likely will not see a prompt and it will happen
	a couple times over this long running converge. Even if you are vigilant, a number of hosts will probably miss the authorization
	and fail.  Doing it again is harmless and will catch any missed hosts.
- Converge the new master
	- `knife ssh "chef_environment:bf2_2 AND tags:master" "sudo chef-client"`, again watching for the slowly flashing light 
	on the yubikey dongle.
- Check the various servers
    - First see how [HDFS master](http://bu398.flurry.bf2.yahoo.com:50070) is doing.  Make sure the number of live nodes
    matches the number of workers.  
    - See how [Spark master](http://bu398.flurry.bf2.yahoo.com:8080) is doing.  It should have the same number of nodes (but
    be careful as it counts the driver and so inflates the number by one).
    - See how [Cell master](http://bu398.flurry.bf2.yahoo.com:57030) is doing.  You should see the Admin UI screen.
- Check the running system
    - Follow the steps for validating a system restart.
    
# Problems and Troubleshooting
The most common indicator that something didn't go right, is the count of HDFS datanodes and Spark executors.  There might be
not enough or too many. 

- Host counts off in HDFS or SPARK.
	- Log in, `ssh bu398.flurry.bf2.yahoo.com` , and switch to the `burst` user, `sudo su - burst`.
	- Stop the docker master process:  `sbin/stop-burst.sh`
	- Kill all the java processes across all cluster machines by doing `sbin/kill-javas.sh`
	It's best to wait until all the data nodes are live and the master is out of SAFE mode.
	- Start spark: `spark/sbin/start-all.sh`. See how [Spark master](http://bu398.flurry.bf2.yahoo.com:8080) is doing.
	It's best to wait until all the executors register with the master.
	- Start the master docker container:  `sbin/start-burst-master.sh cell-spec.sh`.
	See how [Cell master](http://bu398.flurry.bf2.yahoo.com:57030) is doing.
