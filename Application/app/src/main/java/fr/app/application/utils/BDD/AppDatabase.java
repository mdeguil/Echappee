package fr.app.application.utils.BDD;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import fr.app.application.model.Itiniraire;
import fr.app.application.model.Lieu;
import fr.app.application.model.Utilisateur;

@Database(entities = {Lieu.class, Itiniraire.class, Utilisateur.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract MyDao myDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "charente_db").build();
                }
            }
        }
        return INSTANCE;
    }
}