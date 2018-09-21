# Quickstarts

## Introduction

The repository contains a set of quickstart projects which provide working example
for specific Narayana transaction manager capabilities.
The quickstarts can be used as a reference for your own project.

The list of all available quickstart can be found down at this page.
Each quickstart is categorized with tags that depict areas the quickstart is connected with.

* [List of quickstarts](#list-of-quickstarts)
* [Quickstarts by tag](#quickstarts-by-tag)
* [Contacting us](#contacting-us)
* [Quickstarts in CI environment](#quickstarts-in-ci-environment)
* [Running a single quickstart](#running-a-single-quickstart)
* [Running all quickstarts in a single command](#running-all-quickstarts-in-a-single-command)

***TODO: link here quickstarts from WildFly!!!***

## List of quickstarts

| Project name | Description | Maven 

* [ArjunaCore/txoj](ArjunaCore/txoj/) : showcase of how to use the Transactional Object for Java (_TXOJ_)
  which is the core implementation concept of the Narayana state machine (_org.jboss.narayana.arjunacore.quickstart:txoj_),
  **tags:** _[arjuna.core](#arjuna-core-tag), [standalone](#standalone-tag)_
* [ArjunaJTA/maven](ArjunaJTA/maven) : minimalistic maven project showing how to configure
  maven to use the Narayana JTA implementation (_org.jboss.narayana.quickstart.jta:maven_),
  **tags:** _[jta](#jta-tag), [standalone](#standalone-tag)_
* [ArjunaJTA/javax_transaction](ArjunaJTA/javax_transaction/) :  basics on using JTA API in your application,
  you can check how to obtain the `UserTransaction` and `TransactionManager` with use of the Narayana API
  (_org.jboss.narayana.quickstart.jta:javax_transaction_),
  **tags:** _[jta](#jta-tag), [standalone](#standalone-tag)_
* [ArjunaJTA/jee_transactional_app](ArjunaJTA/jee_transactional_app/) : WildFly application
  which shows use of the transaction management in EJB when invoked from CDI bean.
  (_org.jboss.narayana.quickstart.jta:jee_transactional_app_),
  **tags:** _[jta](#jta-tag), [wildfly](#wildfly-tag)_
* [ArjunaJTA/object_store](ArjunaJTA/object_store/) : showing ways how to configure
  Narayana to run different types of object stores in standalone mode
  (_org.jboss.narayana.quickstart.jta:object_store_),
  **tags:** _[jta](#jta-tag), [narayana.configuration](#narayana-configuration-tag), [standalone](#standalone-tag)_
* [ArjunaJTA/recovery](ArjunaJTA/recovery/) : example of running periodic recovery
  in Narayana standalone. The setup shows multiple implementation of the recovery storage
  for could be configured by user.
  (_org.jboss.narayana.quickstart.jta:recovery_),
  **tags:** _[jta](#jta-tag), [narayana.configuration](#narayana-configuration-tag), [standalone](#standalone-tag)_
* [ArjunaJTS/interop](ArjunaJTS/interop/) : transactional EJB calls between GlassFish and WildFly
  (_org.jboss.narayana.quickstart.jts:jts-interop-quickstart_),
  **tags:** _[jts](#jts-tag),[wildfly](#wildfly-tag),[glassfish](#glassfish-tag)_
* [ArjunaJTS/jts](ArjunaJTS/jts/) : demonstration of setting up JTS transactions
  in WildFly and how to use EJB2 beans to pass the transactional context
  over the remote IIOP call
  (_org.jboss.narayana.quickstart.jts:jboss-as-jts-parent_),
  **tags:** _[jts](#jts-tag),[wildfly](#wildfly-tag)_

* [ArjunaJTS/](ArjunaJTS//)
  (_org.jboss.narayana.quickstart.jts:_),
  **tags:** _[jts](#jts-tag),_
* [ArjunaJTS/](ArjunaJTS//)
  (_org.jboss.narayana.quickstart.jts:_),
  **tags:** _[jts](#jts-tag),_
* [ArjunaJTS/](ArjunaJTS//)
  (_org.jboss.narayana.quickstart.jts:_),
  **tags:** _[jts](#jts-tag),_


## Quickstart categorization

* **[arjuna.core](#arjuna-core-tag-definition)**<a name='arjuna-core-tag'> : [ArjunaCore/txoj](ArjunaCore/txoj/)

* **[narayana.configuration](#narayana-configuration-tag-definition)**<a name='narayana-configuration-tag'> :
  [ArjunaJTA/object_store](ArjunaJTA/object_store/), [ArjunaJTA/recovery](ArjunaJTA/recovery/)

* **[jta](#jta-tag-definition)**<a name='jta-tag'> : [ArjunaJTA/maven](ArjunaJTA/maven/), [ArjunaJTA/javax_transaction](ArjunaJTA/javax_transaction/),
  [ArjunaJTA/jee_transactional_app](ArjunaJTA/jee_transactional_app/), [ArjunaJTA/object_store](ArjunaJTA/object_store/),
  [ArjunaJTA/recovery](ArjunaJTA/recovery/)

* **[jts](#jts-tag-definition)**<a name='jts-tag'> : [ArjunaJTS/interop](ArjunaJTS/interop/),
  [ArjunaJTS/jts](ArjunaJTS/jts/)

* **[standalone](#standalone-tag-definition)**<a name='standalone-tag'> : [ArjunaCore/txoj](ArjunaCore/txoj/),
  [ArjunaJTA/maven](ArjunaJTA/maven/), [ArjunaJTA/javax_transaction](ArjunaJTA/javax_transaction/), [ArjunaJTA/object_store](ArjunaJTA/object_store/),
  [ArjunaJTA/recovery](ArjunaJTA/recovery/)

* **[wildfly](#wildfly-tag-definition)**<a name='wildfly-tag'> : [ArjunaJTA/jee_transactional_app](ArjunaJTA/jee_transactional_app/),
  [ArjunaJTS/interop](ArjunaJTS/interop/), [ArjunaJTS/jts](ArjunaJTS/jts/)

* **[glassfish](#glassfish-tag-definition)**<a name='glassfish-tag'> : [ArjunaJTS/interop](ArjunaJTS/interop/)

### Tags definition

* **arjuna.core**<a name='arjuna-core-tag-definition'> : demonstrating capabilities of Narayana API,
  it's helpful for developers want to write a transaction state machine
  and don't want to start on a green field but rather used battle tested library
* **narayana.configuration**<a name='narayana-configuration-tag-definition'> : depicting aspects
  of Narayana configuration and showing options of such configurations
* **jta**<a name='jta-tag-definition'> : using JTA API to demonstrate transaction processing
* **jts**<a name='jts-tag-definition'> : using JTS API to demonstrate how the Narayana transaction system
  could be run and configured to run distributed JTS transactions
* **standalone**<a name='standalone-tag-definition'> : running as standalone Java SE application
* **wildfly**<a name='wildfly-tag-definition'> : running as deployment on WildFly application server
* **glassfish**<a name='glassfish-tag-definition'> : running on GlassFish application server


## Contacting us

We are always happy to talk transactions and how-to use Narayana in exotic and not so exotic environments.
If you have ideas for what we can add to the quickstarts to make them more useful please do reach out to us over on our forum:
https://developer.jboss.org/en/jbosstm/

## Quickstarts in CI environment

If you want to see how we run the quickstarts in our continuous integration environment, take a look at [scripts/hudson/quickstart.sh](scripts/hudson/quickstart.sh).

## Running a single quickstart

Change directory into the required quickstart and follow the instructions in the [README.md](README.md) file.

## Running all quickstarts in a single command

To run the quickstarts:

1. set `WORKSPACE` (to the root of the quickstart checkout)
2. set `JBOSSAS_IP_ADDR` (default is `localhost`)
3. set `JBOSS_HOME` (to the path of WildFly server, you can download the server at http://wildfly.org/downloads)
4. mvn clean install

_NOTE:_
One of the BlackTie quickstarts requires the Oracle driver to be downloaded and configured,
see [blacktie/test/initializeBlackTie.xml](blacktie/test/initializeBlackTie.xml) for more details.

It is disabled by default but running `./blacktie/run_all_quickstarts.[sh|bat] tx` will execute it.
