package org.jboss.narayana.quickstarts.jta;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;
import javax.transaction.TransactionSynchronizationRegistry;

import com.arjuna.ats.jta.common.jtaPropertyManager;

@Alternative
public class SynchronizationProducer {

    @Produces
    @ApplicationScoped
    public TransactionSynchronizationRegistry produceSynchroRegistry() {
        return jtaPropertyManager.getJTAEnvironmentBean().getTransactionSynchronizationRegistry();
    }

}
