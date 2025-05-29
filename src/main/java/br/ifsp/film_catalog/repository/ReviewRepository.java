package br.ifsp.film_catalog.repository;

import br.ifsp.film_catalog.model.Review;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByUserWatched_Movie_IdAndHiddenFalse(Long movieId, Pageable pageable);
    Page<Review> findByUserWatched_User_IdAndHiddenFalse(Long userId, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE SIZE(r.flags) >= :minFlags ORDER BY SIZE(r.flags) DESC, r.id ASC")
    List<Review> findReviewsWithMinimumFlagsOrderByFlagsDesc(
            @Param("minFlags") Integer minFlags
    );
}
