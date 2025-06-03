package br.ifsp.film_catalog.unitTests;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import br.ifsp.film_catalog.controller.UserController;
import br.ifsp.film_catalog.dto.UserRequestDTO;
import br.ifsp.film_catalog.dto.UserResponseDTO;
import br.ifsp.film_catalog.service.UserService;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

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
}