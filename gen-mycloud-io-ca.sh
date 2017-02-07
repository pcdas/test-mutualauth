#!/bin/bash
OUTDIR=${PWD}/certs

mkdir -p ${OUTDIR}
#openssl genrsa -des3 -passout pass:password -out ${OUTDIR}/mycloudca.key 1024
#openssl req -new -passin pass:password -key ${OUTDIR}/mycloudca.key -out ${OUTDIR}/mycloudca.csr
#cp ${OUTDIR}/mycloudca.key ${OUTDIR}/mycloudca.key.org
#openssl rsa -in ${OUTDIR}/mycloudca.key.org -passin pass:password -out ${OUTDIR}/mycloudca.key
#openssl x509 -req -days 3650 -passin pass:password -in ${OUTDIR}/mycloudca.csr -signkey ${OUTDIR}/mycloudca.key -out ${OUTDIR}/mycloudca.crt
#
## remove certificate signing request now that the request has been completed
#rm ${OUTDIR}/mycloudca.csr

# create keystore mycloudca certificate & key
keytool -genkeypair -dname "cn=mycloudca, ou=R&D, o=Mycloud LLC, c=US" -storetype JKS -keystore ${OUTDIR}/mycloudca.jks -storepass password -keypass password -alias mycloudca -keyalg RSA
keytool -storetype JKS -keystore ${OUTDIR}/mycloudca.jks -storepass password -alias mycloudca -certreq -file ${OUTDIR}/mycloudca.csr
# self-sign it
keytool -gencert -infile ${OUTDIR}/mycloudca.csr -outfile ${OUTDIR}/mycloudca.cer -alias mycloudca -storetype JKS -keystore ${OUTDIR}/mycloudca.jks -storepass password -validity 3650
rm ${OUTDIR}/mycloudca.csr

