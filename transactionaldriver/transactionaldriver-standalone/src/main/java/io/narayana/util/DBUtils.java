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

package io.narayana.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.XADataSource;
import org.postgresql.xa.PGXADataSource;

import com.arjuna.ats.jdbc.TransactionalDriver;

/**
 * Utility class providing H2 connection
 * and operations on the test database table
 */
public class DBUtils {
    public static final java.sql.Driver TXN_DRIVER_INSTANCE = new TransactionalDriver();

    public static final String DB_1 = "crashrec";
    public static final String DB_2 = "crashrec2";

    private static final String DB_DRIVER = "org.postgresql.Driver";
    private static final String DB_CONNECTION = "jdbc:postgresql://localhost:5432/%s";

    private static String TEST_TABLE_NAME = "TXN_DRIVER_TEST";
    public static String CREATE_TABLE = String.format("CREATE TABLE %s(id int primary key, value varchar(42))", TEST_TABLE_NAME);
    public static String DROP_TABLE = String.format("DROP TABLE %s", TEST_TABLE_NAME);
    public static String INSERT_STATEMENT = String.format("INSERT INTO %s (id, value) values (?,?)", TEST_TABLE_NAME);
    public static String SELECT_QUERY = String.format("SELECT * FROM %s", TEST_TABLE_NAME);

    public static Connection getDBConnection(String dbname) {
        Connection dbConnection = null;
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        try {
            String dbConnectionUrl = String.format(DB_CONNECTION, dbname);
            dbConnection = DriverManager.getConnection(dbConnectionUrl, dbname, dbname);
            return dbConnection;
        } catch (SQLException e) {
            throw new IllegalStateException("Can't get connection to database '"
                + dbname + "' of connection '" + DB_CONNECTION + "'", e);
        }
    }

    public static XADataSource getXADatasource(String dbName) {
        PGXADataSource ds = new PGXADataSource();
        ds.setServerName("localhost");
        ds.setPortNumber(5432);
        ds.setDatabaseName(dbName);
        ds.setUser(dbName);
        ds.setPassword(dbName);
        return ds;
    }

    public static int createTable(Connection conn) {
        try {
            return conn.createStatement().executeUpdate(CREATE_TABLE);
        } catch (Exception e) {
            throw new RuntimeException("Can't create table by '" + CREATE_TABLE + "'", e);
        }
    }

    public static int dropTable(Connection conn) {
        try {
            return conn.createStatement().executeUpdate(DROP_TABLE);
        } catch (Exception e) {
            throw new RuntimeException("Can't drop table by '" + DROP_TABLE + "'", e);
        }
    }

    public static ResultSet select(Connection conn) {
        try {
            return conn.createStatement().executeQuery(SELECT_QUERY);
        } catch (SQLException e) {
            System.err.println("Cannot select by '" + SELECT_QUERY + "'");
            return null;
        }
    }
}
