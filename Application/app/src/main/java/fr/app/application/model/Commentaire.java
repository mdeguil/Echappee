package fr.app.application.model;

public class Commentaire {

    private int    id;
    private int    note;
    private String message;

    public Commentaire() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getNote() { return note; }
    public void setNote(int note) { this.note = note; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}