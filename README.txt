$ ROOT_OF_SPRING_PROJECTS=$HOME/springwork
$ SOURCE_LOC=${ROOT_OF_SPRING_PROJECT}/test-mutualauth
# Building the jar
$ cd $SOURCE_LOC
$ gradlew --daemon build -x test

# Generating the certificates and keys required to run the tests

# Generating self-signed mycloudca certificate authority
$ ./gen-mycloud-io-ca.sh

# Generating certificate and keys of services signed by mycloudca authority
$ ./gen-myservice-certs.sh

# Generating certificate and keys of user signed by mycloudca authority in
# PKCS12 format
$ ./gen-myservice-client-certs.sh

# Running the webserver in local m/c
$ java -jar -DSOURCE_LOC=${SOURCE_LOC} -DSERVICE_NAME=myserviceA01.mycloud.io -D $SOURCE_LOC/build/libs/test-mutualauth-0.0.1-SNAPSHOT.war

# Running the webserver in a docker container having java
$ docker run -i -t --name mutualauth.MyService --rm -p 8080:8080 -h myserviceA01.mycloud.io -v /Users:/Users icsdev/java java -jar -DSOURCE_LOC=${SOURCE_LOC} -DSERVICE_NAME=myserviceA01.mycloud.io $SOURCE_LOC/build/libs/test-mutualauth-0.0.1-SNAPSHOT.war

# Running the client test against the above server
# Note that gradlew may not actually run the test, if nothing has changed.
# You can force the execution of the test using --rerun-tasks option, or
# by running it from inside your IDE
$ cd $SOURCE_LOC
$ env PORT=8080 SERVICE_NAME=myserviceA01.mycloud.io SOURCE_LOC=${SOURCE_LOC} gradlew -Dtest.single=MyServiceTest3 test
$ env PORT=8080 SERVICE_NAME=myserviceA01.mycloud.io SOURCE_LOC=${SOURCE_LOC} gradlew -Dtest.single=MyServiceTest3 --rerun-tasks test


# Shutting down the server container
$ curl -k -vvvv --request POST --header "Content-Type: application/json" --basic --user admin:password "https://localhost:8080/shutdown"

# Run MyServiceTest3 in haproxy setup that fronts 2 running instances of
# of a service in TLS mode
# NOTE:
#   Port 8080 of haproxy instance is not opened for client to use; instead
# port 80 is mapped to 8080 of the running instances.
# $ env PORT=80 SERVICE_NAME=myserviceA.mycloud.io SOURCE_LOC=${SOURCE_LOC} gradlew -Dtest.single=MyServiceTest3 --rerun-tasks test

# Clean start, creating all the docker related artifacts/metadata
$ env SOURCE_LOC=${SOURCE_LOC} docker-compose up

# Clean shutdown, removing all the docker related artifacts/metadata
$ env SOURCE_LOC=${SOURCE_LOC} docker-compose down

# Once docker artifacts/metadata were created, you can run:
$ env SOURCE_LOC=${SOURCE_LOC} docker-compose start
$ env SOURCE_LOC=${SOURCE_LOC} docker-compose stop
