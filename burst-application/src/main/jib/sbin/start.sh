#!/usr/bin/env bash

ERROR_DIR=${BURST_HOME}/logs/dump

if [ -z "${APP_NAME}" ]; then
  appName="burst-${WORKLOAD}"
else
  appName="${APP_NAME}"
fi

if [ "$EXTRA_JAVA_OPTS" == "" ]; then
  EXTRA_JAVA_OPTS=""
fi
if [ "$FAB_MONIKER" == "" ]; then
  FAB_MONIKER="$POD_NAME"
fi
if [ "$SAMPLESTORE_HOST" == "" ]; then
  SAMPLESTORE_HOST="unknown"
fi
if [ "$DEPLOY_ENV" == "" ]; then
  DEPLOY_ENV="unknown"
fi
if [ "${LOG_LEVEL}" == "" ]; then
    LOG_LEVEL="INFO"
fi

# allow for late variable substitution in these environment variables
# because docker is so finicky about the timing.
eval "SSL_CERT_PATH=${SSL_CERT_PATH}"
eval "SSL_KEY_PATH=${SSL_KEY_PATH}"
eval "SSL_CERT_BUNDLE_PATH=${SSL_CERT_BUNDLE_PATH}"
eval "BURST_SUPERVISOR_HOST=${BURST_SUPERVISOR_HOST}"

# make sure important dirs exist
ERROR_DIR=${BURST_HOME}/logs/dump
mkdir -p ${ERROR_DIR}
containerOpts="${containerOpts} -XX:ErrorFile=${ERROR_DIR}/crash-%p.log"
containerOpts="${containerOpts} -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${ERROR_DIR}"

mkdir -p ${BURST_HOME}/classpath-files

if [ "${SPINDLE_DIR}" == "" ]; then
    SPINDLE_DIR=${BURST_HOME}/data1/burst
fi
eval "SPINDLE_DIR=${SPINDLE_DIR}"
mkdir -p ${SPINDLE_DIR}

# Customization hook
export PRESTART_HOME="$BURST_HOME/$appName-conf"
if [ -f "${PRESTART_HOME}/pre-start.sh" ]; then
  . ${PRESTART_HOME}/pre-start.sh
  didPrestart=$?
else
  didPrestart=0
fi

cellName="$DEPLOY_ENV"
mainClass="org.burstsys.main.ChooseWorkload"

envConfig="${envConfig} -Dburst.home=${BURST_HOME}" # set this so log files go to the correct place
envConfig="${envConfig} -Dburst.loglevel=${LOG_LEVEL:-INFO}"
envConfig="${envConfig} -Dburst.fabric.moniker=${FAB_MONIKER}"
envConfig="${envConfig} -Dburst.cell.name=${cellName}"
envConfig="${envConfig} -DdeploymentName=${cellName}"

if [ "${WORKLOAD}" = "supervisor" ]; then
    mainClass="org.burstsys.supervisor.BurstSupervisorMain"
    envConfig="${envConfig} -Dburst.fabric.supervisor.host=${POD_IP}"
    envConfig="${envConfig} -Dburst.samplestore.api.host=${SAMPLESTORE_HOST}"
    envConfig="${envConfig} -Dburst.supervisor.properties.file=supervisor.properties"

elif [ "${WORKLOAD}" = "worker" ]; then
    mainClass="org.burstsys.worker.BurstWorkerMain"
    envConfig="${envConfig} -Dburst.cell.supervisor.host=${BURST_SUPERVISOR_HOST}"
    envConfig="${envConfig} -Dburst.catalog.api.host=${BURST_SUPERVISOR_HOST}"
    envConfig="${envConfig} -Dburst.agent.api.host=${BURST_SUPERVISOR_HOST}"
    envConfig="${envConfig} -Dburst.fabric.net.host=${BURST_SUPERVISOR_HOST}"
    envConfig="${envConfig} -Dburst.worker.properties.file=worker.properties"
fi

######################################################
# 🚀 Liftoff 🚀
######################################################
JAVA_OPTS="-Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager"
BURST_CLASS_PATH="$(cat $BURST_HOME/jib-classpath-file)"
if [ "$EXTRA_CLASS_PATH" != "" ]; then
  BURST_CLASS_PATH="${BURST_CLASS_PATH}:$EXTRA_CLASS_PATH"
fi

if [ "$didPrestart" == "0" ]; then
  echo "🚀 Starting burst 🚀"
  eval "echo \"$(cat ${BURST_HOME}/conf/supervisor.properties.template)\"" > ${BURST_HOME}/classpath-files/supervisor.properties
  eval "echo \"$(cat ${BURST_HOME}/conf/worker.properties.template)\"" > ${BURST_HOME}/classpath-files/worker.properties

  echo "java $containerOpts $JAVA_OPTS $EXTRA_JAVA_OPTS -cp $BURST_CLASS_PATH $envConfig $mainClass"
  sh -c "java $containerOpts $JAVA_OPTS $EXTRA_JAVA_OPTS -cp $BURST_CLASS_PATH $envConfig $mainClass"
else
  echo "Prestart script failed"
fi

while [ "$KEEPALIVE" != "" ]; do
  echo "KEEPALIVE set '$KEEPALIVE'"
  sleep 300
done
