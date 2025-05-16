package br.ifsp.film_catalog.model;

import jakarta.persistence.*;

@Entity
public class WatchlistMovie {
    @EmbeddedId
    private WatchlistMovieId id = new WatchlistMovieId();

    @ManyToOne
    @MapsId("watchlistId")
    @JoinColumn(name = "watchlist_id")
    private Watchlist watchlist;

    @ManyToOne
    @MapsId("movieId")
    @JoinColumn(name = "movie_id")
    private Movie movie;

    private boolean assistido = false;

    public WatchlistMovie() {
    }

    public WatchlistMovie(Watchlist watchlist, Movie movie, boolean assistido) {
        this.watchlist = watchlist;
        this.movie = movie;
        this.assistido = assistido;
        this.id = new WatchlistMovieId(watchlist.getId(), movie.getId());
    }

    public boolean isAssistido() {
        return assistido;
    }

    public void setAssistido(boolean assistido) {
        this.assistido = assistido;
    }

    public Watchlist getWatchlist() {
        return watchlist;
    }

    public void setWatchlist(Watchlist watchlist) {
        this.watchlist = watchlist;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public WatchlistMovieId getId() {
        return id;
    }

    public void setId(WatchlistMovieId id) {
        this.id = id;
    }
}
