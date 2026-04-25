package fr.app.application.model;

public class Visite {

    private int    id;
    private String date;
    private int    note;
    private String message;
    private String nomLieu;

    public Visite() {}

    public int    getId()      { return id; }
    public void   setId(int id) { this.id = id; }

    public String getDate()    { return date; }
    public void   setDate(String date) { this.date = date; }

    public int    getNote()    { return note; }
    public void   setNote(int note) { this.note = note; }

    public String getMessage() { return message; }
    public void   setMessage(String message) { this.message = message; }

    public String getNomLieu() { return nomLieu; }
    public void   setNomLieu(String nomLieu) { this.nomLieu = nomLieu; }
}