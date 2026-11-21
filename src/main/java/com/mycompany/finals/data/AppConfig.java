package com.mycompany.finals.data;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

/**
 * App settings. The RAWG API key is read from the RAWG_API_KEY environment
 * variable, or from ~/.nazurin/config.properties as a line like:
 * <pre>rawg.api.key=your-key-here</pre>
 * Without a key the app falls back to {@link SampleCatalog}.
 */
public final class AppConfig {

    /** Folder that holds the save file and the config file. */
    public static final Path APP_DIR = Path.of(System.getProperty("user.home"), ".nazurin");

    private AppConfig() {
    }

    public static Optional<String> rawgApiKey() {
        String fromEnv = System.getenv("RAWG_API_KEY");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return Optional.of(fromEnv.strip());
        }
        Path file = APP_DIR.resolve("config.properties");
        if (!Files.isRegularFile(file)) {
            return Optional.empty();
        }
        Properties properties = new Properties();
        try (Reader in = Files.newBufferedReader(file)) {
            properties.load(in);
        } catch (IOException e) {
            System.err.println("Could not read " + file + ": " + e.getMessage());
            return Optional.empty();
        }
        String key = properties.getProperty("rawg.api.key", "").strip();
        return key.isEmpty() ? Optional.empty() : Optional.of(key);
    }

    /** RAWG when a key is set, otherwise the built-in sample games. */
    public static GameCatalog createCatalog() {
        return rawgApiKey().<GameCatalog>map(RawgCatalog::new).orElseGet(SampleCatalog::new);
    }
}
