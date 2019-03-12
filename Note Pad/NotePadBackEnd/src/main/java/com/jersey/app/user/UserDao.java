package com.jersey.app.user;

import java.sql.SQLException;
import java.util.List;

public interface UserDao {
    public List<User> getAllUsers() throws SQLException;
    public User getUser(int id) throws SQLException;
    public void createUser(User user) throws SQLException;
    public void updateUser(User user) throws SQLException;
    public void deleteUser(int id) throws SQLException;
    public void deleteAllUsers() throws SQLException;
    public int login(String username, String password) throws  SQLException;
}
