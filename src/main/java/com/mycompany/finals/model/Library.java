package com.mycompany.finals.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * The user's personal game list. It does no file or network work itself;
 * instead it notifies listeners after every change so the screens can refresh
 * and the library can be saved.
 */
public class Library {

    private final List<LibraryEntry> entries;
    private final List<Runnable> listeners = new ArrayList<>();

    public Library(List<LibraryEntry> entries) {
        this.entries = new ArrayList<>(entries);
    }

    public List<LibraryEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /** Entries whose status is one of the given statuses. */
    public List<LibraryEntry> withStatus(Collection<GameStatus> statuses) {
        return entries.stream()
                .filter(entry -> statuses.contains(entry.getStatus()))
                .toList();
    }

    /** The library entry for this game, if it has been added. */
    public Optional<LibraryEntry> find(Game game) {
        return entries.stream()
                .filter(entry -> entry.getGame().getId() == game.getId())
                .findFirst();
    }

    public boolean contains(Game game) {
        return find(game).isPresent();
    }

    /** Adds the game with the given status. Returns false if it is already in the library. */
    public boolean add(Game game, GameStatus status) {
        if (contains(game)) {
            return false;
        }
        entries.add(new LibraryEntry(game, status));
        fireChanged();
        return true;
    }

    public void setStatus(LibraryEntry entry, GameStatus status) {
        entry.setStatus(status);
        fireChanged();
    }

    public void setReview(LibraryEntry entry, String review) {
        entry.setReview(review.strip());
        fireChanged();
    }

    public void remove(LibraryEntry entry) {
        if (entries.remove(entry)) {
            fireChanged();
        }
    }

    public void addChangeListener(Runnable listener) {
        listeners.add(listener);
    }

    private void fireChanged() {
        listeners.forEach(Runnable::run);
    }
}
