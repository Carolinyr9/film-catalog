package br.ifsp.film_catalog.repository;

import br.ifsp.film_catalog.model.Review;
import br.ifsp.film_catalog.model.key.UserMovieId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReviewRepository extends JpaRepository<Review, UserMovieId> {
    @Query("SELECT r FROM Review r WHERE r.id.userId = :userId AND r.hidden = false")
    Page<Review> findAllVisibleByUserId(Long userId, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.id.movieId = :movieId AND r.hidden = false")
    Page<Review> findAllVisibleByMovieId(Long movieId, Pageable pageable);
}
