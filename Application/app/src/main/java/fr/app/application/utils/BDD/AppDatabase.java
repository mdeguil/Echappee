package fr.app.application.utils.BDD;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import fr.app.application.model.DetailLieux;
import fr.app.application.model.Itiniraire;
import fr.app.application.model.Lieu;
import fr.app.application.model.Utilisateur;

@Database(
        entities = {Lieu.class, Itiniraire.class, Utilisateur.class, DetailLieux.class},
        version = 2,
        exportSchema = false
)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    public abstract MyDao myDao();

    private static volatile AppDatabase INSTANCE;

    /**
     * Migration v1 → v2 :
     * - Renommage table "Itiniraire" → "itiniraire" + ajout colonne userId
     * - Ajout table detail_lieu
     * - Ajout colonne token dans utilisateur
     */
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Recréer la table itiniraire avec userId
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

            // Créer la table detail_lieu
            database.execSQL("CREATE TABLE IF NOT EXISTS `detail_lieu` (" +
                    "`id` INTEGER NOT NULL, " +
                    "`description` TEXT, " +
                    "`horaires` TEXT, " +
                    "`tarif` INTEGER, " +
                    "`accessibilite` TEXT, " +
                    "`photos` TEXT, " +
                    "PRIMARY KEY(`id`))");

            // Ajouter colonne token à utilisateur (si elle n'existe pas)
            try {
                database.execSQL("ALTER TABLE `utilisateur` ADD COLUMN `token` TEXT");
            } catch (Exception ignored) {}
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
                            .addMigrations(MIGRATION_1_2)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
