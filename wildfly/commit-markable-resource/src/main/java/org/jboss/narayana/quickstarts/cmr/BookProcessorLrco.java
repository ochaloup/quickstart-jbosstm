package org.jboss.narayana.quickstarts.cmr;

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

@Dependent
@Named
@Transactional
public class BookProcessorLrco extends BookProcessor {

    @PersistenceContext(unitName = "jdbc-datasource")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public Integer fileBook(String title) {
        return super.fileBook(title);
    }

    public BookEntity getBookById(int id) {
        return super.getBookById(id);
    }

    public List<BookEntity> getBooks() {
        return super.getBooks();
    }
}
