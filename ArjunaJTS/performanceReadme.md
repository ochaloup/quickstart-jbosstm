FIRSTLY!
--------

We took existing quickstart repo so sorry it is interleaved in this folder!!!!!!!!!!!!

For the purpose of the performance tests just look at the code in:
	ArjunaJTS/jts
		(for JTS numbers)
	ArjunaJTS/jta (I know, I know, its misleading!)
		(for JTA numbers)
	as711-async-jta.xml
	as711-async-jts.xml
	as711-jts.xml

Also, the XAResource is a no-op, although the TM still does all its normal work the cost of "prepare" is artificially low but that is fine to test the TM, rather than the RM.

# to run with as711
# jts:
cd jts
unzip /home/tom/Downloads/jboss-as-7.1.1.Final.zip
mv jboss-as-7.1.1.Final/ server1
cp -rp server1 server2
# if you want to use async prepare
cp ../as711-async-jts.xml server1/standalone/configuration/standalone-full.xml
cp ../as711-async-jts.xml server2/standalone/configuration/standalone-full.xml
# if you want to just use jts
cp ../as711-jts.xml server1/standalone/configuration/standalone-full.xml
cp ../as711-jts.xml server2/standalone/configuration/standalone-full.xml
# if you want to patch the version of jbossjts cp ~/projects/jbosstm/eap-60x/install/lib/jbossjts.jar server1/modules/org/jboss/jts/main/jbossjts-4.16.2.Final.jar
# if you want to patch the version of jbossjts cp ~/projects/jbosstm/eap-60x/install/lib/jbossjts.jar server2/modules/org/jboss/jts/main/jbossjts-4.16.2.Final.jar
# open a terminal 
./server1/bin/standalone.sh -c standalone-full.xml -Djboss.socket.binding.port-offset=000
# open another terminal
./server2/bin/standalone.sh -c standalone-full.xml -Djboss.socket.binding.port-offset=100
# open a final terminal
mvn clean install jboss-as:deploy
curl http://localhost:8080/jboss-as-jts-application-component-1/addCustomer.jsf?name=100
# if you change ?name= you can run as many loops as you want, e.g. curl http://localhost:8080/jboss-as-jts-application-component-1/addCustomer.jsf?name=10000 will take a long time ;)


# jta:
cd jta
unzip /home/tom/Downloads/jboss-as-7.1.1.Final.zip
mv jboss-as-7.1.1.Final/ server3
# if you want to use async prepare - if not, the stock standalone-full.xml is fine
cp ../as711-async-jta.xml server3/standalone/configuration/standalone-full.xml
# if you want to patch the version of jbossjts cp ~/projects/jbosstm/eap-60x/install/lib/jbossjts.jar server1/modules/org/jboss/jts/main/jbossjts-4.16.2.Final.jar
# if you want to patch the version of jbossjts cp ~/projects/jbosstm/eap-60x/install/lib/jbossjts.jar server2/modules/org/jboss/jts/main/jbossjts-4.16.2.Final.jar
# open a terminal
./server3/bin/standalone.sh -c standalone-full.xml -Djboss.socket.binding.port-offset=200
# open another terminal
mvn clean install jboss-as:deploy
curl http://localhost:8280/jboss-as-jta/addCustomer.jsf?name=100
# see notes above for how many loops to run

## to run with master of AS7 - advanced use only, please ignore
## to run with jts:
#cd jts
#cp -rp ~/projects/jbosstm/narayana/jboss-as/build/target/jboss-as-7.2.0.Alpha1-SNAPSHOT/ jts/server1
#cp -rp ~/projects/jbosstm/narayana/jboss-as/build/target/jboss-as-7.2.0.Alpha1-SNAPSHOT/ jts/server2
#cp ../asMaster-jts.xml jts/server1/standalone/configuration/standalone-full.xml
#cp ../asMaster-jts.xml jts/server2/standalone/configuration/standalone-full.xml
## to run with asynchronous prepare:
#cp ../asMaster-async-jts.xml jts/server1/standalone/configuration/standalone-full.xml
#cp ../asMaster-async-jts.xml jts/server2/standalone/configuration/standalone-full.xml
#jta
#cp -rp ~/projects/jbosstm/narayana/jboss-as/build/target/jboss-as-7.2.0.Alpha1-SNAPSHOT/ jta/server3

