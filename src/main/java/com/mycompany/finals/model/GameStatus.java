package com.mycompany.finals.model;

/**
 * Where a game sits in the user's backlog.
 */
public enum GameStatus {
    PLAN_TO_PLAY("Plan to Play"),
    PLAYING("Playing"),
    PLAYED("Played"),
    PAUSED("Paused"),
    DROPPED("Dropped");

    private final String label;

    GameStatus(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
