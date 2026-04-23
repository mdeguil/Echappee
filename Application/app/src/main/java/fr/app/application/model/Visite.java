package fr.app.application.model;

public class Visite {

    private int    id;
    private String date;
    private int    commentaires;

    public Visite() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getCommentaires() { return commentaires; }
    public void setCommentaires(int commentaires) { this.commentaires = commentaires; }
}