package com.itcrazy.mybatis.generator.util;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * @author: dengxin.chen
 * @version: $ ConnectionManager.java,v0.1 2024-09-30 17:15 dengxin.chen Exp $
 * @description:
 */
public class ConnectionManager {

    private static final String DB_URL = "jdbc:sqlite:./config/sqlite3.db";

    public static Connection getConnection() throws Exception {
        Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection(DB_URL);
        return conn;
    }
}
