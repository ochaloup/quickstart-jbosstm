/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package io.narayana;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Properties;

import javax.transaction.TransactionManager;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.jdbc.drivers.PropertyFileDynamicClass;
import com.arjuna.ats.jdbc.TransactionalDriver;

import io.narayana.util.DBUtils;
import io.narayana.util.FailingXAResource;

/**
 * <p>
 * Usage of {@link TransactionalDriver} managing the transactionality over jdbc connection where
 * the <code>DirectRecoverableConnection</code> implementation is used beneath.
 * <p>
 * See more details on driver settings at {@link DriverDirectRecoverable}.
 * <p>
 */
public class DriverDirectRecoverableRecovery {

    public void process() throws Exception {
           DriverManager.registerDriver(DBUtils.TXN_DRIVER_INSTANCE);

           // starting recovery manager
           // settings - recovery modules, filters, timeouts etc. is taken from jbossts-properties.xml descriptor
           RecoveryManager.manager();

           //jdbc:arjuna: <path to properties file>
        String jdbcUrl1 = TransactionalDriver.arjunaDriver + "target/classes/ds1.properties";
        Properties props1 = new Properties();
        props1.put(TransactionalDriver.dynamicClass, PropertyFileDynamicClass.class.getName());
        props1.put(TransactionalDriver.userName, "");
        props1.put(TransactionalDriver.password, "");
        Connection conn1 = DriverManager.getConnection(jdbcUrl1, props1);

        TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
        tm.begin();

        // enlisting XAResource which then cause txn to fail and recovery manager to have work to resolve
        tm.getTransaction().enlistResource(new FailingXAResource());

        PreparedStatement ps1 = conn1.prepareStatement(DBUtils.INSERT_STATEMENT);
        ps1.setInt(1, 1);
        ps1.setString(2, "Narayana");

        ps1.executeUpdate();
        tm.commit();
    }

}
