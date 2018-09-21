# Narayana standalone object store quickstart

## Overview

A transaction manager must store enough information such that it can guarantee recovery from failures.
This is achieved by persisting information in an Object Store. Various implementation are provided
to cater for various application requirements.

1. FileStoreExample shows how to change the store type to a file base store but in directory different from the default;
2. HornetqStoreExample shows how to use the Hornetq journal for transction logging;
3. VolatileStoreExample shows how to use an unsafe (because it does not persist logs in the event of
   failures and therefore does not support recovery) in-memory log store implementation.

## Usage

```
mvn compile
./run.[sh|bat]
```
or to run individual tests using the maven java exec plugin:

```
mvn -e compile exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.VolatileStoreExample
mvn -e compile exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.HornetqStoreExample
mvn -e compile exec:java -Dexec.mainClass=org.jboss.narayana.jta.quickstarts.FileStoreExample
```

## Expected output

When running examples one at a time look for the output

```
[INFO] BUILD SUCCESS
```

If you use the run script then you the line "[INFO] BUILD SUCCESS" should appear once for each example.

## What just happened

Each example either changes the object store directory or object store type (or both) and then runs a
transaction. Each example performs a relevant test to verify that the object store type or directory,
as appropriate, was used.
