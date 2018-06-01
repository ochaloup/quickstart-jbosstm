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

// TODO: https://stackoverflow.com/questions/29183503/start-h2-database-programmatically/29184321
@RunWith(Arquillian.class)
public class CmrLrcoTestCase {

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
            .addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml")
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        war.merge(ShrinkWrap.create(GenericArchive.class).as(ExplodedImporter.class)  
            .importDirectory("src/main/webapp").as(GenericArchive.class),  
            "/", Filters.includeAll());

        System.out.printf(">>>>>>> webarchive content:%n%s%n", war.toString(true));
        return war;
    }

    @Test
    public void testCommit() throws Exception {
        final int entitiesCountBefore = bookRepository.getBooks().size();

        int bookId = bookRepository.fileBook("test");

        Assert.assertEquals(entitiesCountBefore + 1, bookRepository.getBooks().size());
        Assert.assertEquals(BookProcessor.textOfMessage(bookId, "test"), messageHandler.get());
    }

    @Test
    public void testRollback() throws Exception {
        final int entitiesCountBefore = bookRepository.getBooks().size();

        bookRepository.fileBook("test");

        Assert.assertEquals(entitiesCountBefore, bookRepository.getBooks().size());
        Assert.assertEquals("", messageHandler.get());
    }

    @Test(expected = TransactionalException.class)
    public void testWithoutTransaction() {
        bookRepository.fileBook("test");
    }

}
