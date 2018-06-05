package org.jboss.narayana.quickstarts.cmr;

import javax.enterprise.context.Dependent;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Dependent
@Named
public class BookProcessorCmr extends BookProcessor {

    @PersistenceContext(unitName = "jdbc-cmr-datasource")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}
