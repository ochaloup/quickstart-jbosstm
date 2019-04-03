/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.narayana.quickstarts.jta;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;

import org.jboss.weld.bootstrap.event.WeldAfterBeanDiscovery;
import org.jboss.weld.environment.se.ContainerLifecycleObserver;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.arjuna.ats.jta.cdi.Bean;
import com.arjuna.ats.jta.common.jtaPropertyManager;

/**
 * <p>
 * This test case shows how to initiate the Narayana transaction manager
 * when used with the CDI standalone container.
 * </p>
 */
public class CDIBindingTestCase {

    private Weld weld;
    private TransactionManager transactionManager;

    private RequiredCounterManager requiredCounterManager;
    private MandatoryCounterManager mandatoryCounterManager;
    private LifeCycleCounter lifeCycleCounter;

    @Before
    public void before() throws Exception {
        // Initialize Weld container
        weld = new Weld();
        ContainerLifecycleObserver<WeldAfterBeanDiscovery> afterBeanDiscovery =
            ContainerLifecycleObserver.afterBeanDiscovery().priority(999).notify( afterDiscovery -> {
            /*    afterDiscovery.addBean()
                    .scope(ApplicationScoped.class)
                    .types(TransactionManager.class)
                    .id("Programatic " + TransactionManager.class)
                    .createWith(tm -> com.arjuna.ats.jta.TransactionManager.transactionManager());
                    */
                afterDiscovery.addBean()
                    .scope(ApplicationScoped.class)
                    .types(TransactionSynchronizationRegistry.class)
                    .id("Programatic " + TransactionSynchronizationRegistry.class)
                    .createWith(tsr -> jtaPropertyManager.getJTAEnvironmentBean().getTransactionSynchronizationRegistry());
                });
        weld.addContainerLifecycleObserver(afterBeanDiscovery);
        final WeldContainer weldContainer = weld.initialize();

        Set<javax.enterprise.inject.spi.Bean<?>> beans = weldContainer.getBeanManager().getBeans(TransactionSynchronizationRegistry.class);
        System.out.println(beans);

        // Bootstrap the beans
        requiredCounterManager = weldContainer.select(RequiredCounterManager.class).get();
        mandatoryCounterManager = weldContainer.select(MandatoryCounterManager.class).get();

        lifeCycleCounter = weldContainer.select(LifeCycleCounter.class).get();
        lifeCycleCounter.clear();

        transactionManager = weldContainer.select(TransactionManager.class).get();
    }

    @After
    public void after() throws SystemException {
        // cleaning the transaction state in case of an error
        if(transactionManager.getTransaction().getStatus() == Status.STATUS_ACTIVE) {
            try {
                transactionManager.rollback();
            } catch (final Throwable ignored) {
            }
        }

        weld.shutdown();
    }

    @Test
    public void testTransactionScoped() throws Exception {
        transactionManager.begin();
        Assert.assertEquals(0, requiredCounterManager.getCounter());
        Assert.assertEquals(0, mandatoryCounterManager.getCounter());
        requiredCounterManager.incrementCounter();
        Assert.assertEquals(1, requiredCounterManager.getCounter());
        Assert.assertEquals(1, mandatoryCounterManager.getCounter());

        Assert.assertTrue(lifeCycleCounter.containsEvent("RequiredCounterManager.*Initialized"));
        Assert.assertTrue(lifeCycleCounter.containsEvent("MandatoryCounterManager.*Initialized"));
        Assert.assertEquals(2, lifeCycleCounter.getEvents().size());

        final Transaction suspendedTransaction = transactionManager.suspend();

        transactionManager.begin();
        Assert.assertEquals(0, requiredCounterManager.getCounter());
        Assert.assertEquals(0, mandatoryCounterManager.getCounter());
        mandatoryCounterManager.incrementCounter();
        Assert.assertEquals(1, requiredCounterManager.getCounter());
        Assert.assertEquals(1, mandatoryCounterManager.getCounter());

        transactionManager.rollback();
        transactionManager.resume(suspendedTransaction);
        transactionManager.rollback();

        // @Initialized of both beans called twice, @Destroy on only one bean twice
        Assert.assertEquals(6, lifeCycleCounter.getEvents().size());
        Assert.assertTrue(lifeCycleCounter.containsEvent("RequiredCounterManager.*Destroy"));
        Assert.assertFalse(lifeCycleCounter.containsEvent("MandatoryCounterManager.*Destroy"));
    }

}
