package fr.app.application.utils.BDD;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import fr.app.application.model.Itiniraire;

public class Converters {

    private static final Gson gson = new Gson();

    @TypeConverter
    public static String fromStringList(List<String> list) {
        return list == null ? null : gson.toJson(list);
    }

    @TypeConverter
    public static List<String> toStringList(String value) {
        if (value == null) return null;
        Type type = new TypeToken<List<String>>(){}.getType();
        return gson.fromJson(value, type);
    }

    @TypeConverter
    public static String fromLieuRefList(List<Itiniraire.LieuRef> list) {
        return list == null ? null : gson.toJson(list);
    }

    @TypeConverter
    public static List<Itiniraire.LieuRef> toLieuRefList(String value) {
        if (value == null) return null;
        Type type = new TypeToken<List<Itiniraire.LieuRef>>(){}.getType();
        return gson.fromJson(value, type);
    }
}
