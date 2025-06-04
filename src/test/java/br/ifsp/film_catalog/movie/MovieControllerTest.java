package br.ifsp.film_catalog.movie;

import br.ifsp.film_catalog.dto.MovieRequestDTO;
import br.ifsp.film_catalog.model.Genre;
import br.ifsp.film_catalog.repository.GenreRepository;
import br.ifsp.film_catalog.repository.MovieRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Collections;
import java.util.Set;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin", roles = {"ADMIN"})
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private MovieRepository movieRepository;

    private Genre genre;

    @BeforeEach
    void setup() {
        // Clear movies first to avoid FK conflict
        movieRepository.deleteAll();
        genreRepository.deleteAll();

        genre = new Genre();
        genre.setName("Action");
        genre = genreRepository.save(genre);
    }

    @Test
    void shouldCreateMovieWithValidData() throws Exception {
        MovieRequestDTO dto = new MovieRequestDTO(
                "Test Movie",
                "A short synopsis",
                2024,
                120,
                "A14",
                Set.of(genre)
        );

        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Movie"))
                .andExpect(jsonPath("$.duration").value(120));
    }

    @Test
    void shouldRejectCreationWhenTitleIsBlank() throws Exception {
        MovieRequestDTO dto = new MovieRequestDTO(
                "",
                "Synopsis",
                2024,
                90,
                "A10",
                Set.of(genre)
        );

        mockMvc.perform(post("/api/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andDo(print())
        .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectCreationWhenGenresAreEmpty() throws Exception {
        MovieRequestDTO dto = new MovieRequestDTO(
                "Movie without genre",
                "Test",
                2023,
                100,
                "AL",
                Collections.emptySet()
        );

        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFetchAllMovies() throws Exception {
        mockMvc.perform(get("/api/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());
    }

    @Test
    void shouldFetchMoviesByTitle() throws Exception {
        mockMvc.perform(get("/api/movies/search/by-title")
                        .param("title", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());
    }

    @Test
    void shouldReturn404ForNonexistentMovie() throws Exception {
        mockMvc.perform(get("/api/movies/{id}", 9999))
                .andExpect(status().isNotFound());
    }
}
