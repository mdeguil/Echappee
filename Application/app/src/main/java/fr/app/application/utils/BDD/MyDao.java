package fr.app.application.utils.BDD;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import fr.app.application.model.Lieu;
import fr.app.application.model.Utilisateur;

@Dao
public interface MyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLieux(List<Lieu> lieux);

    @Query("SELECT * FROM lieux")
    List<Lieu> getAllLieux();

    // Pour l'utilisateur connecté
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void setLastUser(Utilisateur utilisateur);

    @Query("SELECT * FROM user_table LIMIT 1")
    Utilisateur getLastUser();
}