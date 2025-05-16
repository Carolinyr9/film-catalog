package br.ifsp.film_catalog.model;

import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Embeddable;

@Embeddable
public class WatchlistMovieId implements Serializable {

    private Long watchlistId;
    private Long movieId;

    public WatchlistMovieId() {
    }

    public WatchlistMovieId(Long watchlistId, Long movieId) {
        this.watchlistId = watchlistId;
        this.movieId = movieId;
    }

    public Long getWatchlistId() {
        return watchlistId;
    }

    public void setWatchlistId(Long watchlistId) {
        this.watchlistId = watchlistId;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WatchlistMovieId)) return false;
        WatchlistMovieId that = (WatchlistMovieId) o;
        return Objects.equals(getWatchlistId(), that.getWatchlistId()) &&
                Objects.equals(getMovieId(), that.getMovieId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getWatchlistId(), getMovieId());
    }
}
