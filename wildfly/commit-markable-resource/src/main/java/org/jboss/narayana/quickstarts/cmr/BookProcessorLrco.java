package org.jboss.narayana.quickstarts.cmr;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@RequestScoped
@Named
public class BookProcessorLrco extends BookProcessor {

    @PersistenceContext(unitName = "jdbc-datasource")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /*
    public Integer fileBook(String title) {
        return super.fileBook(title);
    }
    */
}
