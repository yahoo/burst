#!/usr/bin/env bash

mainClass="org.burstsys.synthetic.samplestore.main.ChooseWorkload"

envConfig="-Dburst.home=${BURST_HOME}" # set this so log files go to the correct place
envConfig="${envConfig} -Dburst.loglevel=${LOG_LEVEL:=INFO}"
envConfig="${envConfig} -Dburst.cell.name=${DEPLOYMENT_NAME:=synthetic-samplesource}"
envConfig="${envConfig} -DdeploymentName=${DEPLOYMENT_NAME}"

if [ "${WORKLOAD}" = "supervisor" ]; then
    mainClass="org.burstsys.synthetic.samplestore.main.SyntheitcSampleSourceSupervisorMain"

elif [ "${WORKLOAD}" = "worker" ]; then
    mainClass="org.burstsys.synthetic.samplestore.main.SyntheticSampleSourceWorkerMain"

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
