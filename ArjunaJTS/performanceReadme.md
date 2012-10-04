# to run with jts:
cd jts
cp -rp ~/projects/jbosstm/narayana/jboss-as/build/target/jboss-as-7.2.0.Alpha1-SNAPSHOT/ jts/server1
cp -rp ~/projects/jbosstm/narayana/jboss-as/build/target/jboss-as-7.2.0.Alpha1-SNAPSHOT/ jts/server2
cp ../server1-standalone-full.xml jts/server1/standalone/configuration/standalone-full.xml
cp ../server2-standalone-full.xml jts/server2/standalone/configuration/standalone-full.xml
# to run with asynchronous prepare:
cp ../server1-async-prepare-standalone-full.xml jts/server1/standalone/configuration/standalone-full.xml
cp ../server2-async-prepare-standalone-full.xml jts/server2/standalone/configuration/standalone-full.xml

# to run with as711
unzip /home/tom/Downloads/jboss-as-7.1.1.Final.zip
mv jboss-as-7.1.1.Final/ server1
cp -rp server1 server2
cp ../as711-async.xml server1/standalone/configuration/standalone-full.xml
cp ../as711-async.xml server2/standalone/configuration/standalone-full.xml
cp ~/projects/jbosstm/eap-60x/install/lib/jbossjts.jar server1/modules/org/jboss/jts/main/jbossjts-4.16.2.Final.jar
cp ~/projects/jbosstm/eap-60x/install/lib/jbossjts.jar server2/modules/org/jboss/jts/main/jbossjts-4.16.2.Final.jar

./server1/bin/standalone.sh -c standalone-full.xml -Djboss.socket.binding.port-offset=000
./server2/bin/standalone.sh -c standalone-full.xml -Djboss.socket.binding.port-offset=100
mvn clean install jboss-as:deploy
curl http://localhost:8080/jboss-as-jts-application-component-1/addCustomer.jsf?name=100

# to run with jta:
cd jta
cp -rp ~/projects/jbosstm/narayana/jboss-as/build/target/jboss-as-7.2.0.Alpha1-SNAPSHOT/ jta/server3
./server3/bin/standalone.sh -c standalone-full.xml -Djboss.socket.binding.port-offset=200
curl http://localhost:8280/jboss-as-jta/addCustomer.jsf?name=100
