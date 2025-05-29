package br.ifsp.film_catalog.model;

import br.ifsp.film_catalog.model.enums.ContentRating;
import br.ifsp.film_catalog.model.enums.RoleName;
import br.ifsp.film_catalog.model.key.UserMovieId;
import br.ifsp.film_catalog.model.key.UserReviewId;
import br.ifsp.film_catalog.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test") // Ensure you have application-test.properties for H2
@Transactional // Rolls back transactions after each test, keeping tests isolated
public class JpaRelationshipTests {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private MovieRepository movieRepository;
    @Autowired private GenreRepository genreRepository;
    @Autowired private WatchlistRepository watchlistRepository;
    @Autowired private UserFavoriteRepository userFavoriteRepository;
    @Autowired private UserWatchedRepository userWatchedRepository;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private ContentFlagRepository contentFlagRepository;

    private Role roleUser, roleAdmin;
    private Genre genreAction, genreSciFi;

    @BeforeEach
    void setUp() {
        // Create common lookup entities once to ensure they have IDs and are managed
        roleUser = roleRepository.findByRoleName(RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_USER)));
        roleAdmin = roleRepository.findByRoleName(RoleName.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_ADMIN)));

        genreAction = genreRepository.findByNameIgnoreCase("Action")
                .orElseGet(() -> genreRepository.save(new Genre("Action")));
        genreSciFi = genreRepository.findByNameIgnoreCase("Sci-Fi")
                .orElseGet(() -> genreRepository.save(new Genre("Sci-Fi")));
    }

    private User createUser(String usernameSuffix) {
        User user = new User();
        user.setUsername("testuser_" + usernameSuffix);
        user.setEmail("testuser_" + usernameSuffix + "@example.com");
        user.setPassword("password");
        // Ensure User constructor or setters are available if not using @AllArgsConstructor from BaseEntity
        return userRepository.save(user);
    }

    private Movie createMovie(String title) {
        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setReleaseYear(2000 + (int)(Math.random() * 20)); // Add some variation
        movie.setDuration(120 + (int)(Math.random() * 30));
        movie.setContentRating(ContentRating.AL);
        return movieRepository.save(movie);
    }

    @Test
    void testUserRoleRelationship() {
        User user1 = createUser("rolesTest");
        user1.addRole(roleUser);
        user1.addRole(roleAdmin);
        userRepository.save(user1); // User is the owning side for user_roles

        User fetchedUser = userRepository.findById(user1.getId()).orElseThrow();
        assertThat(fetchedUser.getRoles()).hasSize(2);
        assertThat(fetchedUser.getRoles()).containsExactlyInAnyOrder(roleUser, roleAdmin);

        // Test inverse side
        Role fetchedRoleUser = roleRepository.findById(roleUser.getId()).orElseThrow();
        assertThat(fetchedRoleUser.getUsers()).contains(fetchedUser);
        Role fetchedRoleAdmin = roleRepository.findById(roleAdmin.getId()).orElseThrow();
        assertThat(fetchedRoleAdmin.getUsers()).contains(fetchedUser);
    }

    @Test
    void testUserWatchlistMovieRelationship() {
        User user1 = createUser("watchlistOwner_R"); // User is created and saved, has an ID
        Movie movie1 = createMovie("Inception_WM_R");
        Movie movie2 = createMovie("Interstellar_WM_R");

        // Create a new Watchlist instance
        Watchlist watchlist = new Watchlist(); 
        watchlist.setName("My Sci-Fi List For Test");
        // watchlist.setUser(user1); // This will be handled by user1.addWatchlist

        // **Establish the bidirectional relationship fully using the helper method on User**
        // This adds 'watchlist' to 'user1.getWatchlists()' AND calls 'watchlist.setUser(user1)'
        user1.addWatchlist(watchlist);

        // Add movies to the watchlist (which is now linked to user1 in memory)
        // Watchlist is the owning side of the Watchlist-Movie M:N relationship
        watchlist.addMovie(movie1);
        watchlist.addMovie(movie2);
        
        // **Save the User entity.**
        // Due to CascadeType.ALL on User.watchlists, this should:
        // 1. Persist the 'watchlist' entity (if new).
        // 2. Correctly set the 'user_id' foreign key in the 'watchlists' table.
        // Due to CascadeType.PERSIST/MERGE on Watchlist.movies, this should also:
        // 3. Populate the 'watchlist_movies' join table.
        userRepository.saveAndFlush(user1); // Use saveAndFlush to ensure immediate DB operation

        // --- Verification ---

        // Fetch a fresh instance of the user to ensure we're reading from DB state
        User fetchedUser = userRepository.findById(user1.getId()).orElseThrow();
        
        assertThat(fetchedUser.getWatchlists())
            .as("User's watchlist collection should contain the watchlist")
            .hasSize(1); // <<< This was the failing line (line 105 in your log)

        Watchlist fetchedUserWatchlist = fetchedUser.getWatchlists().iterator().next();
        assertThat(fetchedUserWatchlist.getName()).isEqualTo("My Sci-Fi List For Test");
        // Ensure the user link is correct
        assertThat(fetchedUserWatchlist.getUser()).isEqualTo(fetchedUser); // Relies on User having proper equals/hashCode (from BaseEntity)

        // Verify movies in the fetched watchlist
        // Fetch the watchlist directly to ensure its movie collection is loaded correctly
        Watchlist directlyFetchedWatchlist = watchlistRepository.findById(fetchedUserWatchlist.getId()).orElseThrow();
        assertThat(directlyFetchedWatchlist.getMovies())
            .as("Watchlist's movie collection should contain the added movies")
            .hasSize(2);
        assertThat(directlyFetchedWatchlist.getMovies()).extracting(Movie::getTitle)
            .containsExactlyInAnyOrder("Inception_WM_R", "Interstellar_WM_R");

        // Verify inverse side: Movie should know it's in the watchlist
        Movie fetchedMovie1 = movieRepository.findByTitle("Inception_WM_R").orElseThrow();
        assertThat(fetchedMovie1.getWatchlists())
            .as("Movie's watchlist collection should contain the watchlist")
            .contains(directlyFetchedWatchlist); // Relies on Watchlist having proper equals/hashCode
    }

    @Test
    void testMovieGenreRelationship() {
        Movie movie1 = createMovie("The Dark Knight_MG");
        movie1.addGenre(genreAction); // Movie is owning side for movie_genres
        movie1.addGenre(genreSciFi); // Example, though TDK isn't sci-fi
        movieRepository.save(movie1);

        Movie fetchedMovie = movieRepository.findById(movie1.getId()).orElseThrow();
        assertThat(fetchedMovie.getGenres()).hasSize(2);
        assertThat(fetchedMovie.getGenres()).containsExactlyInAnyOrder(genreAction, genreSciFi);

        // Test inverse side
        Genre fetchedGenreAction = genreRepository.findById(genreAction.getId()).orElseThrow();
        assertThat(fetchedGenreAction.getMovies()).contains(fetchedMovie);
    }

    @Test
    void testUserFavoriteMovieRelationship() {
        User user1 = createUser("movieFan");
        Movie movie1 = createMovie("Pulp Fiction_Fav");

        // Use User's helper method
        user1.addFavorite(movie1);
        userRepository.save(user1); // Saving user cascades to UserFavorite

        // Verify from User side
        User fetchedUser = userRepository.findById(user1.getId()).orElseThrow();
        assertThat(fetchedUser.getFavoriteMovies()).hasSize(1);

        UserFavorite favorite = fetchedUser.getFavoriteMovies().iterator().next();
        assertThat(favorite.getMovie().getTitle()).isEqualTo("Pulp Fiction_Fav");
        assertThat(favorite.getUser()).isEqualTo(fetchedUser);
        assertThat(favorite.getFavoritedAt()).isBeforeOrEqualTo(LocalDateTime.now());

        // Verify by fetching UserFavorite directly
        UserMovieId favoriteId = new UserMovieId(user1.getId(), movie1.getId());
        Optional<UserFavorite> fetchedFavoriteDirectly = userFavoriteRepository.findById(favoriteId);
        assertThat(fetchedFavoriteDirectly).isPresent();
        assertThat(fetchedFavoriteDirectly.get().getMovie().getTitle()).isEqualTo("Pulp Fiction_Fav");

        // Verify inverse side on Movie
        Movie fetchedMovie = movieRepository.findById(movie1.getId()).orElseThrow();
        assertThat(fetchedMovie.getFavoritedBy()).hasSize(1);
        assertThat(fetchedMovie.getFavoritedBy().iterator().next().getUser()).isEqualTo(fetchedUser);
    }

    @Test
    void testUserWatchedMovieRelationship() {
        User user1 = createUser("viewer1");
        Movie movie1 = createMovie("Fight Club_Watched");
        LocalDateTime watchedAtTime = LocalDateTime.now().minusDays(1).withNano(0); // Remove nanos for comparison

        // Use User's helper method
        user1.addWatched(movie1, watchedAtTime);
        userRepository.save(user1); // Saving user cascades to UserWatched

        // Verify from User side
        User fetchedUser = userRepository.findById(user1.getId()).orElseThrow();
        assertThat(fetchedUser.getWatchedMovies()).hasSize(1);

        UserWatched watched = fetchedUser.getWatchedMovies().iterator().next();
        assertThat(watched.getMovie().getTitle()).isEqualTo("Fight Club_Watched");
        assertThat(watched.getUser()).isEqualTo(fetchedUser);
        assertThat(watched.getWatchedAt().withNano(0)).isEqualTo(watchedAtTime); // Compare without nanos

        // Verify by fetching UserWatched directly
        UserMovieId watchedId = new UserMovieId(user1.getId(), movie1.getId());
        Optional<UserWatched> fetchedWatchedDirectly = userWatchedRepository.findById(watchedId);
        assertThat(fetchedWatchedDirectly).isPresent();
        assertThat(fetchedWatchedDirectly.get().getWatchedAt().withNano(0)).isEqualTo(watchedAtTime);

        // Verify inverse side on Movie
        Movie fetchedMovie = movieRepository.findById(movie1.getId()).orElseThrow();
        assertThat(fetchedMovie.getWatchedBy()).hasSize(1);
        assertThat(fetchedMovie.getWatchedBy().iterator().next().getUser()).isEqualTo(fetchedUser);
    }

    @Test
    void testUserWatchedReviewAndContentFlagRelationship() {
        User reviewer = createUser("criticUser");
        User reporter = createUser("reporterUser");
        Movie movieToReview = createMovie("Seven_Review");
        LocalDateTime watchedAtTime = LocalDateTime.now().minusHours(5).withNano(0);

        // 1. User watches movie
        reviewer.addWatched(movieToReview, watchedAtTime);
        userRepository.save(reviewer); // Persist reviewer, cascades to UserWatched

        // Fetch UserWatched to ensure it's managed and has an ID for Review's FK
        UserMovieId userWatchedId = new UserMovieId(reviewer.getId(), movieToReview.getId());
        UserWatched userWatchedEntry = userWatchedRepository.findById(userWatchedId).orElseThrow();

        // 2. User reviews watched movie
        userWatchedEntry.addReview("A masterpiece of suspense!", 5, 4, 5, 5);
        userWatchedRepository.save(userWatchedEntry); // Saving UserWatched cascades to Review

        // Verify Review
        // Review's ID is now a simple Long (from BaseEntity)
        assertThat(userWatchedEntry.getReview()).isNotNull();
        Long reviewId = userWatchedEntry.getReview().getId();
        assertThat(reviewId).isNotNull(); // Ensure review got an ID

        Review fetchedReview = reviewRepository.findById(reviewId).orElseThrow();
        assertThat(fetchedReview.getContent()).isEqualTo("A masterpiece of suspense!");
        assertThat(fetchedReview.getUserWatched()).isEqualTo(userWatchedEntry); // Check link back
        assertThat(fetchedReview.getGeneralScore()).isEqualTo(5);

        // 3. Another user flags the review
        String flagReason = "Major spoiler in the first line!";
        reporter.addFlaggedContent(fetchedReview, flagReason);
        userRepository.save(reporter); // Persist reporter, cascades to ContentFlag

        // Verify ContentFlag
        UserReviewId contentFlagId = new UserReviewId(reporter.getId(), fetchedReview.getId());
        ContentFlag fetchedFlag = contentFlagRepository.findById(contentFlagId).orElseThrow();
        
        assertThat(fetchedFlag.getFlagReason()).isEqualTo(flagReason);
        assertThat(fetchedFlag.getUser()).isEqualTo(reporter); // User who flagged
        assertThat(fetchedFlag.getReview()).isEqualTo(fetchedReview); // Review that was flagged
        //System.out.println("ContentFlag createdAt: " + fetchedFlag.getCreatedAt());

        // Verify collections
        User fetchedReporter = userRepository.findById(reporter.getId()).orElseThrow();
        assertThat(fetchedReporter.getFlaggedContent()).hasSize(1);
        assertThat(fetchedReporter.getFlaggedContent()).contains(fetchedFlag);

        Review reviewWithFlags = reviewRepository.findById(fetchedReview.getId()).orElseThrow();
        assertThat(reviewWithFlags.getFlags()).hasSize(1);
        assertThat(reviewWithFlags.getFlags()).contains(fetchedFlag);
    }
}
