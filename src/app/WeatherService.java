package app;

import org.json.*;
import java.net.URI;
import java.net.http.*;
import java.util.ArrayList;
import java.util.List;

public class WeatherService {
    private final String apiKey = "afe3fe76000ae2751c5b6b9024b24732";

    public WeatherData getWeather(String query) throws Exception {
        String url = "https://api.openweathermap.org/data/2.5/weather?" + query + "&appid=" + apiKey + "&units=metric";
        return parse(fetchJson(url), false);
    }

    public List<WeatherData> getForecast(String query) throws Exception {
        String url = "https://api.openweathermap.org/data/2.5/forecast?" + query + "&appid=" + apiKey + "&units=metric";
        JSONObject json = fetchJson(url);
        JSONArray list = json.getJSONArray("list");
        List<WeatherData> forecast = new ArrayList<>();
        // Fetch up to 8 entries (24 hours of data)
        for (int i = 0; i < 8; i++) {
            forecast.add(parse(list.getJSONObject(i), true));
        }
        return forecast;
    }

    private JSONObject fetchJson(String url) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) throw new Exception("API Error: " + resp.statusCode());
        return new JSONObject(resp.body());
    }

    private WeatherData parse(JSONObject json, boolean isForecast) {
        JSONObject main = json.getJSONObject("main");
        String time = isForecast ? json.getString("dt_txt") : "Current";
        return new WeatherData(
            main.getDouble("temp"),
            main.getInt("humidity"),
            json.has("wind") ? json.getJSONObject("wind").getDouble("speed") : 0.0,
            json.getJSONArray("weather").getJSONObject(0).getString("description"),
            json.getJSONArray("weather").getJSONObject(0).getString("icon"),
            time
        );
    }
}