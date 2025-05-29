package br.ifsp.film_catalog.repository;

import br.ifsp.film_catalog.model.Review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByUserWatched_Movie_IdAndHiddenFalse(Long movieId, Pageable pageable);
    Page<Review> findByUserWatched_User_IdAndHiddenFalse(Long userId, Pageable pageable);
}
