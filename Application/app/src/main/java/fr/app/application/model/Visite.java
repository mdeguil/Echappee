package fr.app.application.model;

import java.util.List;

public class Visite {

    private int        id;
    private String     date;
    private Commentaire commentaires;

    public Visite() {}

    public int    getId()   { return id; }
    public void   setId(int id) { this.id = id; }

    public String getDate() { return date; }
    public void   setDate(String date) { this.date = date; }

    public Commentaire getCommentaires() { return commentaires; }
    public void setCommentaires(Commentaire commentaires) { this.commentaires = commentaires; }

    // ── Raccourcis pour l'adapter ─────────────────────────────────────────

    public int getNote() {
        return commentaires != null ? commentaires.getNote() : 0;
    }

    public String getMessage() {
        return commentaires != null ? commentaires.getMessage() : null;
    }

    public String getNomLieu() {
        if (commentaires == null) return null;
        List<Commentaire.Lieu> lieux = commentaires.getLieu();
        if (lieux == null || lieux.isEmpty()) return null;
        return lieux.get(0).getNom(); // premier lieu lié au commentaire
    }

    // Formate "2026-04-23T00:00:00+00:00" ou "2026-04-23" → "23/04/2026"
    public String getDateFormatee() {
        if (date == null || date.isEmpty()) return "Date inconnue";
        try {
            String[] parts = date.split("T")[0].split("-");
            if (parts.length == 3) {
                return parts[2] + "/" + parts[1] + "/" + parts[0];
            }
        } catch (Exception e) { /* ignore */ }
        return date;
    }

    // ── Classes imbriquées ────────────────────────────────────────────────

    public static class Commentaire {
        private int         id;
        private int         note;
        private String      message;
        private List<Lieu>  lieu;

        public int         getId()      { return id; }
        public int         getNote()    { return note; }
        public String      getMessage() { return message; }
        public List<Lieu>  getLieu()    { return lieu; }

        public static class Lieu {
            private int    id;
            private String nom;

            public int    getId()  { return id; }
            public String getNom() { return nom; }
        }
    }
}