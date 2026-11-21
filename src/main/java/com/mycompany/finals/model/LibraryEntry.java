package com.mycompany.finals.model;

/**
 * A game the user has added to their library, with its status and review.
 * The setters are package-private so every change goes through {@link Library},
 * which tells the screens and the save file about it.
 */
public class LibraryEntry {

    private final Game game;
    private GameStatus status;
    private String review;

    public LibraryEntry(Game game, GameStatus status) {
        this.game = game;
        this.status = status;
        this.review = "";
    }

    public Game getGame() {
        return game;
    }

    public GameStatus getStatus() {
        return status;
    }

    void setStatus(GameStatus status) {
        this.status = status;
    }

    public String getReview() {
        return review == null ? "" : review;
    }

    void setReview(String review) {
        this.review = review;
    }
}
