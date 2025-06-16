package br.ifsp.film_catalog.movie;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.ifsp.film_catalog.model.Movie;
import br.ifsp.film_catalog.model.enums.ContentRating;
import br.ifsp.film_catalog.dto.GenreResponseDTO;
import br.ifsp.film_catalog.dto.MoviePatchDTO;
import br.ifsp.film_catalog.dto.MovieRequestDTO;
import br.ifsp.film_catalog.dto.MovieResponseDTO;
import br.ifsp.film_catalog.dto.page.PagedResponse;
import br.ifsp.film_catalog.exception.InvalidMovieStateException;
import br.ifsp.film_catalog.model.Genre;
import br.ifsp.film_catalog.model.Movie;
import br.ifsp.film_catalog.model.enums.ContentRating;
import br.ifsp.film_catalog.repository.MovieRepository;
import br.ifsp.film_catalog.service.MovieService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.ArgumentMatchers.any;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class MovieNotFuncionalTest {

    @Autowired
    private MockMvc mockMvc;

    // Removi o @MockBean MovieService para usar dados reais no repositório
    // @MockBean
    // private MovieService movieService;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MovieResponseDTO exampleMovie;
    private MovieRequestDTO validMovieRequest;

    @BeforeEach
    void setup() {
        // Popula o banco com filmes para os testes
        populateMovies();

        // Setup exemplo de DTO para outras possíveis operações
        exampleMovie = MovieResponseDTO.builder()
                .id(10L)
                .title("Exemplo de Filme")
                .synopsis("Descrição do filme de exemplo")
                .releaseYear(2020)
                .duration(120)
                .contentRating(ContentRating.A12)
                .genres(Set.of(new GenreResponseDTO(1L, "Action")))
                .build();

        Genre genre = new Genre();
        genre.setId(1L);
        genre.setName("Action");

        validMovieRequest = new MovieRequestDTO();
        validMovieRequest.setTitle("Novo Filme");
        validMovieRequest.setSynopsis("Descrição de um novo filme");
        validMovieRequest.setReleaseYear(2023);
        validMovieRequest.setDuration(120);
        validMovieRequest.setContentRating("A12");
        validMovieRequest.setGenres(Set.of(genre));
    }

    private void populateMovies() {
        movieRepository.deleteAll();

        for (int i = 1; i <= 10; i++) {
            Movie movie = Movie.builder()
                    .title("Filme " + i)
                    .synopsis("Sinopse do filme " + i)
                    .releaseYear(2000 + i)
                    .duration(100 + i)
                    .contentRating(ContentRating.A12)
                    .build();
            movieRepository.save(movie);
        }
    }

    @Test
    @WithMockUser
    void getAllMovies_shouldReturnConsistentDataAcrossPages() throws Exception {
        MvcResult resultPage0 = mockMvc.perform(get("/api/movies?page=0&size=5"))
                .andExpect(status().isOk())
                .andReturn();

        String jsonPage0 = resultPage0.getResponse().getContentAsString();
        System.out.println("JSON recebido página 0:\n" + jsonPage0);
        List<String> titlesPage0 = extractTitlesFromJson(jsonPage0);

        MvcResult resultPage1 = mockMvc.perform(get("/api/movies?page=1&size=5"))
                .andExpect(status().isOk())
                .andReturn();

        String jsonPage1 = resultPage1.getResponse().getContentAsString();
        System.out.println("JSON recebido página 1:\n" + jsonPage1);
        List<String> titlesPage1 = extractTitlesFromJson(jsonPage1);

        assertTrue(Collections.disjoint(titlesPage0, titlesPage1));
    }

    private List<String> extractTitlesFromJson(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        JsonNode items = root.get("content");
        List<String> titles = new ArrayList<>();
        if (items != null && items.isArray()) {
            for (JsonNode movie : items) {
                titles.add(movie.get("title").asText());
            }
        } else {
            throw new IllegalStateException("Campo 'content' ausente ou inválido");
        }
        return titles;
    }

    @Test
    @WithMockUser
    void getAllMovies_shouldRespondUnderOneSecondForThousandMovies() throws Exception {
        populateMovies(1000); // Popula 1000 filmes

        long start = System.currentTimeMillis();
        mockMvc.perform(get("/api/movies?page=0&size=20"))
                .andExpect(status().isOk());
        long duration = System.currentTimeMillis() - start;

        System.out.println("Tempo de resposta: " + duration + "ms");
        assertTrue(duration < 1000, "Tempo de resposta maior que 1 segundo");
    }

    private void populateMovies(int count) {
        movieRepository.deleteAll();
        for (int i = 1; i <= count; i++) {
            Movie movie = new Movie();
            movie.setTitle("Filme " + i);
            movie.setSynopsis("Sinopse do filme " + i);
            movie.setReleaseYear(2000 + i);
            movie.setDuration(100 + i);
            movie.setContentRating(ContentRating.A12);
            movieRepository.save(movie);
        }
    }

}
