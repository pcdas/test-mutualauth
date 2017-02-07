#!/bin/bash
OUTDIR=${PWD}/certs

mkdir -p ${OUTDIR}

# import the certificate authority to java keystore file of client
keytool -storetype JKS -keystore ${OUTDIR}/myservice-client.jks -storepass password -importcert -noprompt -alias mycloudca -file ${OUTDIR}/mycloudca.cer

# We are generating 2 certificates signed by mycloudca, and import
# them into their respective .p12 file
for cname in alice bob
do
    keytool -genkeypair -dname "cn=$cname, ou=R&D, o=Mycloud LLC, c=US" -storetype PKCS12 -keystore ${OUTDIR}/$cname.p12 -storepass password -keypass password -alias $cname -keyalg RSA

    keytool -storetype PKCS12 -keystore ${OUTDIR}/$cname.p12 -storepass password -alias $cname -certreq -file ${OUTDIR}/$cname.csr

    keytool -gencert -infile ${OUTDIR}/$cname.csr -outfile ${OUTDIR}/$cname.cer -alias mycloudca -storetype JKS -keystore ${OUTDIR}/mycloudca.jks -storepass password -ext KU=digitalSignature,nonRepudiation -ext EKU=clientAuth,codeSigning -validity 365
    rm ${OUTDIR}/$cname.csr

    keytool -storetype PKCS12 -keystore ${OUTDIR}/$cname.p12 -storepass password -importcert -noprompt -alias mycloudca -file ${OUTDIR}/mycloudca.cer
    keytool -storetype PKCS12 -keystore ${OUTDIR}/$cname.p12 -storepass password -importcert -noprompt -alias $cname -file ${OUTDIR}/$cname.cer
done


