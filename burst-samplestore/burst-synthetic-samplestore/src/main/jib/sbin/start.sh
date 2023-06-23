#!/usr/bin/env bash
if [ -z "${APP_NAME}" ]; then
  appName="samplesource-${WORKLOAD}"
else
  appName="${APP_NAME}"
fi

# make sure important dirs exist
ERROR_DIR=${BURST_HOME}/logs/dump
mkdir -p ${ERROR_DIR}
containerOpts="${containerOpts} -XX:ErrorFile=${ERROR_DIR}/crash-%p.log"
containerOpts="${containerOpts} -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${ERROR_DIR}"

mkdir -p ${BURST_HOME}/classpath-files

# allow for late variable substitution in these environment variables
# because docker is so finicky about the timing.
eval "SSL_CERT_PATH=${SSL_CERT_PATH}"
eval "SSL_KEY_PATH=${SSL_KEY_PATH}"
eval "SSL_CERT_BUNDLE_PATH=${SSL_CERT_BUNDLE_PATH}"

# Customization hook
export PRESTART_HOME="$BURST_HOME/$appName-conf"
if [ -f "${PRESTART_HOME}/pre-start.sh" ]; then
  . ${PRESTART_HOME}/pre-start.sh
  didPrestart=$?
else
  didPrestart=0
fi


mainClass="org.burstsys.samplestore.store.ChooseWorkload"

envConfig="${envConfig} -Dburst.home=${BURST_HOME}" # set this so log files go to the correct place
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

JAVA_OPTS="${JAVA_OPTS} -Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager"
BURST_CLASS_PATH="$(cat $BURST_HOME/jib-classpath-file)"
if [ "$EXTRA_CLASS_PATH" != "" ]; then
  BURST_CLASS_PATH="${BURST_CLASS_PATH}:$EXTRA_CLASS_PATH"
fi

if [ "$didPrestart" == "0" ]; then
  echo "🚀 Starting samplestore 🚀"
  echo "java $containerOpts $JAVA_OPTS $EXTRA_JAVA_OPTS -cp $BURST_CLASS_PATH $envConfig $mainClass"
  sh -c "java $containerOpts $JAVA_OPTS $EXTRA_JAVA_OPTS -cp $BURST_CLASS_PATH $envConfig $mainClass"
else
  echo "Prestart script failed"
fi


while [ "$KEEPALIVE" != "" ]; do
  echo "KEEPALIVE set '$KEEPALIVE'"
  sleep 10
done
