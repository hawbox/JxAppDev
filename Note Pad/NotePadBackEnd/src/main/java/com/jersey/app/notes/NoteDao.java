package com.jersey.app.notes;

import java.sql.SQLException;
import java.util.List;

public interface NoteDao {
    public List<Note> getAllNotes() throws SQLException;
    public List<Note> getAllNotesByUser(int id) throws SQLException;
    public Note getNote(int id) throws SQLException;
    public void createNote(Note note) throws SQLException;
    public void updateNote(Note note) throws SQLException;
    public void deleteNote(int id) throws SQLException;
    public void deleteAllNotes() throws SQLException;
}
