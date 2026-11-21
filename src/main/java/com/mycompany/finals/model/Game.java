package com.mycompany.finals.model;

/**
 * A game from the catalog. Only the details the app shows are kept.
 */
public class Game {

    private final int id;
    private final String name;
    private final String released;
    private final double rating;
    private final String genres;
    private final String platforms;
    private final String imageUrl;

    public Game(int id, String name, String released, double rating, String genres, String platforms,
            String imageUrl) {
        this.id = id;
        this.name = name;
        this.released = released == null ? "" : released;
        this.rating = rating;
        this.genres = genres == null ? "" : genres;
        this.platforms = platforms == null ? "" : platforms;
        this.imageUrl = imageUrl == null ? "" : imageUrl;
    }

    // The getters guard against null because games loaded from the save file skip the constructor.

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /** Release date as yyyy-mm-dd, or empty when unknown. */
    public String getReleased() {
        return released == null ? "" : released;
    }

    /** Year of release, or empty when unknown. */
    public String getYear() {
        String date = getReleased();
        return date.length() >= 4 ? date.substring(0, 4) : "";
    }

    /** Average rating out of 5, or 0 when unrated. */
    public double getRating() {
        return rating;
    }

    /** Comma-separated genre names. */
    public String getGenres() {
        return genres == null ? "" : genres;
    }

    /** Comma-separated platform names. */
    public String getPlatforms() {
        return platforms == null ? "" : platforms;
    }

    /** Web address of the cover picture, or empty when the game has none. */
    public String getImageUrl() {
        return imageUrl == null ? "" : imageUrl;
    }

    @Override
    public String toString() {
        return name;
    }
}
