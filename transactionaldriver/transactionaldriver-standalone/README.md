OVERVIEW
--------
This example shows three different approaches to work with Narayana jdbc transactional driver
(https://github.com/jbosstm/narayana/tree/master/ArjunaJTA/jdbc).

You can see here how to register an XADataSource to the driver and then how to use
the managed connection get back from the transactional resource to work with such connection.

See the approaches at classes

* [src/main/java/io/narayana/DriverProvidedXADataSource.java](src/main/java/io/narayana/DriverProvidedXADataSource.java)
* [src/main/java/io/narayana/DriverDirectRecoverable.java](src/main/java/io/narayana/DriverDirectRecoverable.java)
* [src/main/java/io/narayana/DriverIndirectRecoverable.java](src/main/java/io/narayana/DriverIndirectRecoverable.java)

TESTING
-----

You can run tests and check the behaviour in case of commit and rollback scenarios

```
mvn test
```

To get information from the TRACE log for example to check how recovery works, you can run with added java util logging
properties file.

```
mvn test -Dtest=TransactionalDriverTest#transactionDriverDirectRecoverableRecovery -Djava.util.logging.config.file=src/main/resources/logging.properties 2>&1 | tee my.log
```
