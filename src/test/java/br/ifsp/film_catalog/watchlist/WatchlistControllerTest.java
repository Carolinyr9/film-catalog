package br.ifsp.film_catalog.watchlist;

import br.ifsp.film_catalog.config.SecurityService; // Importar SecurityService
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
import org.springframework.boot.test.mock.mockito.MockBean; // Importar MockBean
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import br.ifsp.film_catalog.model.enums.ContentRating;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootTest
@AutoConfigureMockMvc
// Removido @WithMockUser do nível da classe para testes de autorização mais
// granular.
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

    @MockBean // Adicionado MockBean para SecurityService
    private SecurityService securityService;

    private User user;
    private User otherUser; // Para testar acesso de outros usuários
    private Movie movie;
    private Watchlist watchlist;
    private Long nonExistentId = 999L; // ID que não deve existir

    @BeforeEach
    void setup() {
        watchlistRepository.deleteAll();
        movieRepository.deleteAll();
        userRepository.deleteAll();

        // Usuário principal para os testes
        user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user = userRepository.save(user);

        // Outro usuário para testar permissões
        otherUser = new User();
        otherUser.setName("Other User");
        otherUser.setEmail("other@example.com");
        otherUser.setPassword("password123");
        otherUser = userRepository.save(otherUser);

        // Filme para testes
        movie = new Movie();
        movie.setTitle("Test Movie");
        movie.setSynopsis("A movie for testing.");
        movie.setReleaseYear(2024);
        movie.setDuration(100);
        movie.setContentRating(ContentRating.A18);
        movie = movieRepository.save(movie);

        // Watchlist padrão associada ao 'user'
        watchlist = new Watchlist();
        watchlist.setName("My Watchlist");
        watchlist.setDescription("Description");
        watchlist.setUser(user);
        watchlist = watchlistRepository.save(watchlist);

        // Mock securityService para o user principal
        when(securityService.isOwner(any(), eq(user.getId().toString()))).thenReturn(true);
        // Mock securityService para o outro usuário
        when(securityService.isOwner(any(), eq(otherUser.getId().toString()))).thenReturn(true);
    }

    // --- Testes Já Existentes (Mantidos para referência) ---
    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void shouldCreateWatchlist() throws Exception {
        WatchlistRequestDTO dto = new WatchlistRequestDTO("Favorites", "My favorite movies");

        mockMvc.perform(post("/api/users/{userId}/watchlists", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Favorites"));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void shouldGetWatchlistsByUser() throws Exception {
        mockMvc.perform(get("/api/users/{userId}/watchlists", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void shouldGetWatchlistByIdAndUser() throws Exception {
        mockMvc.perform(get("/api/users/{userId}/watchlists/{watchlistId}", user.getId(), watchlist.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("My Watchlist"));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void shouldUpdateWatchlist() throws Exception {
        WatchlistRequestDTO dto = new WatchlistRequestDTO("Updated Name", "Updated Description");

        mockMvc.perform(put("/api/users/{userId}/watchlists/{watchlistId}", user.getId(), watchlist.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void shouldDeleteWatchlist() throws Exception {
        mockMvc.perform(delete("/api/users/{userId}/watchlists/{watchlistId}", user.getId(), watchlist.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void shouldAddMovieToWatchlist() throws Exception {
        mockMvc.perform(post("/api/users/{userId}/watchlists/{watchlistId}/movies/{movieId}",
                user.getId(), watchlist.getId(), movie.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movies[0].title").value("Test Movie"));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void shouldRemoveMovieFromWatchlist() throws Exception {
        // First add it
        watchlist.addMovie(movie);
        watchlistRepository.save(watchlist);

        mockMvc.perform(delete("/api/users/{userId}/watchlists/{watchlistId}/movies/{movieId}",
                user.getId(), watchlist.getId(), movie.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movies").isEmpty());
    }

    // --- Métodos de Teste a Serem Criados (Adicionais) ---

    // --- createWatchlist ---
    @Test
    // Sem @WithMockUser para simular usuário não autenticado
    void createWatchlist_shouldReturn401_whenUnauthorized() throws Exception {
        WatchlistRequestDTO dto = new WatchlistRequestDTO("Unauthorized List", "Desc");
        mockMvc.perform(post("/api/users/{userId}/watchlists", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void createWatchlist_shouldReturn404_whenUserNotFound() throws Exception {
        WatchlistRequestDTO dto = new WatchlistRequestDTO("User Not Found List", "Desc");
        mockMvc.perform(post("/api/users/{userId}/watchlists", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void createWatchlist_shouldReturn400_whenInvalidName() throws Exception {
        WatchlistRequestDTO emptyNameDto = new WatchlistRequestDTO("", "Desc");
        mockMvc.perform(post("/api/users/{userId}/watchlists", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyNameDto)))
                .andExpect(status().isBadRequest());

        WatchlistRequestDTO nullNameDto = new WatchlistRequestDTO(null, "Desc");
        mockMvc.perform(post("/api/users/{userId}/watchlists", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nullNameDto)))
                .andExpect(status().isBadRequest());

        String veryLongName = "a".repeat(256); // Exemplo: se o limite for 255
        WatchlistRequestDTO longNameDto = new WatchlistRequestDTO(veryLongName, "Desc");
        mockMvc.perform(post("/api/users/{userId}/watchlists", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(longNameDto)))
                .andExpect(status().isBadRequest());
    }

    // --- getWatchlistsByUser ---
    @Test
    // Sem @WithMockUser
    void getWatchlistsByUser_shouldReturn401_whenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/{userId}/watchlists", user.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void getWatchlistsByUser_shouldReturn404_whenUserNotFound() throws Exception {
        mockMvc.perform(get("/api/users/{userId}/watchlists", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void getWatchlistsByUser_shouldHandlePaginationAndSorting() throws Exception {
        // Adicionar mais watchlists para o usuário para testar paginação
        watchlistRepository.save(new Watchlist("WL A", user));
        watchlistRepository.save(new Watchlist("WL C", user));
        watchlistRepository.save(new Watchlist("WL B", user));

        // Testar paginação e ordenação por nome
        mockMvc.perform(get("/api/users/{userId}/watchlists", user.getId())
                .param("page", "0")
                .param("size", "2")
                .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name").value("My Watchlist")) // name of 'watchlist' from setup
                .andExpect(jsonPath("$.content[1].name").value("WL A"));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void getWatchlistsByUser_shouldReturnEmptyList_whenNoWatchlists() throws Exception {
        watchlistRepository.deleteAll(); // Limpa as watchlists do usuário

        mockMvc.perform(get("/api/users/{userId}/watchlists", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    // --- getWatchlistByIdAndUser ---
    @Test
    // Sem @WithMockUser
    void getWatchlistByIdAndUser_shouldReturn401_whenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/{userId}/watchlists/{watchlistId}", user.getId(), watchlist.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void getWatchlistByIdAndUser_shouldReturn404_whenWatchlistOrUserNotFound() throws Exception {
        // Usuário não encontrado
        mockMvc.perform(get("/api/users/{userId}/watchlists/{watchlistId}", nonExistentId, watchlist.getId()))
                .andExpect(status().isNotFound());

        // Watchlist não encontrada para o usuário
        mockMvc.perform(get("/api/users/{userId}/watchlists/{watchlistId}", user.getId(), nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void getWatchlistByIdAndUser_shouldReturn404_whenWatchlistBelongsToAnotherUser() throws Exception {
        // Cria uma watchlist para 'otherUser'
        Watchlist otherUserWatchlist = new Watchlist();
        otherUserWatchlist.setName("Other User List");
        otherUserWatchlist.setDescription("Desc");
        otherUserWatchlist.setUser(otherUser);
        otherUserWatchlist = watchlistRepository.save(otherUserWatchlist);

        // Tentar acessar a watchlist de 'otherUser' usando o ID de 'user'
        mockMvc.perform(get("/api/users/{userId}/watchlists/{watchlistId}", user.getId(), otherUserWatchlist.getId()))
                .andExpect(status().isNotFound()); // A service deve retornar 404 se a watchlist não for do userId
    }

    // --- updateWatchlist ---
    @Test
    // Sem @WithMockUser
    void updateWatchlist_shouldReturn401_whenUnauthorized() throws Exception {
        WatchlistRequestDTO dto = new WatchlistRequestDTO("Unauthorized Update", "Desc");
        mockMvc.perform(put("/api/users/{userId}/watchlists/{watchlistId}", user.getId(), watchlist.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void updateWatchlist_shouldReturn404_whenWatchlistOrUserNotFound() throws Exception {
        WatchlistRequestDTO dto = new WatchlistRequestDTO("Update", "Desc");
        // Usuário não encontrado
        mockMvc.perform(put("/api/users/{userId}/watchlists/{watchlistId}", nonExistentId, watchlist.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        // Watchlist não encontrada para o usuário
        mockMvc.perform(put("/api/users/{userId}/watchlists/{watchlistId}", user.getId(), nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void updateWatchlist_shouldReturn400_whenInvalidName() throws Exception {
        WatchlistRequestDTO emptyNameDto = new WatchlistRequestDTO("", "Desc");
        mockMvc.perform(put("/api/users/{userId}/watchlists/{watchlistId}", user.getId(), watchlist.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyNameDto)))
                .andExpect(status().isBadRequest());

        WatchlistRequestDTO nullNameDto = new WatchlistRequestDTO(null, "Desc");
        mockMvc.perform(put("/api/users/{userId}/watchlists/{watchlistId}", user.getId(), watchlist.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nullNameDto)))
                .andExpect(status().isBadRequest());

        String veryLongName = "a".repeat(256);
        WatchlistRequestDTO longNameDto = new WatchlistRequestDTO(veryLongName, "Desc");
        mockMvc.perform(put("/api/users/{userId}/watchlists/{watchlistId}", user.getId(), watchlist.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(longNameDto)))
                .andExpect(status().isBadRequest());
    }

    // --- deleteWatchlist ---
    @Test
    // Sem @WithMockUser
    void deleteWatchlist_shouldReturn401_whenUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/users/{userId}/watchlists/{watchlistId}", user.getId(), watchlist.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void deleteWatchlist_shouldReturn404_whenWatchlistOrUserNotFound() throws Exception {
        // Usuário não encontrado
        mockMvc.perform(delete("/api/users/{userId}/watchlists/{watchlistId}", nonExistentId, watchlist.getId()))
                .andExpect(status().isNotFound());

        // Watchlist não encontrada para o usuário
        mockMvc.perform(delete("/api/users/{userId}/watchlists/{watchlistId}", user.getId(), nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    // Sem @WithMockUser
    void addMovieToWatchlist_shouldReturn401_whenUnauthorized() throws Exception {
        mockMvc.perform(post("/api/users/{userId}/watchlists/{watchlistId}/movies/{movieId}",
                user.getId(), watchlist.getId(), movie.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void addMovieToWatchlist_shouldReturn404_whenWatchlistUserOrMovieNotFound() throws Exception {
        // Usuário não encontrado
        mockMvc.perform(post("/api/users/{userId}/watchlists/{watchlistId}/movies/{movieId}",
                nonExistentId, watchlist.getId(), movie.getId()))
                .andExpect(status().isNotFound());

        // Watchlist não encontrada
        mockMvc.perform(post("/api/users/{userId}/watchlists/{watchlistId}/movies/{movieId}",
                user.getId(), nonExistentId, movie.getId()))
                .andExpect(status().isNotFound());

        // Filme não encontrado
        mockMvc.perform(post("/api/users/{userId}/watchlists/{watchlistId}/movies/{movieId}",
                user.getId(), watchlist.getId(), nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void addMovieToWatchlist_shouldReturn200_whenMovieAlreadyInWatchlist() throws Exception {
        watchlist.addMovie(movie);
        watchlistRepository.save(watchlist);

        mockMvc.perform(post("/api/users/{userId}/watchlists/{watchlistId}/movies/{movieId}",
                user.getId(), watchlist.getId(), movie.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void removeMovieFromWatchlist_shouldReturn401_whenUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/users/{userId}/watchlists/{watchlistId}/movies/{movieId}",
                user.getId(), watchlist.getId(), movie.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void removeMovieFromWatchlist_shouldReturn404_whenWatchlistUserMovieOrAssociationNotFound() throws Exception {

        mockMvc.perform(delete("/api/users/{userId}/watchlists/{watchlistId}/movies/{movieId}",
                nonExistentId, watchlist.getId(), movie.getId()))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/users/{userId}/watchlists/{watchlistId}/movies/{movieId}",
                user.getId(), nonExistentId, movie.getId()))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/users/{userId}/watchlists/{watchlistId}/movies/{movieId}",
                user.getId(), watchlist.getId(), nonExistentId))
                .andExpect(status().isNotFound());

        watchlist.removeMovie(movie); 
        watchlistRepository.save(watchlist);
        mockMvc.perform(delete("/api/users/{userId}/watchlists/{watchlistId}/movies/{movieId}",
                user.getId(), watchlist.getId(), movie.getId()))
                .andExpect(status().isOk()); 
    }


}