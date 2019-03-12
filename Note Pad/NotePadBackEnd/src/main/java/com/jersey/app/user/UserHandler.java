package com.jersey.app.user;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jersey.app.database.ConnectionHandler;

import java.sql.Connection;
import java.sql.SQLException;

public class UserHandler {

    public UserHandler(){}

    public String createUser(final String input) throws SQLException {

        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(input).getAsJsonObject();

        ConnectionHandler conHandler = new ConnectionHandler();
        Connection con = conHandler.getConnection();

        // Create instance of user DAO
        UserDaoImpl userDAO = new UserDaoImpl(con);

        // Create instance of user object
        User user = new User();

        // Set user details before creating them
        user.setFirstname(jsonObject.get("firstname").getAsString());
        user.setLastname(jsonObject.get("lastname").getAsString());
        user.setPhone(jsonObject.get("phone").getAsString());
        user.setEmail(jsonObject.get("email").getAsString());
        user.setUsername(jsonObject.get("username").getAsString());
        user.setPassword(jsonObject.get("password").getAsString());

        System.out.println("USernmae = " + user.getUsername());
        System.out.println("USerPw = " + user.getPassword());

        // Save user in DB
        userDAO.createUser(user);

        // Close Connection
        conHandler.closeConnection();

        return "{\"result\": \"OK\"}";
        //return "[{}, {\"result\": \"OK\"}]";
    }



    public String editUser(final String input) throws SQLException {

        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(input).getAsJsonObject();

        ConnectionHandler conHandler = new ConnectionHandler();
        Connection con = conHandler.getConnection();

        // Create instance of user DAO
        UserDaoImpl userDAO = new UserDaoImpl(con);

        // Create instance of user object
        User user = new User();

        // Set user details before creating them
        user.setId(jsonObject.get("id").getAsInt());
        user.setFirstname(jsonObject.get("firstname").getAsString());
        user.setLastname(jsonObject.get("lastname").getAsString());
        user.setPhone(jsonObject.get("phone").getAsString());
        user.setEmail(jsonObject.get("email").getAsString());

        userDAO.updateUser(user);

        // Close Connection
        conHandler.closeConnection();


        return "{\"result\": \"OK\"}";
        //return "[{}, {\"result\": \"OK\"}]";
    }


    public String deleteUser(final String input) throws SQLException {

        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(input).getAsJsonObject();

        ConnectionHandler conHandler = new ConnectionHandler();
        Connection con = conHandler.getConnection();

        // Create instance of user DAO
        UserDaoImpl userDAO = new UserDaoImpl(con);


        // Delete user with ID = 1
        userDAO.deleteUser(jsonObject.get("id").getAsInt());

        // Close Connection
        conHandler.closeConnection();

        return "{\"result\": \"OK\"}";
        //return "[{}, {\"result\": \"OK\"}]";
    }

    public String loginUser(final String input) throws SQLException {

        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(input).getAsJsonObject();

        System.out.println("INPUT = " + input);

        ConnectionHandler conHandler = new ConnectionHandler();
        Connection con = conHandler.getConnection();

        // Create instance of user DAO
        UserDaoImpl userDAO = new UserDaoImpl(con);


        // Delete user with ID = 1
        int res = userDAO.login(jsonObject.get("user_id").getAsString(), jsonObject.get("user_pw").getAsString());

        // Close Connection
        conHandler.closeConnection();

        String response = "{\"result\": \"Error\"}";
        //String response = "[{}, {\"result\": \"Error\"}]";

        if(res != -1){
            response = "{\"result\": \"OK\", \"user_id\": " + res + "}";
            //response = "[{}, {\"result\": \"OK\", \"user_id\": " + res + "}]";
        }

        return response;
    }
}
