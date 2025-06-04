package br.ifsp.film_catalog.watchlist;

import br.ifsp.film_catalog.dto.WatchlistRequestDTO;
import br.ifsp.film_catalog.model.Movie;
import br.ifsp.film_catalog.model.User;
import br.ifsp.film_catalog.model.Watchlist;
import br.ifsp.film_catalog.repository.MovieRepository;
import br.ifsp.film_catalog.repository.UserRepository;
import br.ifsp.film_catalog.repository.WatchlistRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import br.ifsp.film_catalog.model.enums.ContentRating;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin", roles = {"ADMIN"})
@ActiveProfiles("test")
class WatchlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WatchlistRepository watchlistRepository;

    @Autowired
    private MovieRepository movieRepository;

    private User user;
    private Movie movie;
    private Watchlist watchlist;

    @BeforeEach
    void setup() {
        watchlistRepository.deleteAll();
        movieRepository.deleteAll();
        userRepository.deleteAll();

        user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("123456");
        user = userRepository.save(user);

        movie = new Movie();
        movie.setTitle("Test Movie");
        movie.setSynopsis("A movie for testing.");
        movie.setReleaseYear(2024);
        movie.setDuration(100);
        movie.setContentRating(ContentRating.A18);
        movie = movieRepository.save(movie);

        watchlist = new Watchlist();
        watchlist.setName("My Watchlist");
        watchlist.setDescription("Description");
        watchlist.setUser(user);
        watchlist = watchlistRepository.save(watchlist);
    }

    @Test
    void shouldCreateWatchlist() throws Exception {
        WatchlistRequestDTO dto = new WatchlistRequestDTO("Favorites", "My favorite movies");

        mockMvc.perform(post("/api/users/{userId}/watchlists", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Favorites"));
    }

    @Test
    void shouldGetWatchlistsByUser() throws Exception {
        mockMvc.perform(get("/api/users/{userId}/watchlists", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());
    }

    @Test
    void shouldGetWatchlistByIdAndUser() throws Exception {
        mockMvc.perform(get("/api/users/{userId}/watchlists/{watchlistId}", user.getId(), watchlist.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("My Watchlist"));
    }

    @Test
    void shouldUpdateWatchlist() throws Exception {
        WatchlistRequestDTO dto = new WatchlistRequestDTO("Updated Name", "Updated Description");

        mockMvc.perform(put("/api/users/{userId}/watchlists/{watchlistId}", user.getId(), watchlist.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void shouldDeleteWatchlist() throws Exception {
        mockMvc.perform(delete("/api/users/{userId}/watchlists/{watchlistId}", user.getId(), watchlist.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldAddMovieToWatchlist() throws Exception {
        mockMvc.perform(post("/api/users/{userId}/watchlists/{watchlistId}/movies/{movieId}",
                        user.getId(), watchlist.getId(), movie.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movies[0].title").value("Test Movie"));
    }

    @Test
    void shouldRemoveMovieFromWatchlist() throws Exception {
        // First add it
        watchlist.addMovie(movie);
        watchlistRepository.save(watchlist);

        mockMvc.perform(delete("/api/users/{userId}/watchlists/{watchlistId}/movies/{movieId}",
                        user.getId(), watchlist.getId(), movie.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movies").isEmpty());
    }
}
