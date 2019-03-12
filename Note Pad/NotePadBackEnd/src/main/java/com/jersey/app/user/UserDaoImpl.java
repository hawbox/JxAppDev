package com.jersey.app.user;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

public class UserDaoImpl implements UserDao {

    Connection con = null;

    private static final String createStmt = "INSERT INTO users(firstname, lastname, phone, email, username, password) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String loadStmt = "SELECT id, firstname, lastname, phone, email, username, password FROM users WHERE id = ?";
    private static final String loadAllStmt = "SELECT * FROM users";
    private static final String updateStmt = "UPDATE users SET firstname = ?, lastname = ?, phone = ?, email = ?, username = ?, password = ? WHERE id = ?";
    private static final String deleteStmt = "DELETE FROM users WHERE id = ?";
    private static final String deleteAllStmt = "DELETE FROM users";
    private static final String loginStatement = "SELECT id, firstname, lastname, phone, email, username, password FROM users WHERE username = ?";

    public UserDaoImpl(Connection con) {
        this.con = con;
    }

    @Override
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new LinkedList<User>();

        Statement statement = con.createStatement();
        ResultSet resultSet = statement.executeQuery(loadAllStmt);

        User user = null;

        while (resultSet.next()) {
            user = new User();

            user.setId(resultSet.getInt("id"));
            user.setFirstname(resultSet.getString("firstname"));
            user.setLastname(resultSet.getString("lastname"));
            user.setPhone(resultSet.getString("phone"));
            user.setEmail(resultSet.getString("email"));
            user.setUsername(resultSet.getString("username"));
            user.setPassword(resultSet.getString("password"));

            users.add(user);
        }

        resultSet.close();
        statement.close();
        return users;
    }

    @Override
    public User getUser(int id) throws SQLException {
        PreparedStatement ps = con.prepareStatement(loadStmt);
        User a = new User();
        ps.setInt(1, id);
        ResultSet result = ps.executeQuery();
        ps.close();

        if (!result.next()) return null;

        a.setId(result.getInt("id"));
        a.setFirstname(result.getString("firstname"));
        a.setLastname(result.getString("lastname"));
        a.setPhone(result.getString("phone"));
        a.setEmail(result.getString("email"));
        a.setUsername(result.getString("username"));
        a.setPassword(result.getString("password"));
        return a;
    }

    @Override
    public void createUser(User user) throws SQLException {
        PreparedStatement ps = con.prepareStatement(createStmt, Statement.RETURN_GENERATED_KEYS);

        ps.setString(1, user.getFirstname());
        ps.setString(2, user.getLastname());
        ps.setString(3, user.getPhone());
        ps.setString(4, user.getEmail());
        ps.setString(5, user.getUsername());
        ps.setString(6, user.getPassword());
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if(rs.next())
        {
            user.setId(rs.getInt(1));
        }

        ps.close();
    }

    @Override
    public void updateUser(User user) throws SQLException {
        PreparedStatement ps = con.prepareStatement(updateStmt);

        ps.setInt(1, user.getId());
        ps.setString(2, user.getFirstname());
        ps.setString(3, user.getLastname());
        ps.setString(4, user.getPhone());
        ps.setString(5, user.getEmail());
        ps.setString(6, user.getUsername());
        ps.setString(7, user.getPassword());

        ps.executeUpdate();
        ps.close();
    }

    @Override
    public void deleteUser(int id) throws SQLException {
        PreparedStatement ps = con.prepareStatement(deleteStmt);

        ps.setInt(1, id);

        ps.executeUpdate();
        ps.close();
    }

    @Override
    public void deleteAllUsers() throws SQLException {
        PreparedStatement ps = con.prepareStatement(deleteAllStmt);
        ps.executeUpdate();
        ps.close();
    }

    @Override
    public int login(String username, String password) throws SQLException {
        int id = -1;
        System.out.println("Username = "+ username);
        System.out.println("Password = "+ password);
        PreparedStatement ps = con.prepareStatement(loginStatement);
        User a = new User();
        ps.setString(1, username);
        ResultSet result = ps.executeQuery();

        if (result.next()){

            a.setId(result.getInt("id"));
            a.setFirstname(result.getString("firstname"));
            a.setLastname(result.getString("lastname"));
            a.setPhone(result.getString("phone"));
            a.setEmail(result.getString("email"));
            a.setEmail(result.getString("username"));
            a.setEmail(result.getString("password"));

            id = result.getInt("id");


            if(result.getString("password").equals(password)){
                ps.close();
                return id;
            }else{
                ps.close();
                return id;
            }
        }

        return id;
    }
}
