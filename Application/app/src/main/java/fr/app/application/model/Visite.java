package fr.app.application.model;

public class Visite {

    private int         id;
    private String      date;
    private Commentaire commentaires;

    public Visite() {}

    public int    getId()   { return id; }
    public void   setId(int id) { this.id = id; }

    public String getDate() { return date; }
    public void   setDate(String date) { this.date = date; }

    public Commentaire getCommentaires() { return commentaires; }
    public void setCommentaires(Commentaire commentaires) { this.commentaires = commentaires; }

    public int getNote() {
        return commentaires != null ? commentaires.getNote() : 0;
    }

    public String getMessage() {
        return commentaires != null ? commentaires.getMessage() : null;
    }

    /**
     * Retourne le nom du lieu lié au commentaire.
     * Côté API, Commentaire a un Lieu unique (ManyToOne), pas une liste.
     */
    public String getNomLieu() {
        if (commentaires == null) return null;
        Commentaire.Lieu lieu = commentaires.getLieu();
        if (lieu == null) return null;
        return lieu.getNom();
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


    public static class Commentaire {
        private int    id;
        private int    note;
        private String message;
        private Lieu   lieu;

        public int    getId()      { return id; }
        public int    getNote()    { return note; }
        public String getMessage() { return message; }
        public Lieu   getLieu()    { return lieu; }

        public static class Lieu {
            private int    id;
            private String nom;

            public int    getId()  { return id; }
            public String getNom() { return nom; }
        }
    }
}