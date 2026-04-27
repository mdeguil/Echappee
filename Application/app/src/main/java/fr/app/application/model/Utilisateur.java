package fr.app.application.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.List;

import fr.app.application.utils.BDD.Converters;

@Entity(tableName = "utilisateur")
@TypeConverters(Converters.class)
public class Utilisateur {
    @PrimaryKey
    private int id;
    private String email;
    private String password;
    private String token; // JWT token sauvegardé pour l'accès offline
    private List<String> roles;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}
