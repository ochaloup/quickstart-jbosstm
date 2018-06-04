/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2018, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.narayana.quickstarts.cmr.arquillian;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Enumeration;
import java.util.logging.Logger;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.event.container.AfterDeploy;
import org.jboss.arquillian.container.spi.event.container.BeforeStart;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jboss.as.arquillian.container.ManagementClient;
import org.wildfly.extras.creaper.core.online.FailuresAllowedBlock;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public class H2TestsuiteSetupObserver {
    private static final Logger log = Logger.getLogger(H2TestsuiteSetupObserver.class.getName());

    @Inject
    private Instance<ManagementClient> arqMgmtClient;

    public void handleBeforeSuite(@Observes(precedence = Integer.MAX_VALUE) BeforeSuite event) throws Exception {
        System.out.println(">>>>> what the fuck suite");
        log.severe(">>>>>>>>>>>>>>>>>>>>> before suite");
    }

    public void handleAfterSuite(@Observes AfterSuite event) throws Exception {
        System.out.println(">>>>> what the fuck after s");
        log.info(">>>>>>>>>>>>>>>>>>>>> after suite");
    }

    public synchronized void handleBeforeSetup(@Observes BeforeStart event, Container container) throws Exception {
        System.out.println(">>>>> what the fuck befor start");
        log.info(">>>>>>>>>>>>>>>>>>>>> before setup");
    }
    
    public synchronized void handleAfterDeployment(@Observes AfterDeploy event, Container container) throws Exception {
    	System.out.println(">>>>> what the fuck after deploy: " + container);

    	try {
    	    System.out.println("Absolute path:" + new File(".").getAbsolutePath());
    	    System.out.println("Class Resource: " + getClass().getResource("/"));
    	    Enumeration<URL> classClassLoaderResources = getClass().getClassLoader().getResources("");
    	    while (classClassLoaderResources.hasMoreElements())
    	        System.out.println("ClassLoader Resource: " + classClassLoaderResources.nextElement());
    	    Enumeration<URL> threadContextClassLoaderResources = Thread.currentThread().getContextClassLoader().getResources("");
    	    while (threadContextClassLoaderResources.hasMoreElements())
    	        System.out.println("ThreadContextLoader Resource: " + threadContextClassLoaderResources.nextElement());
    	    Enumeration<URL> systemClassLoaderResources = ClassLoader.getSystemClassLoader().getResources("");
    	    while (systemClassLoaderResources.hasMoreElements())
    	        System.out.println("SystemClassLoader Resource: " + systemClassLoaderResources.nextElement());
    	} catch (Exception e) {
    	    System.err.println("Can't get data about paths");
    	}

    	log.info(">>>>>>>>>>>>>>>>>>>>> after deploy");
    	OnlineManagementClient creaper = org.wildfly.extras.creaper.core.ManagementClient.online(
    	    OnlineOptions.standalone().hostAndPort(arqMgmtClient.get().getMgmtAddress(), arqMgmtClient.get().getMgmtPort())
    	    .build()
    	);
    	FailuresAllowedBlock allowedBlock = creaper.allowFailures();
    	try {
    	    creaper.execute("/subsystem=transactions/commit-markable-resource=\"java:jboss/datasources/jdbc-cmr\":add()");
    	    Administration admin = new Administration(creaper);
    	    admin.reload();
    	} finally {
    	    if(allowedBlock != null) allowedBlock.close();
    	    if(creaper !=null) creaper.close();
    	}

    	try(Connection conn = DriverManager.getConnection("jdbc:h2:mem:cmrdatasource")) {
    	    conn.createStatement().executeUpdate("CREATE TABLE xids (xid bytea, transactionManagerID varchar(64), actionuid bytea)");
    	    conn.createStatement().executeUpdate("CREATE UNIQUE INDEX index_xid ON xids (xid)");
    	}
    }
}
