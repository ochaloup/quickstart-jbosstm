### Description

This quickstart shows how to get started with Narayana and Transactional Driver with a simple JDBC example.

### Start Tomcat

Start Tomcat in the usual manner, for example:
`$CATALINA_HOME/bin/catalina.sh  run`

### Build the app

`mvn clean package`

### Deploy the app

`cp target/*.war apache-tomcat-9.0.7/webapps/`

### Get strings from the database

`curl http://localhost:8080/transactionaldriver-and-tomcat`

### Save string to the database

`curl --data "test" http://localhost:8080/transactionaldriver-and-tomcat`
