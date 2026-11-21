package com.mycompany.finals.data;

import java.io.IOException;

/**
 * Somewhere the app can look up games. The screens only use this interface,
 * so the source (RAWG, or the built-in samples) can be swapped freely.
 */
public interface GameCatalog {

    /** Short name shown on screen, for example "RAWG". */
    String getName();

    /**
     * One page of games whose title matches the query. A blank query lists popular games.
     * Pages are numbered from 1.
     */
    GamePage search(String query, int page) throws IOException;
}
