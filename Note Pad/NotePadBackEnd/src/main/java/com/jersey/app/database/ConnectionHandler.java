package com.jersey.app.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionHandler {

    Connection con = null;

    public Connection getConnection() {

        String dbURL = "jdbc:mysql://localhost:3306/jxtest?autoReconnect=true&useSSL=false";
        String dbUser = "jxb";
        String dbPassword = "jxbpw";

        try {
            if (con == null) {
                Class.forName("com.mysql.jdbc.Driver");
                con = DriverManager.getConnection(dbURL, dbUser, dbPassword);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            throw new RuntimeException(e);
        }

        return con;

    }

    public void closeConnection() {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                //e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }
}
