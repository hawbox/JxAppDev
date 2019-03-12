package com.jersey.app.notes;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jersey.app.database.ConnectionHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class NoteHandler {


    public NoteHandler(){}

    public String createNote(final String input) throws SQLException {

        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(input).getAsJsonObject();

        ConnectionHandler conHandler = new ConnectionHandler();
        Connection con = conHandler.getConnection();

        // Create instance of user DAO
        NoteDaoImpl noteDAO = new NoteDaoImpl(con);

        // Create instance of user object
        Note note = new Note();

        // Set user details before creating them
        note.setNotename(jsonObject.get("notename").getAsString());
        note.setNote(jsonObject.get("note").getAsString());
        note.setUser_id(Integer.parseInt(jsonObject.get("user_id").getAsString()));
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        note.setDate(timestamp);

        // Save user in DB
        noteDAO.createNote(note);

        // Close Connection
        conHandler.closeConnection();

        return "{\"result\": \"OK\"}";
    }


    public String listNotes(final String input) throws SQLException {
        ConnectionHandler conHandler = new ConnectionHandler();
        Connection con = conHandler.getConnection();

        // Create instance of user DAO
        NoteDaoImpl noteDao = new NoteDaoImpl(con);

        // Display all Users
        List<Note> notes = noteDao.getAllNotes();

        // Close Connection
        conHandler.closeConnection();

        String json = new Gson().toJson(notes);

        return json;
    }

    public String listNotesByUser(final String input) throws  SQLException {

        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(input).getAsJsonObject();

        ConnectionHandler conHandler = new ConnectionHandler();
        Connection con = conHandler.getConnection();

        //Create instance of notes DAO
        NoteDaoImpl noteDao = new NoteDaoImpl(con);

        //Display all notes
        List<Note> notes = noteDao.getAllNotesByUser(Integer.parseInt(jsonObject.get("user_id").getAsString()));

        //Close connection
        conHandler.closeConnection();

        String json = new Gson().toJson(notes);

        return json;
    }

    public String editNote(final String input) throws SQLException {

        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(input).getAsJsonObject();

        ConnectionHandler conHandler = new ConnectionHandler();
        Connection con = conHandler.getConnection();

        // Create instance of user DAO
        NoteDaoImpl noteDAO = new NoteDaoImpl(con);

        // Create instance of user object
        Note note = new Note();

        // Set user details before creating them
        note.setId(jsonObject.get("id").getAsInt());
        note.setNotename(jsonObject.get("notename").getAsString());
        note.setNote(jsonObject.get("note").getAsString());
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        note.setDate(timestamp);

        noteDAO.updateNote(note);

        // Close Connection
        conHandler.closeConnection();

        //return "[{\"type\": \"notify\", \"group\": \"test\", \"notification\": \"refresh-table\"}, {\"result\": \"OK\"}]";
        return "{\"result\": \"OK\"}";
    }


    public String deleteNote(final String input) throws SQLException {

        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(input).getAsJsonObject();

        ConnectionHandler conHandler = new ConnectionHandler();
        Connection con = conHandler.getConnection();

        // Create instance of user DAO
        NoteDaoImpl noteDAO = new NoteDaoImpl(con);


        // Delete user with ID = 1
        noteDAO.deleteNote(jsonObject.get("id").getAsInt());

        // Close Connection
        conHandler.closeConnection();

        //return "[{\"type\": \"notify\", \"group\": \"test\", \"notification\": \"refresh-table\"}, {\"result\": \"OK\"}]";
        return "{\"result\": \"OK\"}";
    }
}
