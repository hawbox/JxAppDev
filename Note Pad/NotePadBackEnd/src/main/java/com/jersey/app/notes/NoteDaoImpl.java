package com.jersey.app.notes;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

public class NoteDaoImpl implements NoteDao {

    Connection con = null;

    private static final String createStmt = "INSERT INTO notes(notename, note, date, user_id) VALUES (?, ?, ?, ?)";
    private static final String loadStmt = "SELECT id, notename, note, date, user_id FROM notes WHERE id = ?";
    private static final String loadAllStmt = "SELECT * FROM notes";
    private static final String loadAllByUserStmt = "SELECT * FROM notes WHERE user_id = ?";
    private static final String updateStmt = "UPDATE notes SET id = ?, notename = ?, note = ?, date = ? WHERE id = ?";
    private static final String deleteStmt = "DELETE FROM notes WHERE id = ?";
    private static final String deleteAllStmt = "DELETE FROM notes";

    public NoteDaoImpl(Connection con) {
        this.con = con;
    }

    @Override
    public List<Note> getAllNotes() throws SQLException {
        List<Note> notes = new LinkedList<Note>();

            Statement statement = con.createStatement();
            ResultSet resultSet = statement.executeQuery(loadAllStmt);

            Note note = null;

            while (resultSet.next()) {
                note = new Note();

                note.setId(resultSet.getInt("id"));
                note.setNotename(resultSet.getString("notename"));
                note.setNote(resultSet.getString("note"));
                note.setDate(Timestamp.valueOf(resultSet.getString("date")));
                note.setUser_id(resultSet.getInt("user_id"));

                notes.add(note);
            }

            resultSet.close();
            statement.close();
        return notes;
    }

    @Override
    public List<Note> getAllNotesByUser(int id) throws SQLException {
        List<Note> notes = new LinkedList<Note>();

        PreparedStatement ps = con.prepareStatement(loadAllByUserStmt);
        ps.setInt(1, id);
        ResultSet resultSet = ps.executeQuery();

        Note note = null;

        while (resultSet.next()) {
            note = new Note();

            note.setId(resultSet.getInt("id"));
            note.setNotename(resultSet.getString("notename"));
            note.setNote(resultSet.getString("note"));
            note.setDate(Timestamp.valueOf(resultSet.getString("date")));
            note.setUser_id(resultSet.getInt("user_id"));

            notes.add(note);
        }

        resultSet.close();
        ps.close();
        return notes;
    }

    @Override
    public Note getNote(int id) throws SQLException {
        PreparedStatement ps = con.prepareStatement(loadStmt);
        Note a = new Note();
        ps.setInt(1, id);
        ResultSet result = ps.executeQuery();
        ps.close();

        if (!result.next()) return null;

        a.setId(result.getInt("id"));
        a.setNotename(result.getString("notename"));
        a.setNote(result.getString("note"));
        a.setDate(Timestamp.valueOf(result.getString("date")));
        a.setUser_id(result.getInt("user_id"));
        return a;
    }

    @Override
    public void createNote(Note a) throws SQLException {
        PreparedStatement ps = con.prepareStatement(createStmt, Statement.RETURN_GENERATED_KEYS);

        ps.setString(1, a.getNotename());
        ps.setString(2, a.getNote());
        ps.setTimestamp(3, a.getDate());
        ps.setInt(4, a.getUser_id());
        System.out.println("Request = " + ps.toString());
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if(rs.next())
        {
            a.setId(rs.getInt(1));
        }

        ps.close();
    }

    @Override
    public void updateNote(Note note) throws SQLException {
        PreparedStatement ps = con.prepareStatement(updateStmt);

        ps.setInt(1, note.getId());
        ps.setString(2, note.getNotename());
        ps.setString(3, note.getNote());
        ps.setTimestamp(4, note.getDate());
        ps.setInt(5, note.getId());

        ps.executeUpdate();
        ps.close();
    }

    @Override
    public void deleteNote(int id) throws SQLException {
        PreparedStatement ps = con.prepareStatement(deleteStmt);

        ps.setInt(1, id);

        ps.executeUpdate();
        ps.close();
    }

    @Override
    public void deleteAllNotes() throws SQLException {
        PreparedStatement ps = con.prepareStatement(deleteAllStmt);
        ps.executeUpdate();
        ps.close();
    }
}
