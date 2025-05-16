package br.ifsp.film_catalog.exception;

public class InvalidReviewStateException extends RuntimeException {
    public InvalidReviewStateException(String message) {
        super(message);
    }
}
