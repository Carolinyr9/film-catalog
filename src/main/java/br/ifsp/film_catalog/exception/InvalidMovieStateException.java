package br.ifsp.film_catalog.exception;

public class InvalidMovieStateException extends RuntimeException {
    public InvalidMovieStateException(String message) {
        super(message);
    }
}
