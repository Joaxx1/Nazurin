package com.mycompany.finals.data;

import com.mycompany.finals.model.Game;
import java.util.List;
import java.util.Locale;

/**
 * A small built-in list of games, used when no RAWG API key is set so the app
 * still works offline. The ids are negative so they never clash with real RAWG ids,
 * and there are no cover pictures, so the screens show placeholders instead.
 */
public class SampleCatalog implements GameCatalog {

    private static final List<Game> GAMES = List.of(
            sample(-1, "The Witcher 3: Wild Hunt", "2015-05-18", 4.7, "Action, RPG", "PC, PlayStation 4, Xbox One, Nintendo Switch"),
            sample(-2, "Portal 2", "2011-04-18", 4.6, "Shooter, Puzzle", "PC, PlayStation 3, Xbox 360"),
            sample(-3, "Stardew Valley", "2016-02-26", 4.4, "Simulation, RPG, Indie", "PC, Nintendo Switch, PlayStation 4, Xbox One"),
            sample(-4, "Hollow Knight", "2017-02-24", 4.4, "Action, Platformer, Indie", "PC, Nintendo Switch, PlayStation 4, Xbox One"),
            sample(-5, "Red Dead Redemption 2", "2018-10-26", 4.6, "Action, Adventure", "PC, PlayStation 4, Xbox One"),
            sample(-6, "Celeste", "2018-01-25", 4.3, "Platformer, Indie", "PC, Nintendo Switch, PlayStation 4, Xbox One"),
            sample(-7, "Hades", "2020-09-17", 4.5, "Action, RPG, Indie", "PC, Nintendo Switch, PlayStation 4, Xbox One"),
            sample(-8, "Elden Ring", "2022-02-25", 4.4, "Action, RPG", "PC, PlayStation 5, PlayStation 4, Xbox Series S/X, Xbox One"),
            sample(-9, "Grand Theft Auto V", "2013-09-17", 4.5, "Action, Adventure", "PC, PlayStation 5, PlayStation 4, Xbox Series S/X, Xbox One"),
            sample(-10, "Minecraft", "2009-05-10", 4.4, "Action, Arcade", "PC, Nintendo Switch, PlayStation 4, Xbox One, iOS, Android"));

    private static Game sample(int id, String name, String released, double rating, String genres, String platforms) {
        return new Game(id, name, released, rating, genres, platforms, "");
    }

    @Override
    public String getName() {
        return "the built-in samples (no RAWG key set)";
    }

    /** The samples all fit on page 1; later pages are empty. */
    @Override
    public GamePage search(String query, int page) {
        String needle = query.strip().toLowerCase(Locale.ROOT);
        List<Game> matches = GAMES.stream()
                .filter(game -> game.getName().toLowerCase(Locale.ROOT).contains(needle))
                .toList();
        return new GamePage(page <= 1 ? matches : List.of(), matches.size(), false);
    }
}
