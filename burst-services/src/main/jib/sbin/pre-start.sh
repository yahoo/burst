#!/usr/bin/env bash

export KEYSTORE_FILE=$BURST_HOME/classpath-files/keystore

cat $SSL_CERT_BUNDLE_PATH $SSL_CERT_PATH > ${BURST_HOME}/conf/master.cert.pem
openssl pkcs12 -export -inkey $SSL_KEY_PATH -in ${BURST_HOME}/conf/master.cert.pem -out ${BURST_HOME}/conf/service.cert.pkcs12 -password pass:$KEYSTORE_PASS
keytool -importkeystore -srckeystore ${BURST_HOME}/conf/service.cert.pkcs12  -srcstoretype PKCS12 -destkeystore $KEYSTORE_FILE -storepass $KEYSTORE_PASS -srcstorepass $KEYSTORE_PASS -noprompt
