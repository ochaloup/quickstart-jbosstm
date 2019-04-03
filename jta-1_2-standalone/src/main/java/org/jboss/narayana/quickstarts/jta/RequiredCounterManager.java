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

import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionScoped;
import javax.transaction.Transactional;

import org.jboss.logging.Logger;

/**
 * <p>
 * A class with definition of the
 * {@link Transactional.TxType#REQUIRED} transactional boundary
 * for one particular method {@link #isTransactionAvailable()}.
 * </p>
 * <p>
 * If the method is invoked with a transactional context being
 * available the method joins the context.
 * If there is no context available a new transactional context
 * is created (a new transaction is started) before the method
 * code is executed.
 * </p>
 * <p>
 * The class demonstrates the usage of the {@link TransactionScoped} events.
 * A method can observe the {@link Initialized} and {@link Destroyed} events.
 * </p>
 * 
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class RequiredCounterManager {
    private static final Logger LOG = Logger.getLogger(RequiredCounterManager.class);

    @Inject
    private Counter counter;

    @Inject
    private LifeCycleCounter lifeCycle;

    @Inject
    private TransactionManager transactionManager;

    @Transactional
    public boolean isTransactionAvailable() {
        try {
            return transactionManager.getTransaction().getStatus() == Status.STATUS_ACTIVE;
        } catch (SystemException se) {
            throw new IllegalStateException("Transaction manager " + transactionManager
                    + " is not capable to provide transaction status");
        }
    }

    public int getCounter() {
        return counter.get();
    }

    public void incrementCounter() {
        counter.increment();
    }

    void transactionScopeActivated(@Observes @Initialized(TransactionScoped.class) final Object event, final BeanManager beanManager) {
        lifeCycle.addEvent(this.getClass().getSimpleName() + "_" + Initialized.class.getSimpleName());
    }

    void transactionScopeDestroyed(@Observes @Destroyed(TransactionScoped.class) final Object event, final BeanManager beanManager) {
        try {
            lifeCycle.addEvent(this.getClass().getSimpleName() + "_" + Destroyed.class.getSimpleName());
        } catch (Exception e) {
            LOG.trace("This is the expected situation."
                    + "The context was destroyed the @Transactional scope is not available at this time.");
        }
    }
}
