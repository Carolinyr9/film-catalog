package br.ifsp.film_catalog.genre;

import br.ifsp.film_catalog.dto.GenreRequestDTO;
import br.ifsp.film_catalog.model.Genre;
import br.ifsp.film_catalog.model.Movie;
import br.ifsp.film_catalog.model.enums.ContentRating;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin", roles = {"ADMIN"})
class GenreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Genre genre;

    @BeforeEach
    void setup() {
        movieRepository.deleteAll();
        genreRepository.deleteAll();

        genre = new Genre();
        genre.setName("Action");
        genre = genreRepository.save(genre);
    }

    @Test
    void shouldReturnPagedGenres() throws Exception {
        mockMvc.perform(get("/api/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void shouldReturnGenreById() throws Exception {
        mockMvc.perform(get("/api/genres/{id}", genre.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Action"));
    }

    @Test
    void shouldReturn404WhenGenreNotFound() throws Exception {
        mockMvc.perform(get("/api/genres/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateGenre() throws Exception {
        GenreRequestDTO dto = new GenreRequestDTO();
        dto.setName("Drama");

        mockMvc.perform(post("/api/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Drama"));
    }

    @Test
    void shouldNotCreateGenreWithDuplicateName() throws Exception {
        GenreRequestDTO dto = new GenreRequestDTO();
        dto.setName("Action");

        mockMvc.perform(post("/api/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateGenre() throws Exception {
        GenreRequestDTO dto = new GenreRequestDTO();
        dto.setName("Updated Genre");

        mockMvc.perform(put("/api/genres/{id}", genre.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Genre"));
    }

    @Test
    void shouldNotUpdateToDuplicateGenreName() throws Exception {
        Genre other = new Genre();
        other.setName("Horror");
        genreRepository.save(other);

        GenreRequestDTO dto = new GenreRequestDTO();
        dto.setName("Horror");

        mockMvc.perform(put("/api/genres/{id}", genre.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDeleteGenre() throws Exception {
        mockMvc.perform(delete("/api/genres/{id}", genre.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn409WhenDeletingGenreLinkedToMovie() throws Exception {
        Genre persistedGenre = genreRepository.save(new Genre("Drama")); // ou recuperar um existente
        Movie movie = new Movie();
        movie.setTitle("Linked Movie");
        movie.setSynopsis("Linked");
        movie.setReleaseYear(2024);
        movie.setDuration(120);
        movie.setContentRating(ContentRating.A14);
        movie.addGenre(persistedGenre); // use o genre gerenciado
        movieRepository.save(movie);

        mockMvc.perform(delete("/api/genres/{id}", persistedGenre.getId()))
                .andExpect(status().isConflict());
    }

}
