/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.narayana.quickstarts.cmr;


import java.util.Optional;

import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Status;
import javax.transaction.TransactionManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.extension.byteman.api.BMRule;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.narayana.quickstarts.cmr.arquillian.ArquillianExtension;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.FailuresAllowedBlock;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

// TODO: https://stackoverflow.com/questions/29183503/start-h2-database-programmatically/29184321
@RunWith(Arquillian.class)
@ServerSetup(value = CmrLrcoTestCase.ServerCmrSetup.class)
public class CmrLrcoTestCase {

    public static class ServerCmrSetup implements ServerSetupTask {
        @Override
        public void setup(ManagementClient managementClient, String containerId) throws Exception {
            OnlineManagementClient creaper = org.wildfly.extras.creaper.core.ManagementClient.online(
                OnlineOptions.standalone().wrap(managementClient.getControllerClient()));

            try(FailuresAllowedBlock allowedBlock = creaper.allowFailures()) {
                creaper.execute("/subsystem=transactions/commit-markable-resource=\"java:jboss/datasources/jdbc-cmr\":add()");
            }
            new Administration(creaper).reload();
        }

        @Override
        public void tearDown(ManagementClient managementClient, String containerId) throws Exception {
            OnlineManagementClient creaper = org.wildfly.extras.creaper.core.ManagementClient.online(
                    OnlineOptions.standalone().wrap(managementClient.getControllerClient()));

            try(FailuresAllowedBlock allowedBlock = creaper.allowFailures()) {
                creaper.execute("/subsystem=transactions/commit-markable-resource=\"java:jboss/datasources/jdbc-cmr\":remove()");
            }
        }
    }


    @Inject
    private MessageHandler messageHandler;

    @Inject
    private BookProcessor bookRepository;

    private TransactionManager transactionManager;

    @Deployment
    public static WebArchive createTestArchive() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "cmr.war")
            .addPackages(true, BookEntity.class.getPackage().getName())
            .deletePackage(ArquillianExtension.class.getPackage())
            .addClass(ServerSetupTask.class)
            .addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml")
            .addAsResource("META-INF/cmr-create-script.sql", "META-INF/cmr-create-script.sql")
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        war.merge(ShrinkWrap.create(GenericArchive.class).as(ExplodedImporter.class)  
            .importDirectory("src/main/webapp").as(GenericArchive.class),  
            "/", Filters.includeAll());

        System.out.printf(">>>>>>> webarchive content:%n%s%n", war.toString(true));
        return war;
    }

    @Before
    public void before() throws NamingException {
        transactionManager = (TransactionManager) new InitialContext().lookup("java:/jboss/TransactionManager");
    }

    @After
    public void after() {
        try {
            transactionManager.rollback();
        } catch (final Throwable t) {
        }
    }

    @Test
    public void testCommit() throws Exception {
        final int entitiesCountBefore = bookRepository.getBooks().size();

        transactionManager.begin();
        int bookId = bookRepository.fileBook("test");
        transactionManager.commit();

        Assert.assertEquals("A new book should be filed",
            entitiesCountBefore + 1, bookRepository.getBooks().size());
        Optional<String> queueMessage = messageHandler.get();
        Assert.assertTrue("Expecting transaction being committed and message delivered", queueMessage.isPresent());
        Assert.assertEquals("The transaction was committed thus the inform message is expected to be received",
            BookProcessor.textOfMessage(bookId, "test"), queueMessage.get());
    }

    @Test
    public void testRollback() throws Exception {
        final int entitiesCountBefore = bookRepository.getBooks().size();

        transactionManager.begin();
        bookRepository.fileBook("test");
        transactionManager.rollback();

        Assert.assertEquals("Book filing was canceled no new book expected",
            entitiesCountBefore, bookRepository.getBooks().size());
        Assert.assertFalse("Sending the message was rolled back. No message expected.",
            messageHandler.get().isPresent());
    }


    @Test
    @BMRule(
        name = "Throw exception before prepare being finished",
        condition = "NOT flagged(\"lrcoflag\")",
        targetClass = "com.arjuna.ats.arjuna.coordinator.BasicAction", targetMethod = "save_state",
        action = "flag(\"lrcoflag\"), throw new java.lang.RuntimeException(\"byteman rules\")")
    public void testLrcoFailure() throws Exception {
        final int entitiesCountBefore = bookRepository.getBooks().size();

        transactionManager.begin();
        int bookId = bookRepository.fileBook("test");
        try {
        	transactionManager.commit();
        } catch (Exception re) {
        	if(transactionManager.getStatus() == Status.STATUS_ACTIVE)
        		transactionManager.rollback();
			if(!re.getMessage().equals("byteman rules"))
			    throw new IllegalStateException("test failed, not expected exception caught", re);
			// else: ignore as expected
		}

        Assert.assertEquals("A new book should be filed",
            entitiesCountBefore + 1, bookRepository.getBooks().size());
        Assert.assertEquals("The transaction was committed thus the inform message is expected to be received",
            BookProcessor.textOfMessage(bookId, "test"), messageHandler.get().get());
    }

}
