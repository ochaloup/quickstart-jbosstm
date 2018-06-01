/*
 * JBoss, Home of Professional Open Source
 * Copyright 2018, Red Hat, Inc. and/or its affiliates, and individual
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

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.jboss.logging.Logger;

/**
 * Maintaining books in the system.
 */
@Dependent
@Named
public class BookProcessor {
    private static final Logger log = Logger.getLogger(BookProcessor.class);

    @PersistenceContext(unitName = "jdbc-datasource")
    private EntityManager entityManager;

    @Inject
    private MessageHandler messageHandler;

    public static String textOfMessage(int id, String title) {
        return id + "#" + title;
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public List<BookEntity> getBooks() {
        final Query query = entityManager.createQuery("select book from " + BookEntity.class.getSimpleName() + " book");
        return (List<BookEntity>) query.getResultList();
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public Integer fileBook(String title) {
        if(title == null) throw new NullPointerException("title");

        BookEntity book = new BookEntity().setTitle(title);
        Integer id = this.save(book);

        messageHandler.send(textOfMessage(id, title));

        log.infof("New book was filed as id: %s, title: %s", id, title);
        return id;
    }

    @Transactional(Transactional.TxType.MANDATORY)
    private Integer save(BookEntity book) {
        if (book.isTransient()) {
            entityManager.persist(book);
        } else {
            entityManager.merge(book);
        }
        return book.getId();
    }
}
