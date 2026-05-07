package fr.app.application.utils.BDD;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import fr.app.application.model.DetailLieux;
import fr.app.application.model.Itineraire;
import fr.app.application.model.Lieu;
import fr.app.application.model.Utilisateur;

@Database(
        entities = {Lieu.class, Itineraire.class, Utilisateur.class, DetailLieux.class},
        version = 3,
        exportSchema = false
)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    public abstract MyDao myDao();

    private static volatile AppDatabase INSTANCE;

    /**
     * Migration de la version 1 à 2 :
     * Refactorisation de la table 'Itiniraire', ajout de 'detail_lieu'
     * et du champ 'token' dans 'utilisateur'.
     */
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("DROP TABLE IF EXISTS `itiniraire_old`");
            database.execSQL("ALTER TABLE `Itiniraire` RENAME TO `itiniraire_old`");
            database.execSQL("CREATE TABLE IF NOT EXISTS `itiniraire` (" +
                    "`id` INTEGER NOT NULL, " +
                    "`dureTotal` INTEGER, " +
                    "`lieux` TEXT, " +
                    "`nbLieux` INTEGER NOT NULL DEFAULT 0, " +
                    "`userId` INTEGER NOT NULL DEFAULT 0, " +
                    "PRIMARY KEY(`id`))");
            database.execSQL("INSERT INTO `itiniraire` (id, dureTotal, lieux, nbLieux, userId) " +
                    "SELECT id, dureTotal, lieux, nbLieux, 0 FROM `itiniraire_old`");
            database.execSQL("DROP TABLE `itiniraire_old`");

            database.execSQL("CREATE TABLE IF NOT EXISTS `detail_lieu` (" +
                    "`id` INTEGER NOT NULL, " +
                    "`description` TEXT, " +
                    "`horaires` TEXT, " +
                    "`tarif` INTEGER, " +
                    "`accessibilite` TEXT, " +
                    "`photos` TEXT, " +
                    "PRIMARY KEY(`id`))");

            try {
                database.execSQL("ALTER TABLE `utilisateur` ADD COLUMN `token` TEXT");
            } catch (Exception ignored) {}
        }
    };

    /**
     * Migration de la version 2 à 3 :
     * La table detail_lieu existait déjà depuis la migration 1→2.
     * Cette migration est un no-op structurel, mais elle est obligatoire
     * pour que Room ne déclenche pas fallbackToDestructiveMigration()
     * et n'efface pas les données offline mises en cache.
     */
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Aucun changement de schéma entre v2 et v3 :
            // cette migration vide empêche la destruction des données offline.
        }
    };

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "charente_db")
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}