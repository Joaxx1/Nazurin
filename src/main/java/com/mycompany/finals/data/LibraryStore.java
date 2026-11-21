package com.mycompany.finals.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.mycompany.finals.model.LibraryEntry;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves and loads the library as a JSON file on this computer.
 * The project has no database, so this file is the only copy of the user's data.
 */
public class LibraryStore {

    private static final Type ENTRY_LIST = new TypeToken<List<LibraryEntry>>() { }.getType();

    private final Path file;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public LibraryStore(Path file) {
        this.file = file;
    }

    /** Reads the saved library, or returns an empty one if nothing has been saved yet. */
    public List<LibraryEntry> load() throws IOException {
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        try (Reader in = Files.newBufferedReader(file)) {
            List<LibraryEntry> entries = gson.fromJson(in, ENTRY_LIST);
            return entries == null ? new ArrayList<>() : entries;
        } catch (JsonParseException e) {
            throw new IOException(file + " is not a valid library file.", e);
        }
    }

    /** Writes the library. A temporary file is written first so a crash cannot leave a half-written save. */
    public void save(List<LibraryEntry> entries) throws IOException {
        Files.createDirectories(file.getParent());
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        Files.writeString(temp, gson.toJson(entries, ENTRY_LIST));
        Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }
}
