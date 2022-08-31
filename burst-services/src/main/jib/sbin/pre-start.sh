#!/usr/bin/env bash

export KEYSTORE_FILE=$BURST_HOME/classpath-files/keystore.pkcs12

cat $SSL_CERT_BUNDLE_PATH $SSL_CERT_PATH > ${BURST_HOME}/conf/master.cert.pem
openssl pkcs12 -export -inkey $SSL_KEY_PATH -in ${BURST_HOME}/conf/master.cert.pem -out $KEYSTORE_FILE -password pass:$KEYSTORE_PASS
