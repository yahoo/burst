#!/usr/bin/env bash

ERROR_DIR=${BURST_HOME}/logs/dump

if [ "$CPU_REQUEST" == "" ]; then
  CPU_REQUEST="1"
fi
if [ "$JAVA_NMT_OPTS" == "" ]; then
  JAVA_NMT_OPTS="-XX:NativeMemoryTracking=summary -XX:+PrintNMTStatistics"
fi
if [ "$EXTRA_JAVA_OPTS" == "" ]; then
  EXTRA_JAVA_OPTS=""
fi
if [ "$FAB_MONIKER" == "" ]; then
  FAB_MONIKER="$POD_NAME"
fi
if [ "$JVM_DIRECT" == "" ]; then
  JVM_DIRECT="4"
fi
if [ "$JVM_HEAP" == "" ]; then
  JVM_HEAP="2"
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
mkdir -p ${BURST_HOME}/logs/dump
mkdir -p ${BURST_HOME}/classpath-files
if [ "${SPINDLE_DIR}" == "" ]; then
    SPINDLE_DIR=${BURST_HOME}/data1/burst
fi
eval "SPINDLE_DIR=${SPINDLE_DIR}"
mkdir -p ${SPINDLE_DIR}

# Customization hook
if [ -f "$BURST_HOME/sbin/pre-start.sh" ]; then
  . $BURST_HOME/sbin/pre-start.sh
  didPrestart=$?
fi

######################################################
# additional java options if desired such as heap size
######################################################
containerOpts="-XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+UnlockDiagnosticVMOptions"
containerOpts="${containerOpts} -XX:ErrorFile=${ERROR_DIR}/burst-crash-%p.log"
containerOpts="${containerOpts} -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${ERROR_DIR}"
containerOpts="${containerOpts} -Xss2M -Xmx${JVM_HEAP}G -XX:MaxDirectMemorySize=${JVM_DIRECT}G"

if [ "$JAVA_NMT_OPTS" != "" ]; then
  containerOpts="${containerOpts} $JAVA_NMT_OPTS"
fi

if [ "${WORKLOAD}" = "worker" ]; then
    containerOpts="${containerOpts} -XX:ReservedCodeCacheSize=2G -XX:+PrintCodeCache"
    containerOpts="${containerOpts} -XX:-ResizePLAB -XX:+ParallelRefProcEnabled -XX:MetaspaceSize=256M"
    containerOpts="${containerOpts} -XX:+UseStringDeduplication"
fi

cellName="$DEPLOY_ENV"
mainClass="org.burstsys.main.ChooseWorkload"

envConfig="-Dburst.home=${BURST_HOME}" # set this so log files go to the correct place
envConfig="${envConfig} -Dburst.loglevel=${LOG_LEVEL}"
envConfig="${envConfig} -Dburst.fabric.moniker=${FAB_MONIKER}"
envConfig="${envConfig} -Dburst.cell.name=${cellName}"
envConfig="${envConfig} -DdeploymentName=${cellName}"

if [ "${WORKLOAD}" = "supervisor" ]; then
    mainClass="org.burstsys.master.server.container.BurstMasterMain"
    envConfig="${envConfig} -Dburst.fabric.master.host=${POD_IP}"
    envConfig="${envConfig} -Dburst.samplestore.api.host=${SAMPLESTORE_HOST}"
    envConfig="${envConfig} -Dburst.master.properties.file=supervisor.properties"

elif [ "${WORKLOAD}" = "worker" ]; then
    mainClass="org.burstsys.worker.BurstWorkerMain"
    envConfig="${envConfig} -Dburst.fabric.worker.core.count=${CPU_REQUEST}"
    envConfig="${envConfig} -Dburst.cell.master.host=${BURST_SUPERVISOR_HOST}"
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
  sleep 10
done
