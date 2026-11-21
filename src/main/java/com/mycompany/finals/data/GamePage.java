package com.mycompany.finals.data;

import com.mycompany.finals.model.Game;
import java.util.List;

/**
 * One page of catalog results.
 *
 * @param games   the games on this page
 * @param total   how many games match in the whole catalog
 * @param hasMore whether another page can be loaded
 */
public record GamePage(List<Game> games, int total, boolean hasMore) {

    public GamePage {
        games = List.copyOf(games);
    }
}
