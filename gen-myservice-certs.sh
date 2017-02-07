#!/bin/bash
OUTDIR=${PWD}/certs

mkdir -p ${OUTDIR}

# We are generating certificates signed by mycloudca and importing it
# into ${DOMAIN}.jks
for service in myserviceA myserviceB
do
    for i in ${service} ${service}01 ${service}02
    do
        DOMAIN=${i}.mycloud.io
        if [[ ${i} == ${service} ]]
	then EXT_SAN=""
	else EXT_SAN="-ext SAN=dns:${service}.mycloud.io"
	fi

        # import the public certificate to a serverside java keystore file
        keytool -storetype JKS -keystore ${OUTDIR}/${DOMAIN}.jks -storepass password -importcert -noprompt -alias mycloudca -file ${OUTDIR}/mycloudca.cer

        keytool -genkeypair -dname "cn=$DOMAIN, ou=R&D, o=Mycloud LLC, c=US" -storetype JKS -keystore ${OUTDIR}/${DOMAIN}.jks -storepass password -keypass password -alias $DOMAIN -keyalg RSA
        keytool -storetype JKS -keystore ${OUTDIR}/${DOMAIN}.jks -storepass password -alias $DOMAIN -certreq -file ${OUTDIR}/$DOMAIN.csr

        keytool -gencert -infile ${OUTDIR}/$DOMAIN.csr -outfile ${OUTDIR}/$DOMAIN.cer -alias mycloudca -storetype JKS -keystore ${OUTDIR}/mycloudca.jks -storepass password -ext KU=digitalSignature,nonRepudiation -ext EKU=clientAuth,serverAuth,codeSigning ${EXT_SAN} -validity 365

        rm ${OUTDIR}/$DOMAIN.csr

        keytool -storetype JKS -keystore ${OUTDIR}/${DOMAIN}.jks -storepass password -importcert -noprompt -alias $DOMAIN -file ${OUTDIR}/$DOMAIN.cer
    done
done



