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
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Properties;

import javax.naming.Context;

import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.jdbc.common.jdbcPropertyManager;
import com.arjuna.ats.jta.common.jtaPropertyManager;

import io.narayana.util.CodeUtils;
import io.narayana.util.DBUtils;
import io.narayana.util.TestInitialContextFactory;

/**
 * Tests running commit and rollback scenarios for showcases
 * of managing database connections with use of the Narayana transaction driver.
 */
@RunWith(BMUnitRunner.class)
public class TransactionalDriverTest {
    Connection conn1, conn2;

    @Before
    public void setUp() {
        conn1 = DBUtils.getDBConnection(DBUtils.DB_1);
        conn2 = DBUtils.getDBConnection(DBUtils.DB_2);

        DBUtils.createTable(conn1);
        DBUtils.createTable(conn2);
    }

    @After
    public void tearDown() throws Exception {
        // cleaning recovery settings
        jtaPropertyManager.getJTAEnvironmentBean().setXaResourceRecoveryClassNames(null);
        // cleaning database
        Thread.sleep(10); // waiting for tables veing unlocked
        DBUtils.dropTable(conn1);
        DBUtils.dropTable(conn2);
        // closing connections
        CodeUtils.closeMultiple(conn1, conn2);
    }

    @Test
    public void localTxnCommit() throws Exception {
        new JdbcLocalTransaction().process(() -> {});

        ResultSet rs1 = DBUtils.select(conn1);
        ResultSet rs2 = DBUtils.select(conn2);
        Assert.assertTrue("First database does not contain data as expected", rs1.next());
        Assert.assertTrue("Second database does not contain data as expected", rs2.next());
    }

    @Test
    public void localTxnRollback() throws Exception {
        try {
            new JdbcLocalTransaction().process(() -> {throw new RuntimeException("expected");});
        } catch (Exception e) {
            checkcException(e);
        }

        ResultSet rs1 = DBUtils.select(conn1);
        ResultSet rs2 = DBUtils.select(conn2);
        Assert.assertTrue("First database does not contain data as expected", rs1.next());
        Assert.assertFalse("Second database contain data which is not expected", rs2.next());
    }

    @Test
    public void transactionManagerCommit() throws Exception {
        new ManagedTransaction().process(() -> {});

        ResultSet rs1 = DBUtils.select(conn1);
        ResultSet rs2 = DBUtils.select(conn2);
        Assert.assertTrue("First database does not contain data as expected to be commited", rs1.next());
        Assert.assertTrue("Second database does not contain data as expected to be commited", rs2.next());
    }

    @Test
    public void transactionManagerRollback() throws Exception {
        try {
            new ManagedTransaction().process(() -> {throw new RuntimeException("expected");});
        } catch (Exception e) {
            checkcException(e);
        }

        ResultSet rs1 = DBUtils.select(conn1);
        ResultSet rs2 = DBUtils.select(conn2);
        Assert.assertFalse("First database contains data which is not expected as rolled-back", rs1.next());
        Assert.assertFalse("Second database contains data which is not expected as rolled-back", rs2.next());
    }

    @Test
    public void transactionDriverProvidedCommit() throws Exception {
        new DriverProvidedXADataSource().process(() -> {});

        ResultSet rs1 = DBUtils.select(conn1);
        ResultSet rs2 = DBUtils.select(conn2);
        Assert.assertTrue("First database does not contain data as expected to be commited", rs1.next());
        Assert.assertTrue("Second database does not contain data as expected to be commited", rs2.next());
    }

    @Test
    public void transactionDriverProvidedRollback() throws Exception {
        try {
            new DriverProvidedXADataSource().process(() -> {throw new RuntimeException("expected");});
        } catch (Exception e) {
            checkcException(e);
        }

        ResultSet rs1 = DBUtils.select(conn1);
        ResultSet rs2 = DBUtils.select(conn2);
        Assert.assertFalse("First database contains data which is not expected as rolled-back", rs1.next());
        Assert.assertFalse("Second database contains data which is not expected as rolled-back", rs2.next());
    }

    @Test
    public void transactionDriverIndirectCommit() throws Exception {
        new DriverIndirectRecoverable().process(() -> {});

        ResultSet rs1 = DBUtils.select(conn1);
        ResultSet rs2 = DBUtils.select(conn2);
        Assert.assertTrue("First database does not contain data as expected to be commited", rs1.next());
        Assert.assertTrue("Second database does not contain data as expected to be commited", rs2.next());
    }

    @Test
    public void transactionDriverIndirectRollback() throws Exception {
        try {
            new DriverIndirectRecoverable().process(() -> {throw new RuntimeException("expected");});
        } catch (Exception e) {
            checkcException(e);
        }

        ResultSet rs1 = DBUtils.select(conn1);
        ResultSet rs2 = DBUtils.select(conn2);
        Assert.assertFalse("First database contains data which is not expected as rolled-back", rs1.next());
        Assert.assertFalse("Second database contains data which is not expected as rolled-back", rs2.next());
    }

    @Test
    public void transactionDriverDirectRecoverableCommit() throws Exception {
        new DriverDirectRecoverable().process(() -> {});

        ResultSet rs1 = DBUtils.select(conn1);
        ResultSet rs2 = DBUtils.select(conn2);
        Assert.assertTrue("First database does not contain data as expected to be commited", rs1.next());
        Assert.assertTrue("Second database does not contain data as expected to be commited", rs2.next());
    }

    @Test
    public void transactionDriverDirectRecoverableRollback() throws Exception {
        try {
            new DriverDirectRecoverable().process(() -> {throw new RuntimeException("expected");});
        } catch (Exception e) {
            checkcException(e);
        }

        ResultSet rs1 = DBUtils.select(conn1);
        ResultSet rs2 = DBUtils.select(conn2);
        Assert.assertFalse("First database contains data which is not expected as rolled-back", rs1.next());
        Assert.assertFalse("Second database contains data which is not expected as rolled-back", rs2.next());
    }

    @BMRule(
        name = "Fail on first commit call to XAResource",
        targetClass = "^javax.transaction.xa.XAResource",
        isInterface = true,
        targetMethod = "commit",
        targetLocation = "AT ENTRY",
        condition = "NOT flagged(\"is_failed\")",
        action = "System.out.println(\"Failing by Byteman rule\");" +
                 "flag(\"is_failed\");" +
                 "throw new XAException(XAException.XAER_RMFAIL);"
    )
    @Test
    public void transactionDriverDirectRecoverableRecovery() throws Exception {
        // starting recovery manager
        // settings - recovery modules, filters, timeouts etc. is taken from jbossts-properties.xml descriptor
        RecoveryManager manager = RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT);
        manager.initialize();

        new DriverDirectRecoverable().process(() -> {});

        manager.scan();

        ResultSet rs1 = DBUtils.select(conn1);
        Assert.assertTrue("First database does not contain data as expected to be commited", rs1.next());
    }
    
    @BMRule(
        name = "Fail on first commit call to XAResource",
        targetClass = "^javax.transaction.xa.XAResource",
        isInterface = true,
        targetMethod = "commit",
        targetLocation = "AT ENTRY",
        condition = "NOT flagged(\"is_failed\")",
        action = "System.out.println(\"Failing by Byteman rule\");" +
                 "flag(\"is_failed\");" +
                 "throw new XAException(XAException.XAER_RMFAIL);"
    )
    @Test
    public void transactionDriverIndirectRecoverableRecovery() throws Exception {
        // starting recovery manager
        // settings - recovery modules, filters, timeouts etc. is taken from jbossts-properties.xml descriptor
        jtaPropertyManager.getJTAEnvironmentBean().setXaResourceRecoveryClassNames(Arrays.asList(
            "com.arjuna.ats.internal.jdbc.recovery.JDBCXARecovery;target/classes/recovery-jdbcxa-test1.xml"
        ));
        Properties initProps = new Properties();
        initProps.setProperty(Context.INITIAL_CONTEXT_FACTORY, TestInitialContextFactory.class.getName());
        jdbcPropertyManager.getJDBCEnvironmentBean().setJndiProperties(initProps);
        RecoveryManager manager = RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT);
        manager.initialize();

        new DriverIndirectRecoverable().process(() -> {});

        manager.scan();
        manager.terminate(false);

        ResultSet rs1 = DBUtils.select(conn1);
        Assert.assertTrue("First database does not contain data as expected to be commited", rs1.next());
    }

    private void checkcException(Exception e) {
        if (!e.getMessage().toLowerCase().contains("expected"))
            Assert.fail("Exception message does not contain 'expected' but it's '"
                + e.getClass().getName() + ":" + e.getMessage() + "'");
    }
}
