package br.ifsp.film_catalog.unitTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;

import br.ifsp.film_catalog.controller.UserController;
import br.ifsp.film_catalog.dto.UserPatchDTO;
import br.ifsp.film_catalog.dto.UserRequestDTO;
import br.ifsp.film_catalog.dto.UserResponseDTO;
import br.ifsp.film_catalog.dto.page.PagedResponse;
import br.ifsp.film_catalog.exception.ErrorResponse;
import br.ifsp.film_catalog.model.User;
import br.ifsp.film_catalog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @BeforeEach
    void setUp() throws Exception { 
        UserRequestDTO userRequestDTO1 = new UserRequestDTO();
        userRequestDTO1.setName("Ana");
        userRequestDTO1.setEmail("ana@email.com");
        userRequestDTO1.setPassword("senhaSegura123@");
        userRequestDTO1.setUsername("ana");
        userService.createUser(userRequestDTO1);
        UserRequestDTO userRequestDTO2 = new UserRequestDTO();
        userRequestDTO2.setName("Beatriz");
        userRequestDTO2.setEmail("bea@email.com");
        userRequestDTO2.setUsername("bea");
        userRequestDTO2.setPassword("senhaSegura123@");
        userService.createUser(userRequestDTO2);

    }

    @Test
    void givenValidUserRequest_whenRegisterUser_thenReturnsCreatedUserResponse() {
        // Given
        UserRequestDTO requestDTO = new UserRequestDTO();
        requestDTO.setName("Jéssica");
        requestDTO.setEmail("jessica@example.com");
        requestDTO.setPassword("senhaSegura123@");
        requestDTO.setUsername("jessica");

        UserResponseDTO responseDTO = new UserResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setName("Jéssica");
        responseDTO.setEmail("jessica@example.com");
        responseDTO.setUsername("jessica");

        when(userService.createUser(requestDTO)).thenReturn(responseDTO);

        // When
        ResponseEntity<UserResponseDTO> response = userController.registerUser(requestDTO);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Jéssica", response.getBody().getName());
        assertEquals("jessica@example.com", response.getBody().getEmail());
        assertEquals("jessica", response.getBody().getUsername());
    }

    @Test
    @DisplayName("Retorna todos os usuários cadastrados")
    void getAllUsers_ShouldReturnAllUsers() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        UserResponseDTO user1 = new UserResponseDTO();
        user1.setId(1L);
        user1.setName("Ana");
        user1.setEmail("ana@email.com");
        user1.setUsername("ana");
        UserResponseDTO user2 = new UserResponseDTO();
        user2.setId(2L);
        user2.setName("Beatriz");
        user2.setEmail("bea@email.com");
        user2.setUsername("bea");

        PagedResponse<UserResponseDTO> pagedResponse = new PagedResponse<>(
                List.of(user1, user2), // content
                0, // page
                10, // size
                2, // totalElements
                1, // totalPages
                true // last
        );

        when(userService.getAllUsers(pageable)).thenReturn(pagedResponse);

        // When
        ResponseEntity<PagedResponse<UserResponseDTO>> response = userController.getAllUsers(pageable);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        assertEquals("Ana", response.getBody().getContent().get(0).getName());
        assertEquals("Beatriz", response.getBody().getContent().get(1).getName());
    }

    @Test
    @DisplayName("Retorna usuário por ID")
    void getUserById_ShouldReturnUser() {
        // Given
        Long userId = 1L;
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(userId);
        userResponseDTO.setName("Ana");
        userResponseDTO.setEmail("ana@email.com");
        userResponseDTO.setUsername("ana");

        // When
        when(userService.getUserById(userId)).thenReturn(userResponseDTO);
        ResponseEntity<UserResponseDTO> response = userController.getUserById(userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Ana", response.getBody().getName());
        assertEquals("ana@email.com", response.getBody().getEmail());
        assertEquals("ana", response.getBody().getUsername());
    }

    @Test
    @DisplayName("Retorna um usuário por nome")
    void getUserByName_ShouldReturnUser() {
        // Given
        String username = "ana";
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(1L);
        userResponseDTO.setName("Ana");
        userResponseDTO.setEmail("ana@email.com");
        userResponseDTO.setUsername("ana");
        
        //When
        when(userService.getUserByUsername(username)).thenReturn(userResponseDTO);
        ResponseEntity<UserResponseDTO> response = userController.getUserByUsername(username);
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Ana", response.getBody().getName());
        assertEquals("ana@email.com", response.getBody().getEmail());
        assertEquals("ana", response.getBody().getUsername());
    }

    @Test
    @DisplayName("Retorna um novo usuário cadastrado")
    void registerUser_ShouldReturnCreatedUser() {
        // Given
        UserRequestDTO userRequestDTO = new UserRequestDTO();
        userRequestDTO.setName("Carlos");
        userRequestDTO.setEmail("carlso@email.com");
        userRequestDTO.setUsername("carlos");
        userRequestDTO.setPassword("senhaSegura123@");
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(3L);
        userResponseDTO.setName("Carlos");
        userResponseDTO.setEmail("carlos@email.com");
        userResponseDTO.setUsername("carlos");

        //When
        when(userService.createUser(userRequestDTO)).thenReturn(userResponseDTO);
        ResponseEntity<UserResponseDTO> response = userController.registerUser(userRequestDTO);
        
        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Carlos", response.getBody().getName());
        assertEquals("carlos@email.com", response.getBody().getEmail());
        assertEquals("carlos", response.getBody().getUsername());
    }

    @Test
    @DisplayName("Atualiza um usuário existente")
    void updateUser_ShouldReturnUpdatedUser() {
        // Given
        Long userId = 1L;
        UserPatchDTO userPathDTO = new UserPatchDTO();
        userPathDTO.setName(Optional.of("Ana Maria"));
        userPathDTO.setEmail(Optional.of("ana@email.com"));
        userPathDTO.setUsername(Optional.of("ana_maria"));
        userPathDTO.setPassword(Optional.of("novaSenhaSegura123@"));

        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(userId);
        userResponseDTO.setName("Ana Maria");
        userResponseDTO.setEmail("ana@email.com");
        userResponseDTO.setUsername("ana_maria");

        // When
        when(userService.patchUser(userId, userPathDTO)).thenReturn(userResponseDTO);
        ResponseEntity<UserResponseDTO> response = userController.patchUser(userId, userPathDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Ana Maria", response.getBody().getName());
        assertEquals("ana@email.com", response.getBody().getEmail());
        assertEquals("ana_maria", response.getBody().getUsername());
    }

    @Test
    @DisplayName("Deleta um usuário existente")
    void deleteUser_ShouldReturnNoContent() {
        // Given
        Long userId = 1L;

        // When
        doNothing().when(userService).deleteUser(userId);
        ResponseEntity<Void> response = userController.deleteUser(userId);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}