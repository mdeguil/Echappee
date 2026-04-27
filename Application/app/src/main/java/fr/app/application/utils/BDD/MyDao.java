package fr.app.application.utils.BDD;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import fr.app.application.model.DetailLieux;
import fr.app.application.model.Itiniraire;
import fr.app.application.model.Lieu;
import fr.app.application.model.Utilisateur;

@Dao
public interface MyDao {

    // ── Lieux ─────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLieux(List<Lieu> lieux);

    @Query("SELECT * FROM lieu")
    List<Lieu> getAllLieux();

    // ── Détails lieux ─────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDetailLieu(DetailLieux detail);

    @Query("SELECT * FROM detail_lieu WHERE id = :id LIMIT 1")
    DetailLieux getDetailLieu(int id);

    // ── Itinéraires ───────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertItineraires(List<Itiniraire> itineraires);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertItineraire(Itiniraire itineraire);

    @Query("SELECT * FROM itiniraire WHERE userId = :userId")
    List<Itiniraire> getItinerairesByUser(int userId);

    @Query("DELETE FROM itiniraire WHERE id = :id")
    void deleteItineraire(int id);

    // ── Utilisateur ───────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void setLastUser(Utilisateur utilisateur);

    @Query("SELECT * FROM utilisateur LIMIT 1")
    Utilisateur getLastUser();

    @Query("DELETE FROM utilisateur")
    void clearUsers();
}
