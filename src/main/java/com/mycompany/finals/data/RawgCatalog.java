package com.mycompany.finals.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mycompany.finals.model.Game;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Searches the RAWG Video Games Database (https://rawg.io/apidocs).
 */
public class RawgCatalog implements GameCatalog {

    private static final String BASE_URL = "https://api.rawg.io/api/games";
    /** The most games RAWG returns per request. */
    private static final int PAGE_SIZE = 40;

    private final String apiKey;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public RawgCatalog(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String getName() {
        return "RAWG";
    }

    @Override
    public GamePage search(String query, int page) throws IOException {
        StringBuilder url = new StringBuilder(BASE_URL)
                .append("?key=").append(encode(apiKey))
                .append("&page_size=").append(PAGE_SIZE)
                .append("&page=").append(Math.max(1, page));
        if (!query.isBlank()) {
            url.append("&search=").append(encode(query.strip()));
        }
        HttpRequest request = HttpRequest.newBuilder(URI.create(url.toString()))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("The search was interrupted.", e);
        }

        if (response.statusCode() == 401) {
            throw new IOException("RAWG rejected the API key (HTTP 401).");
        }
        if (response.statusCode() != 200) {
            throw new IOException("RAWG returned HTTP " + response.statusCode() + ".");
        }
        return parsePage(response.body());
    }

    /** Turns the JSON of a RAWG /games response into a page of Game objects. */
    static GamePage parsePage(String json) throws IOException {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            List<Game> games = new ArrayList<>();
            JsonArray results = root.getAsJsonArray("results");
            if (results != null) {
                for (JsonElement element : results) {
                    games.add(parseGame(element.getAsJsonObject()));
                }
            }
            int total = root.has("count") && !root.get("count").isJsonNull() ? root.get("count").getAsInt() : games.size();
            boolean hasMore = root.has("next") && !root.get("next").isJsonNull();
            return new GamePage(games, total, hasMore);
        } catch (RuntimeException e) {
            // Gson signals malformed or unexpected JSON with unchecked exceptions.
            throw new IOException("RAWG sent a response the app could not read.", e);
        }
    }

    private static Game parseGame(JsonObject game) {
        return new Game(
                game.get("id").getAsInt(),
                text(game, "name"),
                text(game, "released"),
                number(game, "rating"),
                names(game.get("genres"), null),
                names(game.get("platforms"), "platform"),
                thumbnailUrl(text(game, "background_image")));
    }

    /**
     * RAWG's cover pictures are full size (often 200 KB or more). Putting "resize/640/-/" after
     * "/media/" asks RAWG for a 640-pixel-wide copy instead, which is plenty for the app.
     */
    static String thumbnailUrl(String url) {
        String marker = "/media/";
        int at = url.indexOf(marker);
        if (at < 0 || url.contains("/media/resize/") || url.contains("/media/crop/")) {
            return url;
        }
        int split = at + marker.length();
        return url.substring(0, split) + "resize/640/-/" + url.substring(split);
    }

    private static String text(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value == null || value.isJsonNull() ? "" : value.getAsString();
    }

    private static double number(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value == null || value.isJsonNull() ? 0 : value.getAsDouble();
    }

    /**
     * Joins the "name" of each item in a RAWG array, e.g. genres become "Action, RPG".
     * Platform names sit one level down, inside a "platform" object, so pass that as the wrapper.
     */
    private static String names(JsonElement array, String wrapper) {
        if (array == null || !array.isJsonArray()) {
            return "";
        }
        List<String> names = new ArrayList<>();
        for (JsonElement item : array.getAsJsonArray()) {
            JsonObject object = item.getAsJsonObject();
            if (wrapper != null) {
                object = object.getAsJsonObject(wrapper);
            }
            if (object != null) {
                names.add(text(object, "name"));
            }
        }
        return String.join(", ", names);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
