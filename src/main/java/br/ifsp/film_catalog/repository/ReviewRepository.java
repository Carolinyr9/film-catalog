package br.ifsp.film_catalog.repository;

import br.ifsp.film_catalog.model.Movie;
import br.ifsp.film_catalog.model.Review;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByUserWatched_User_Id(Long userId, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE SIZE(r.flags) >= :minFlags ORDER BY SIZE(r.flags) DESC, r.id ASC")
    List<Review> findReviewsWithMinimumFlagsOrderByFlagsDesc(
            @Param("minFlags") Integer minFlags
    );

    @Query("""
    SELECT r.userWatched.movie
    FROM Review r
    WHERE r.hidden = false
    GROUP BY r.userWatched.movie
    ORDER BY MAX(r.generalScore) DESC
    """)
    Page<Movie> findTopRatedMovies(Pageable pageable);

    Page<Review> findByUserWatched_Movie_Id(Long movieId, Pageable pageable);

    long countByUserWatched_User_Id(Long userId);

    @Query("SELECT COALESCE(SUM(r.likesCount), 0) FROM Review r WHERE r.userWatched.user.id = :userId")
    long sumLikesCountByUserWatched_User_Id(@Param("userId") Long userId);

    @Query("SELECT AVG(r.directionScore) FROM Review r WHERE r.userWatched.user.id = :userId")
    double calculateAverageDirectionScoreByUserWatched_User_Id(@Param("userId") Long userId);

    @Query("SELECT AVG(r.screenplayScore) FROM Review r WHERE r.userWatched.user.id = :userId")
    double calculateAverageScreenplayScoreByUserWatched_User_Id(@Param("userId") Long userId);

    @Query("SELECT AVG(r.cinematographyScore) FROM Review r WHERE r.userWatched.user.id = :userId")
    double calculateAverageCinematographyScoreByUserWatched_User_Id(@Param("userId") Long userId);

    @Query("SELECT AVG(r.generalScore) FROM Review r WHERE r.userWatched.user.id = :userId")
    double calculateAverageGeneralScoreByUserWatched_User_Id(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM Review r WHERE r.userWatched.id.movieId = :movieId")
    void deleteByUserWatchedMovieId(@Param("movieId") Long movieId);
}


