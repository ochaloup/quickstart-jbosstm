package org.jboss.narayana.quickstarts.cmr;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@RequestScoped
@Named
public class BookProcessorCmr extends BookProcessor {

    @PersistenceContext(unitName = "jdbc-cmr-datasource")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}
