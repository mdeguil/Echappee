package fr.app.application.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Meteo {

    @SerializedName("weather")
    private List<WeatherCondition> weather;

    @SerializedName("main")
    private Main main;

    @SerializedName("wind")
    private Wind wind;

    public DataPoint getPremierPoint() {
        if (main == null) return null;
        DataPoint point = new DataPoint();
        point.temp      = main.temp;
        point.feelsLike = main.feelsLike;
        point.humidity  = main.humidity;
        point.windSpeed = (wind != null) ? wind.speed : 0;
        point.weather   = weather;
        return point;
    }

    public static class Main {
        @SerializedName("temp")       public double temp;
        @SerializedName("feels_like") public double feelsLike;
        @SerializedName("humidity")   public int    humidity;
    }

    public static class Wind {
        @SerializedName("speed") public double speed;
    }

    public static class DataPoint {
        private double temp;
        private double feelsLike;
        private int    humidity;
        private double windSpeed;
        private List<WeatherCondition> weather;

        public double getTemp()      { return temp; }
        public double getFeelsLike() { return feelsLike; }
        public int    getHumidity()  { return humidity; }
        public double getWindSpeed() { return windSpeed; }

        public WeatherCondition getConditionPrincipale() {
            return (weather != null && !weather.isEmpty()) ? weather.get(0) : null;
        }
    }

    public static class WeatherCondition {
        @SerializedName("description") private String description;
        @SerializedName("icon")        private String icon;

        public String getDescription() { return description; }
        public String getIconUrl() {
            return "https://openweathermap.org/img/wn/" + icon + "@2x.png";
        }
    }
}