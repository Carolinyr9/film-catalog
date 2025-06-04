package br.ifsp.film_catalog.user;

import br.ifsp.film_catalog.config.SecurityService;
import br.ifsp.film_catalog.controller.UserController;
import br.ifsp.film_catalog.dto.*;
import br.ifsp.film_catalog.dto.page.PagedResponse;
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
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        return new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
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
    @WithMockUser(roles = {"ADMIN"})
    void getUserById_shouldReturnUser_whenFound() throws Exception {
        when(userService.getUserById(1L)).thenReturn(exampleUser);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(exampleUser.getId()))
                .andExpect(jsonPath("$.username").value(exampleUser.getUsername()));
    }

    @Test
    @WithMockUser(username = "joaosilva", roles = "USER")
    void getUserByUsername_shouldReturnUser_whenOwner() throws Exception {
        when(securityService.isOwner(any(), eq("joaosilva"))).thenReturn(true);
        when(userService.getUserByUsername("joaosilva")).thenReturn(exampleUser);

        mockMvc.perform(get("/api/users/search/by-username")
                .param("username", "joaosilva"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("joaosilva"));
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
                .andDo(print())  // imprime request/resposta para debug
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
    @WithMockUser(username = "admin", roles = {"ADMIN"})
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


}