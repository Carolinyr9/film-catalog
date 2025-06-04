package br.ifsp.film_catalog.unitTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import br.ifsp.film_catalog.dto.page.PagedResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.ifsp.film_catalog.dto.MovieResponseDTO;
import br.ifsp.film_catalog.dto.RoleRequestDTO;
import br.ifsp.film_catalog.dto.UserRequestWithRolesDTO;
import br.ifsp.film_catalog.dto.UserResponseDTO;
import br.ifsp.film_catalog.exception.ResourceNotFoundException;
import br.ifsp.film_catalog.mapper.PagedResponseMapper;
import br.ifsp.film_catalog.model.Genre;
import br.ifsp.film_catalog.model.Movie;
import br.ifsp.film_catalog.model.Role;
import br.ifsp.film_catalog.model.User;
import br.ifsp.film_catalog.model.UserFavorite;
import br.ifsp.film_catalog.model.enums.ContentRating;
import br.ifsp.film_catalog.model.enums.RoleName;
import br.ifsp.film_catalog.model.key.UserMovieId;
import br.ifsp.film_catalog.repository.MovieRepository;
import br.ifsp.film_catalog.repository.RoleRepository;
import br.ifsp.film_catalog.repository.UserFavoriteRepository;
import br.ifsp.film_catalog.repository.UserRepository;
import br.ifsp.film_catalog.repository.UserWatchedRepository;
import br.ifsp.film_catalog.service.UserService;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private UserFavoriteRepository userFavoriteRepository;

    @Mock
    private UserWatchedRepository userWatchedRepository;

    @BeforeEach
    void setUp() throws Exception {
        Movie movie1 = new Movie();
        movie1.setId(1L);
        movie1.setTitle("Oppenheimer");
        movie1.setSynopsis("Biografia de um físico");
        movie1.setReleaseYear(2023);
        movie1.setDuration(180);
        movie1.setContentRating(ContentRating.A14); 
        movie1.getGenres().add(new Genre("Drama"));
        movie1.getGenres().add(new Genre("Histórico"));

        Long movieId1 = 1L;
    }

    @Test
    void createUser_shouldCreateUserSuccessfully() {
        UserRequestWithRolesDTO userRequestDTO = new UserRequestWithRolesDTO();
        userRequestDTO.setUsername("jessica");
        userRequestDTO.setEmail("jessica@email.com");
        userRequestDTO.setPassword("123456");

        RoleRequestDTO roleRequestDTO = new RoleRequestDTO();
        roleRequestDTO.setRoleName("ROLE_USER");
        Set<RoleRequestDTO> roles = new HashSet<>();
        roles.add(roleRequestDTO);
        userRequestDTO.setRoles(roles);

        User user = new User();
        user.setUsername("jessica");
        user.setEmail("jessica@email.com");
        user.setPassword("encodedPassword");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("jessica");

        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setUsername("jessica");

        Role role = new Role();
        role.setRoleName(RoleName.ROLE_USER);

        when(userRepository.existsByUsername("jessica")).thenReturn(false);
        when(userRepository.existsByEmail("jessica@email.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("encodedPassword");
        when(modelMapper.map(userRequestDTO, User.class)).thenReturn(user);
        when(roleRepository.findByRoleName(RoleName.ROLE_USER)).thenReturn(Optional.of(role));
        when(userRepository.save(user)).thenReturn(savedUser);
        when(modelMapper.map(savedUser, UserResponseDTO.class)).thenReturn(userResponseDTO);

        UserResponseDTO result = userService.createUser(userRequestDTO);

        assertEquals("jessica", result.getUsername());
        verify(userRepository).save(user);
    }
    
    @Test
    void updateUser_shouldUpdateUserSuccessfully() {
        Long userId = 1L;

        UserRequestWithRolesDTO dto = new UserRequestWithRolesDTO();
        dto.setName("Updated Name");
        dto.setEmail("updated@example.com");
        dto.setUsername("updatedUsername");
        dto.setPassword("newPassword");

        RoleRequestDTO roleDTO = new RoleRequestDTO();
        roleDTO.setRoleName("ROLE_USER");
        Set<RoleRequestDTO> roles = new HashSet<>();
        roles.add(roleDTO);
        dto.setRoles(roles);

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setUsername("oldUsername");
        existingUser.setEmail("old@example.com");
        existingUser.setName("Old Name");

        Role role = new Role();
        role.setId(1L);
        role.setRoleName(RoleName.ROLE_USER);

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setName(dto.getName());
        updatedUser.setEmail(dto.getEmail());
        updatedUser.setUsername(dto.getUsername());
        updatedUser.setPassword("encodedPassword");
        updatedUser.addRole(role);

        UserResponseDTO responseDTO = new UserResponseDTO();
        responseDTO.setId(userId);
        responseDTO.setName(dto.getName());
        responseDTO.setEmail(dto.getEmail());
        responseDTO.setUsername(dto.getUsername());

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername(dto.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase(dto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByRoleName(RoleName.ROLE_USER)).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(modelMapper.map(updatedUser, UserResponseDTO.class)).thenReturn(responseDTO);

        UserResponseDTO result = userService.updateUser(userId, dto);

        assertNotNull(result);
        assertEquals(dto.getName(), result.getName());
        assertEquals(dto.getEmail(), result.getEmail());
        assertEquals(dto.getUsername(), result.getUsername());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_shouldThrowWhenUserNotFound() {
        UserRequestWithRolesDTO requestDTO = new UserRequestWithRolesDTO();
        requestDTO.setUsername("updatedName");
        requestDTO.setEmail("updated@email.com");
        requestDTO.setPassword("newpass");

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
            userService.updateUser(1L, requestDTO)
        );

        assertEquals("User not found with id: 1", ex.getMessage());
    }

    @Test
    void updateUser_shouldThrowWhenUsernameAlreadyExistsForAnotherUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("original");

        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setUsername("conflict");

        UserRequestWithRolesDTO requestDTO = new UserRequestWithRolesDTO();
        requestDTO.setUsername("updatedName");
        requestDTO.setEmail("updated@email.com");
        requestDTO.setPassword("newpass");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("updatedName")).thenReturn(Optional.of(existingUser)); 

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            userService.updateUser(1L, requestDTO)
        );

        assertEquals("Username 'updatedName' already exists.", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteById_shouldDeleteUserWhenUserExists() {
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(true);

        userService.deleteUser(userId);

        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteById_shouldThrowExceptionWhenUserDoesNotExist() {
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.deleteUser(userId);
        });

        assertEquals("User not found with id: " + userId, exception.getMessage());
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void addFavoriteMovie_shouldAddFavoriteMovieSuccessfully() {
        Long userId = 1L;
        Long movieId = 100L;
        UserMovieId favoriteId = new UserMovieId(userId, movieId);

        User user = new User();
        user.setId(userId);

        Movie movie = new Movie();
        movie.setId(movieId);

        when(userFavoriteRepository.existsById(favoriteId)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));

        userService.addFavoriteMovie(userId, movieId);

        verify(userFavoriteRepository).save(any(UserFavorite.class));
    }


}