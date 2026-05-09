package fr.app.application.model;

public class Commentaire {

    private int    id;
    private int    note;
    private String message;
    private Lieu   lieu;

    public Commentaire() {}

    public int    getId()      { return id; }
    public void   setId(int id) { this.id = id; }

    public int    getNote()    { return note; }
    public void   setNote(int note) { this.note = note; }

    public String getMessage() { return message; }
    public void   setMessage(String message) { this.message = message; }

    public Lieu   getLieu()    { return lieu; }
    public void   setLieu(Lieu lieu) { this.lieu = lieu; }

    public static class Lieu {
        private int    id;
        private String nom;

        public int    getId()  { return id; }
        public String getNom() { return nom; }
    }
}