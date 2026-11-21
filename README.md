# Nazurin

A Java Swing desktop app that helps gamers track their game backlog and decide what to play next.

Object-Oriented Programming finals project by **Team Nazurin**: Calaoagan, Sean · Cordero, Hexel · Juanico, Joaquin.

## Features

- **Catalog:** search the RAWG Video Games Database and browse games with their cover art, release year, and rating. More games load as you scroll.
- **My Library:** add games as Plan to Play, Playing, Played, Paused, or Dropped, filter them by status, and write short reviews.
- **Game Roulette:** picks a random game from your Plan to Play and Paused games.
- **Saves automatically** to a file on your computer: no database and no account needed.

## Requirements

- Java 21
- Apache NetBeans (the project is a Maven project)

## RAWG API key

The full catalog needs a free API key from [rawg.io/apidocs](https://rawg.io/apidocs). Create the file `~/.nazurin/config.properties` (on Windows: `C:\Users\YourName\.nazurin\config.properties`) containing:

```
rawg.api.key=your-key-here
```

Without a key, the app shows ten built-in sample games. The key is never stored in this repository.

## Running

Open the project in Apache NetBeans and press **Run Project** (F6).

## Where your data is saved

Everything is kept in the `.nazurin` folder in your home folder: `library.json` holds your games, statuses, and reviews, and `covers/` holds downloaded cover pictures.
