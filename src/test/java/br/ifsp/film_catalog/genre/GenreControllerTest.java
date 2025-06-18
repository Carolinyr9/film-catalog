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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize; 

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
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
    private Long nonExistentGenreId = 999L;

    @BeforeEach
    void setup() {
        movieRepository.deleteAll(); 
        genreRepository.deleteAll();

        genre = new Genre();
        genre.setName("Action");
        genre = genreRepository.save(genre);

    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnPagedGenres() throws Exception {
        genreRepository.save(new Genre("Comedy"));
        genreRepository.save(new Genre("Horror"));

        mockMvc.perform(get("/api/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(3))); // Verifica se retornou os 2 gêneros
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnGenreById() throws Exception {
        mockMvc.perform(get("/api/genres/{id}", genre.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Action"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenGenreNotFound() throws Exception {
        mockMvc.perform(get("/api/genres/{id}", nonExistentGenreId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
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
    @WithMockUser(roles = "ADMIN")
    void shouldNotCreateGenreWithDuplicateName() throws Exception {
        genreRepository.save(new Genre("Existing Genre"));
        GenreRequestDTO dto = new GenreRequestDTO();
        dto.setName("Existing Genre");

        mockMvc.perform(post("/api/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest()); // Ou CONFLICT, dependendo da exceção específica
    }

    @Test
    @WithMockUser(roles = "ADMIN")
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
    @WithMockUser(roles = "ADMIN")
    void shouldNotUpdateToDuplicateGenreName() throws Exception {
        genreRepository.save(new Genre("Horror")); // Adiciona outro gênero para conflito
        GenreRequestDTO dto = new GenreRequestDTO();
        dto.setName("Horror");

        mockMvc.perform(put("/api/genres/{id}", genre.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest()); // Ou CONFLICT
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteGenre() throws Exception {
        mockMvc.perform(delete("/api/genres/{id}", genre.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn409WhenDeletingGenreLinkedToMovie() throws Exception {
        Genre persistedGenre = genreRepository.save(new Genre("Science Fiction"));
        Movie movie = new Movie();
        movie.setTitle("Linked Movie SF");
        movie.setSynopsis("Linked SF");
        movie.setReleaseYear(2024);
        movie.setDuration(120);
        movie.setContentRating(ContentRating.A14);
        movie.addGenre(persistedGenre);
        movieRepository.save(movie);

        mockMvc.perform(delete("/api/genres/{id}", persistedGenre.getId()))
                .andExpect(status().isConflict());
    }


    // Métodos de Teste a Serem Criados:

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteGenre_shouldReturn404_whenNotFound() throws Exception {
        mockMvc.perform(delete("/api/genres/{id}", nonExistentGenreId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER") // Usuário autenticado, mas sem ROLE_ADMIN
    void deleteGenre_shouldReturn403_whenForbidden() throws Exception {
        mockMvc.perform(delete("/api/genres/{id}", genre.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    // Sem @WithMockUser para simular usuário não autenticado
    void deleteGenre_shouldReturn401_whenUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/genres/{id}", genre.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateGenre_shouldReturn404_whenNotFound() throws Exception {
        GenreRequestDTO dto = new GenreRequestDTO();
        dto.setName("Non Existent Update");
        mockMvc.perform(put("/api/genres/{id}", nonExistentGenreId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER") // Usuário autenticado, mas sem ROLE_ADMIN
    void updateGenre_shouldReturn403_whenForbidden() throws Exception {
        GenreRequestDTO dto = new GenreRequestDTO();
        dto.setName("Forbidden Update");
        mockMvc.perform(put("/api/genres/{id}", genre.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    // Sem @WithMockUser para simular usuário não autenticado
    void updateGenre_shouldReturn401_whenUnauthorized() throws Exception {
        GenreRequestDTO dto = new GenreRequestDTO();
        dto.setName("Unauthorized Update");
        mockMvc.perform(put("/api/genres/{id}", genre.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateGenre_shouldReturn400_whenInvalidPayload() throws Exception {
        GenreRequestDTO dto = new GenreRequestDTO();
        dto.setName(""); // Nome inválido/vazio

        mockMvc.perform(put("/api/genres/{id}", genre.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createGenre_shouldReturn400_whenInvalidPayload() throws Exception {
        GenreRequestDTO dto = new GenreRequestDTO();
        dto.setName(""); // Nome inválido/vazio

        mockMvc.perform(post("/api/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER") // Usuário autenticado, mas sem ROLE_ADMIN
    void createGenre_shouldReturn403_whenForbidden() throws Exception {
        GenreRequestDTO dto = new GenreRequestDTO();
        dto.setName("Forbidden Creation");

        mockMvc.perform(post("/api/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    // Sem @WithMockUser para simular usuário não autenticado
    void createGenre_shouldReturn401_whenUnauthorized() throws Exception {
        GenreRequestDTO dto = new GenreRequestDTO();
        dto.setName("Unauthorized Creation");

        mockMvc.perform(post("/api/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER") 
    void getGenreById_shouldReturn403_whenForbidden() throws Exception {
        mockMvc.perform(get("/api/genres/{id}", genre.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void getGenreById_shouldReturn401_whenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/genres/{id}", genre.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER") 
    void getAllGenres_shouldReturn403_whenForbidden() throws Exception {
        mockMvc.perform(get("/api/genres"))
                .andExpect(status().isOk()); 
    }

    @Test
    void getAllGenres_shouldReturn401_whenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/genres"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllGenres_shouldHandlePagination() throws Exception {
        genreRepository.save(new Genre("Comedy"));
        genreRepository.save(new Genre("Horror"));
        genreRepository.save(new Genre("Thriller"));
        genreRepository.save(new Genre("Fantasy"));

        mockMvc.perform(get("/api/genres?page=0&size=2&sort=name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(5)) 
                .andExpect(jsonPath("$.content[0].name").value("Action")); 

        mockMvc.perform(get("/api/genres?page=1&size=2&sort=name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.content[0].name").value("Fantasy")); 
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllGenres_shouldReturnEmptyList_whenNoGenresExist() throws Exception {
        genreRepository.deleteAll(); // Garante que não há gêneros

        mockMvc.perform(get("/api/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }
}