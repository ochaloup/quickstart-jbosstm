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

import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionalException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.quickstarts.cmr.BookProcessor;
import org.jboss.narayana.quickstarts.cmr.BookEntity;
import org.jboss.narayana.quickstarts.cmr.MessageHandler;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

// TODO: https://stackoverflow.com/questions/29183503/start-h2-database-programmatically/29184321
@RunWith(Arquillian.class)
public class TestCase {

    @Inject
    private MessageHandler quickstartQueue;

    @Inject
    private BookProcessor quickstartEntityRepository;


    private TransactionManager transactionManager;

    @Deployment
    public static WebArchive createTestArchive() {
        WebArchive wa = ShrinkWrap.create(WebArchive.class, "cmr.war")
            .addPackages(true, BookEntity.class.getPackage().getName())
            .addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")
            .addAsWebInfResource("webapp/WEB-INF/jdbc-ds.xml", "jdbc-ds.xml")
            .addAsWebInfResource("webapp/WEB-INF/test-jms.xml", "test-jms.xml")
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        System.out.printf(">>>>>>> webarchive content:%n%s%n", wa.toString(true));
        return wa;
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
        final int entitiesCountBefore = quickstartEntityRepository.getBooks().size();

        transactionManager.begin();
        quickstartEntityRepository.fileBook("test");
        quickstartQueue.send("testCommit");
        transactionManager.commit();

        Assert.assertEquals(entitiesCountBefore + 1, quickstartEntityRepository.getBooks().size());
        Assert.assertEquals("testCommit", quickstartQueue.get());
    }

    @Test
    public void testRollback() throws Exception {
        final int entitiesCountBefore = quickstartEntityRepository.getBooks().size();

        transactionManager.begin();
        quickstartEntityRepository.fileBook("test");
        quickstartQueue.send("testRollback");
        transactionManager.rollback();

        Assert.assertEquals(entitiesCountBefore, quickstartEntityRepository.getBooks().size());
        Assert.assertEquals("", quickstartQueue.get());
    }

    @Test(expected = TransactionalException.class)
    public void testWithoutTransaction() {
        quickstartEntityRepository.fileBook("test");
    }

}
