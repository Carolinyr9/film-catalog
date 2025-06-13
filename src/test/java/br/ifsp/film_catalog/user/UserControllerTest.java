package br.ifsp.film_catalog.user;

import br.ifsp.film_catalog.config.CustomUserDetails;
import br.ifsp.film_catalog.config.SecurityService;
import br.ifsp.film_catalog.controller.UserController;
import br.ifsp.film_catalog.dto.*;
import br.ifsp.film_catalog.dto.page.PagedResponse;
import br.ifsp.film_catalog.exception.InvalidMovieStateException;
import br.ifsp.film_catalog.exception.ResourceNotFoundException;
import br.ifsp.film_catalog.repository.MovieRepository;
import br.ifsp.film_catalog.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class UserControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private UserService userService;

        @MockBean
        private MovieRepository movieRepository;

        @MockBean
        private SecurityService securityService;

        @Autowired
        private ObjectMapper objectMapper;

        private UserResponseDTO exampleUser;

        @BeforeEach
        void setup() {
                exampleUser = UserResponseDTO.builder()
                                .id(1L)
                                .name("João Silva")
                                .username("joaosilva")
                                .email("joao@example.com")
                                .build();
        }

        public Authentication getAuthentication() {
                CustomUserDetails principal = new CustomUserDetails(
                                1L,
                                "joaosilva",
                                "password",
                                List.of(new SimpleGrantedAuthority("ROLE_USER")));
                return new UsernamePasswordAuthenticationToken(principal, principal.getPassword(),
                                principal.getAuthorities());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void getAllUsers_shouldReturnPagedUsers_whenAdmin() throws Exception {
                PagedResponse<UserResponseDTO> pagedUsers = new PagedResponse<>(
                                List.of(exampleUser), 1, 0, 10, 1, true);
                when(userService.getAllUsers(any(Pageable.class))).thenReturn(pagedUsers);

                mockMvc.perform(get("/api/users"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].id").value(exampleUser.getId()))
                                .andExpect(jsonPath("$.content[0].username").value(exampleUser.getUsername()));

                verify(userService).getAllUsers(any(Pageable.class));
        }

        @Test
        @WithMockUser(roles = { "ADMIN" })
        void getUserById_shouldReturnUser_whenFound() throws Exception {
                when(userService.getUserById(1L)).thenReturn(exampleUser);

                mockMvc.perform(get("/api/users/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(exampleUser.getId()))
                                .andExpect(jsonPath("$.username").value(exampleUser.getUsername()));
        }

        @Test
        void registerUser_shouldCreateUser_whenValid() throws Exception {
                UserRequestDTO request = new UserRequestDTO();
                request.setUsername("novousuario");
                request.setEmail("novo@example.com");
                request.setName("Novo Usuário");
                request.setPassword("senha123@A");

                UserResponseDTO createdUser = UserResponseDTO.builder()
                                .id(2L)
                                .username("novousuario")
                                .email("novo@example.com")
                                .name("Novo Usuário")
                                .build();

                when(userService.createUser(any(UserRequestDTO.class))).thenReturn(createdUser);

                mockMvc.perform(post("/api/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(2L))
                                .andExpect(jsonPath("$.username").value("novousuario"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void createUserByAdmin_shouldCreateUser_whenValid() throws Exception {
                UserRequestWithRolesDTO request = new UserRequestWithRolesDTO();
                request.setUsername("admincreated");
                request.setEmail("admincreated@example.com");
                request.setName("Admin Created");
                request.setPassword("Senha123!");

                RoleRequestDTO roleUser = new RoleRequestDTO("ROLE_USER");
                Set<RoleRequestDTO> roles = Set.of(roleUser);
                request.setRoles(roles);

                UserResponseDTO createdUser = UserResponseDTO.builder()
                                .id(3L)
                                .username("admincreated")
                                .email("admincreated@example.com")
                                .name("Admin Created")
                                .build();

                when(userService.createUser(any(UserRequestWithRolesDTO.class))).thenReturn(createdUser);

                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andDo(print()) // imprime request/resposta para debug
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(3L))
                                .andExpect(jsonPath("$.username").value("admincreated"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void updateUser_shouldReturnUpdatedUser_whenSuccess() throws Exception {
                UserRequestWithRolesDTO updateRequest = new UserRequestWithRolesDTO();
                updateRequest.setUsername("updateduser");
                updateRequest.setEmail("updated@example.com");
                updateRequest.setName("Updated User");
                updateRequest.setPassword("novaSenha@123F");
                RoleRequestDTO roleUser = new RoleRequestDTO();
                roleUser.setRoleName("ROLE_USER");
                Set<RoleRequestDTO> roles = Set.of(roleUser);
                updateRequest.setRoles(roles);

                UserResponseDTO updatedUser = UserResponseDTO.builder()
                                .id(1L)
                                .username("updateduser")
                                .email("updated@example.com")
                                .name("Updated User")
                                .build();

                when(userService.updateUser(eq(1L), any(UserRequestWithRolesDTO.class))).thenReturn(updatedUser);

                mockMvc.perform(put("/api/users/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.username").value("updateduser"));

        }

        @Test
        @WithMockUser(username = "joaosilva")
        void patchUser_shouldReturnPatchedUser_whenSuccess() throws Exception {
                Map<String, Object> patchBody = Map.of("name", "Nome Patch");

                UserResponseDTO patchedUser = UserResponseDTO.builder()
                                .id(1L)
                                .username("joaosilva")
                                .name("Nome Patch")
                                .email("joao@example.com")
                                .build();

                when(userService.patchUser(eq(1L), any(UserPatchDTO.class))).thenReturn(patchedUser);
                when(securityService.isOwner(any(), eq("1"))).thenReturn(true);

                mockMvc.perform(patch("/api/users/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(patchBody)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Nome Patch"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void deleteUser_shouldReturnNoContent_whenDeleted() throws Exception {
                doNothing().when(userService).deleteUser(1L);

                mockMvc.perform(delete("/api/users/1"))
                                .andExpect(status().isNoContent());

                verify(userService).deleteUser(1L);
        }

        @Test
        @WithMockUser(username = "admin", roles = { "ADMIN" })
        void addFavoriteMovie_shouldReturnOk_whenSuccess() throws Exception {
                doNothing().when(userService).addFavoriteMovie(1L, 2L);

                mockMvc.perform(post("/api/users/1/favorites/2"))
                                .andExpect(status().isOk());

                verify(userService).addFavoriteMovie(1L, 2L);
        }

        @Test
        @WithMockUser(username = "joaosilva")
        void getFavoriteMovies_shouldReturnPagedMovies_whenSuccess() throws Exception {
                MovieResponseDTO movie = MovieResponseDTO.builder()
                                .id(2L)
                                .title("Filme Favorito")
                                .build();
                PagedResponse<MovieResponseDTO> pagedMovies = new PagedResponse<>(
                                List.of(movie), 1, 0, 10, 1, true);

                when(userService.getFavoriteMovies(eq(1L), any(Pageable.class))).thenReturn(pagedMovies);

                when(securityService.isOwner(any(), eq("1"))).thenReturn(true);

                mockMvc.perform(get("/api/users/1/favorites"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].title").value("Filme Favorito"));
        }

        // --- getAllUsers ---

        @Test
        void getAllUsers_shouldReturn401_whenUnauthorized() throws Exception {
                mockMvc.perform(get("/api/users"))
                                .andExpect(status().isUnauthorized());
                verifyNoInteractions(userService);
        }

        @Test
        @WithMockUser(roles = "USER")
        void getAllUsers_shouldReturn403_whenForbidden() throws Exception {
                mockMvc.perform(get("/api/users"))
                                .andExpect(status().isForbidden());
                verifyNoInteractions(userService);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void getAllUsers_shouldHandlePaginationAndSorting() throws Exception {
                PagedResponse<UserResponseDTO> pagedUsers = new PagedResponse<>(
                                List.of(exampleUser), 1, 0, 5, 1, true);
                when(userService.getAllUsers(any(Pageable.class))).thenReturn(pagedUsers);

                mockMvc.perform(get("/api/users")
                                .param("page", "0")
                                .param("size", "5")
                                .param("sort", "username,desc"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].id").value(exampleUser.getId()));

                verify(userService).getAllUsers(any(Pageable.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void getAllUsers_shouldReturnEmptyList_whenNoUsersExist() throws Exception {
                PagedResponse<UserResponseDTO> emptyPage = new PagedResponse<>(
                                List.of(), 0, 0, 10, 0, false);
                when(userService.getAllUsers(any(Pageable.class))).thenReturn(emptyPage);

                mockMvc.perform(get("/api/users"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isEmpty());

                verify(userService).getAllUsers(any(Pageable.class));
        }

        // --- getUserById ---

        @Test
        void getUserById_shouldReturn401_whenUnauthorized() throws Exception {
                mockMvc.perform(get("/api/users/1"))
                                .andExpect(status().isUnauthorized());
                verifyNoInteractions(userService);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void getUserById_shouldReturn404_whenNotFound() throws Exception {
                when(userService.getUserById(999L)).thenThrow(new ResourceNotFoundException("User not found"));

                mockMvc.perform(get("/api/users/999"))
                                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "USER", username = "joaosilva")
        void getUserById_shouldReturn400_whenInvalidIdFormat() throws Exception {
                mockMvc.perform(get("/api/users/invalidId"))
                                .andExpect(status().isBadRequest());
                verifyNoInteractions(userService);
        }

        // --- getUserByUsername ---

        @Test
        void getUserByUsername_shouldReturn401_whenUnauthorized() throws Exception {
                mockMvc.perform(get("/api/users/search/by-username").param("username", "joaosilva"))
                                .andExpect(status().isUnauthorized());
                verifyNoInteractions(userService);
        }

        @Test
        @WithMockUser(roles = "USER", username = "otheruser")
        void getUserByUsername_shouldReturn403_whenNotOwnerAndNotAdmin() throws Exception {
                when(securityService.isOwner(any(), eq("joaosilva"))).thenReturn(false);

                mockMvc.perform(get("/api/users/search/by-username").param("username", "joaosilva"))
                                .andExpect(status().isForbidden());

                verifyNoInteractions(userService);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void getUserByUsername_shouldReturn404_whenNotFound() throws Exception {
                when(userService.getUserByUsername("nonexistent"))
                                .thenThrow(new ResourceNotFoundException("User not found"));

                mockMvc.perform(get("/api/users/search/by-username").param("username", "nonexistent"))
                                .andExpect(status().isNotFound());
        }

        // --- registerUser ---

        @Test
        void registerUser_shouldReturn400_whenInvalidPayload() throws Exception {
                UserRequestDTO invalidRequest = new UserRequestDTO(); // sem campos preenchidos

                mockMvc.perform(post("/api/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void registerUser_shouldReturn409_whenUsernameAlreadyExists() throws Exception {
                UserRequestDTO request = new UserRequestDTO();
                request.setUsername("joaosilva");
                request.setEmail("new@example.com");
                request.setName("Nome");
                request.setPassword("Senha123@");

                when(userService.createUser(any(UserRequestDTO.class)))
                                .thenThrow(new InvalidMovieStateException("Username already exists"));

                mockMvc.perform(post("/api/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isConflict());
        }

        @Test
        void registerUser_shouldReturn409_whenEmailAlreadyExists() throws Exception {
                UserRequestDTO request = new UserRequestDTO();
                request.setUsername("newuser");
                request.setEmail("joao@example.com");
                request.setName("Nome");
                request.setPassword("Senha123@");

                when(userService.createUser(any(UserRequestDTO.class)))
                                .thenThrow(new InvalidMovieStateException("Email already exists"));

                mockMvc.perform(post("/api/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isConflict());
        }

        @Test
        void registerUser_shouldHandleMissingFields() throws Exception {
                UserRequestDTO incompleteRequest = new UserRequestDTO();
                incompleteRequest.setUsername("user");
                // falta email, password etc

                mockMvc.perform(post("/api/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(incompleteRequest)))
                                .andExpect(status().isBadRequest());
        }

        // --- createUserByAdmin ---

        @Test
        void createUserByAdmin_shouldReturn401_whenUnauthorized() throws Exception {
                UserRequestWithRolesDTO request = new UserRequestWithRolesDTO();
                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "USER")
        void createUserByAdmin_shouldReturn403_whenNotAdmin() throws Exception {
                UserRequestWithRolesDTO request = new UserRequestWithRolesDTO();

                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void createUserByAdmin_shouldReturn400_whenInvalidPayload() throws Exception {
                UserRequestWithRolesDTO invalidRequest = new UserRequestWithRolesDTO();

                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void createUserByAdmin_shouldReturn400_whenUsernameOrEmailAlreadyExists() throws Exception {
                UserRequestWithRolesDTO request = new UserRequestWithRolesDTO();
                request.setUsername("joaosilva");
                request.setEmail("joao@example.com");

                when(userService.createUser(any(UserRequestDTO.class)))
                                .thenThrow(new InvalidMovieStateException("Username or email exists"));

                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void createUserByAdmin_shouldReturn400_whenInvalidRole() throws Exception {
                UserRequestWithRolesDTO request = new UserRequestWithRolesDTO();
                request.setUsername("userx");
                request.setEmail("userx@example.com");
                RoleRequestDTO invalidRole = new RoleRequestDTO("INVALID_ROLE");
                request.setRoles(Set.of(invalidRole));

                when(userService.createUser(any(UserRequestWithRolesDTO.class)))
                                .thenThrow(new InvalidMovieStateException("Invalid role"));

                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        // --- updateUser ---

        @Test
        void updateUser_shouldReturn401_whenUnauthorized() throws Exception {
                UserRequestWithRolesDTO request = new UserRequestWithRolesDTO();
                mockMvc.perform(put("/api/users/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "USER")
        void updateUser_shouldReturn403_whenNotAdmin() throws Exception {
                UserRequestWithRolesDTO request = new UserRequestWithRolesDTO();
                mockMvc.perform(put("/api/users/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void updateUser_shouldReturn400_whenInvalidPayload() throws Exception {
                UserRequestWithRolesDTO invalidRequest = new UserRequestWithRolesDTO();

                mockMvc.perform(put("/api/users/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void updateUser_shouldReturn400_whenUsernameOrEmailAlreadyExists() throws Exception {
                UserRequestWithRolesDTO request = new UserRequestWithRolesDTO();
                request.setUsername("joaosilva");
                request.setEmail("joao@example.com");

                when(userService.updateUser(eq(1L), any()))
                                .thenThrow(new InvalidMovieStateException("Username or email exists"));

                mockMvc.perform(put("/api/users/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        // --- patchUser ---

        @Test
        void patchUser_shouldReturn401_whenUnauthorized() throws Exception {
                Map<String, Object> patchBody = Map.of("name", "Nome");
                mockMvc.perform(patch("/api/users/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(patchBody)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "USER", username = "joaosilva")
        void patchUser_shouldReturn404_whenNotFound() throws Exception {
                Map<String, Object> patchBody = Map.of("name", "Nome");
                when(userService.patchUser(eq(1L), any())).thenThrow(new ResourceNotFoundException("User not found"));
                when(securityService.isOwner(any(), eq("1"))).thenReturn(true);

                mockMvc.perform(patch("/api/users/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(patchBody)))
                                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "USER", username = "joaosilva")
        void patchUser_shouldReturn400_whenInvalidPayload() throws Exception {
                Map<String, Object> patchBody = Map.of("email", "not-an-email");
                when(securityService.isOwner(any(), eq("1"))).thenReturn(true);

                mockMvc.perform(patch("/api/users/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(patchBody)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "USER", username = "joaosilva")
        void patchUser_shouldReturn409_whenUsernameOrEmailAlreadyExists() throws Exception {
                Map<String, Object> patchBody = Map.of("username", "joaosilva");
                when(userService.patchUser(eq(1L), any()))
                                .thenThrow(new InvalidMovieStateException("Username or email exists"));
                when(securityService.isOwner(any(), eq("1"))).thenReturn(true);

                mockMvc.perform(patch("/api/users/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(patchBody)))
                                .andExpect(status().isConflict());
        }

        @Test
        @WithMockUser(roles = "USER", username = "joaosilva")
        void patchUser_shouldUpdateOnlySpecifiedFields() throws Exception {
                Map<String, Object> patchBody = Map.of("name", "Novo Nome");

                UserResponseDTO patchedUser = UserResponseDTO.builder()
                                .id(1L)
                                .username("joaosilva")
                                .name("Novo Nome")
                                .email("joao@example.com")
                                .build();

                when(userService.patchUser(eq(1L), any())).thenReturn(patchedUser);
                when(securityService.isOwner(any(), eq("1"))).thenReturn(true);

                mockMvc.perform(patch("/api/users/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(patchBody)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Novo Nome"));
        }

        // --- deleteUser ---

        @Test
        void deleteUser_shouldReturn401_whenUnauthorized() throws Exception {
                mockMvc.perform(delete("/api/users/1"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "USER")
        void deleteUser_shouldReturn403_whenNotAdmin() throws Exception {
                mockMvc.perform(delete("/api/users/1"))
                                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void deleteUser_shouldReturn404_whenNotFound() throws Exception {
                doThrow(new ResourceNotFoundException("User not found")).when(userService).deleteUser(999L);

                mockMvc.perform(delete("/api/users/999"))
                                .andExpect(status().isNotFound());
        }

        // --- addFavoriteMovie ---

        @Test
        void addFavoriteMovie_shouldReturn401_whenUnauthorized() throws Exception {
                mockMvc.perform(post("/api/users/1/favorites/2"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void addFavoriteMovie_shouldReturn404_whenUserOrMovieNotFound() throws Exception {
                doThrow(new ResourceNotFoundException("User or Movie not found")).when(userService).addFavoriteMovie(1L,
                                999L);

                mockMvc.perform(post("/api/users/1/favorites/999"))
                                .andExpect(status().isNotFound());
        }

        // --- removeFavoriteMovie ---

        @Test
        @WithMockUser(username = "joaosilva")
        void removeFavoriteMovie_shouldReturn200_whenSuccess() throws Exception {
                doNothing().when(userService).removeFavoriteMovie(1L, 2L);
                when(securityService.isOwner(any(), eq("1"))).thenReturn(true);

                mockMvc.perform(delete("/api/users/1/favorites/2"))
                                .andExpect(status().isOk());

                verify(userService).removeFavoriteMovie(1L, 2L);
        }

        @Test
        void removeFavoriteMovie_shouldReturn401_whenUnauthorized() throws Exception {
                mockMvc.perform(delete("/api/users/1/favorites/2"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "joaosilva")
        void unmarkMovieAsWatched_shouldReturn200_whenSuccess() throws Exception {
                doNothing().when(userService).removeWatchedMovie(1L, 2L);
                when(securityService.isOwner(any(), eq("1"))).thenReturn(true);

                mockMvc.perform(delete("/api/users/1/watched/2"))
                                .andExpect(status().isOk());
        }

        @Test
        void unmarkMovieAsWatched_shouldReturn401_whenUnauthorized() throws Exception {
                mockMvc.perform(delete("/api/users/1/watched/2"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "joaosilva")
        void unmarkMovieAsWatched_shouldReturn404_whenUserOrMovieOrWatchedEntryNotFound() throws Exception {
                when(securityService.isOwner(any(), eq("1"))).thenReturn(true);
                doThrow(new ResourceNotFoundException("Not found")).when(userService).removeWatchedMovie(1L, 2L);

                mockMvc.perform(delete("/api/users/1/watched/2"))
                                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(username = "joaosilva")
        void getWatchedMovies_shouldReturn200_whenSuccess() throws Exception {
                MovieResponseDTO movie = MovieResponseDTO.builder().id(3L).title("Filme B").build();
                PagedResponse<MovieResponseDTO> page = new PagedResponse<>(List.of(movie), 1, 0, 10, 1, true);

                when(securityService.isOwner(any(), eq("1"))).thenReturn(true);
                when(userService.getWatchedMovies(eq(1L), any(Pageable.class))).thenReturn(page);

                mockMvc.perform(get("/api/users/1/watched"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].id").value(3L))
                                .andExpect(jsonPath("$.content[0].title").value("Filme B"));
        }

        @Test
        void getWatchedMovies_shouldReturn401_whenUnauthorized() throws Exception {
                mockMvc.perform(get("/api/users/1/watched"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "joaosilva")
        void getWatchedMovies_shouldReturn404_whenUserNotFound() throws Exception {
                when(securityService.isOwner(any(), eq("1"))).thenReturn(true);
                when(userService.getWatchedMovies(eq(1L), any(Pageable.class)))
                                .thenThrow(new ResourceNotFoundException("User not found"));

                mockMvc.perform(get("/api/users/1/watched"))
                                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(username = "joaosilva")
        void getWatchedMovies_shouldReturnEmptyList_whenNoWatchedMovies() throws Exception {
                PagedResponse<MovieResponseDTO> emptyPage = new PagedResponse<>(List.of(), 0, 0, 10, 0, true);

                when(securityService.isOwner(any(), eq("1"))).thenReturn(true);
                when(userService.getWatchedMovies(eq(1L), any(Pageable.class))).thenReturn(emptyPage);

                mockMvc.perform(get("/api/users/1/watched"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isEmpty());
        }

        @Test
        @WithMockUser(username = "joaosilva")
        void getWatchedMovies_shouldHandlePaginationAndSorting() throws Exception {
                MovieResponseDTO movie = MovieResponseDTO.builder().id(9L).title("Filme C").build();
                PagedResponse<MovieResponseDTO> page = new PagedResponse<>(List.of(movie), 1, 0, 1, 1, true);

                when(securityService.isOwner(any(), eq("1"))).thenReturn(true);
                when(userService.getWatchedMovies(eq(1L), any(Pageable.class))).thenReturn(page);

                mockMvc.perform(get("/api/users/1/watched?page=0&size=1&sort=title,desc"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].id").value(9L))
                                .andExpect(jsonPath("$.content[0].title").value("Filme C"));
        }

        @Test
        void getPersonalizedRecommendations_shouldReturn401_whenUnauthorized() throws Exception {
                mockMvc.perform(get("/api/recommendations/personalized")
                        .param("page", "0")
                        .param("size", "10"))
                        .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "user1")
        void markMovieAsWatched_shouldReturn200_whenSuccess() throws Exception {
                long userId = 1L;
                long movieId = 10L;

                mockMvc.perform(post("/api/users/{userId}/watched/{movieId}", userId, movieId))
                        .andExpect(status().isOk());
        }

}