#!/usr/bin/env bash

mainClass="org.burstsys.samplestore.store.ChooseWorkload"

envConfig="-Dburst.home=${BURST_HOME}" # set this so log files go to the correct place
envConfig="${envConfig} -Dburst.loglevel=${LOG_LEVEL:=INFO}"
envConfig="${envConfig} -Dburst.cell.name=${DEPLOYMENT_NAME:=synthetic-samplesource}"
envConfig="${envConfig} -DdeploymentName=${DEPLOYMENT_NAME}"
envConfig="${envConfig} -Dburst.samplestore.api.host=${SAMPLESTORE_HOST}"
envConfig="${envConfig} -Dburst.fabric.net.port=${BURST_STORE_FABRIC_PORT}"

if [ "${WORKLOAD}" = "supervisor" ]; then
    mainClass="org.burstsys.samplestore.store.SampleStoreSupervisorMain"
    envConfig="${envConfig} -Dburst.fabric.net.host=${POD_IP}"
elif [ "${WORKLOAD}" = "worker" ]; then
    mainClass="org.burstsys.samplestore.store.SampleStoreWorkerMain"
    envConfig="${envConfig} -Dburst.fabric.net.host=${BURST_STORE_SUPERVISOR_HOST}"
fi

JAVA_OPTS="-Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager"
BURST_CLASS_PATH="$(cat $BURST_HOME/jib-classpath-file)"
if [ "$EXTRA_CLASS_PATH" != "" ]; then
  BURST_CLASS_PATH="${BURST_CLASS_PATH}:$EXTRA_CLASS_PATH"
fi

echo "🚀 Starting samplestore 🚀"

echo "java $containerOpts $JAVA_OPTS $EXTRA_JAVA_OPTS -cp $BURST_CLASS_PATH $envConfig $mainClass"
sh -c "java $containerOpts $JAVA_OPTS $EXTRA_JAVA_OPTS -cp $BURST_CLASS_PATH $envConfig $mainClass"

while [ "$KEEPALIVE" != "" ]; do
  echo "KEEPALIVE set '$KEEPALIVE'"
  sleep 10
done
