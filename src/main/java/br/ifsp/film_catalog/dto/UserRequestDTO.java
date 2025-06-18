package br.ifsp.film_catalog.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO {

    @NotBlank(message = "Name cannot be blank.")
    @Size(max = 255, message = "Name cannot exceed 255 characters.")
    private String name;

    @NotBlank(message = "Email cannot be blank.")
    @Email(message = "Email should be a valid email format.")
    @Size(max = 255, message = "Email cannot exceed 255 characters.")
    private String email;

    @NotBlank(message = "Username cannot be blank.")
    @NotEmpty(message = "Username cannot be empty.")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters.")
    private String username;

    @NotBlank(message = "Password cannot be blank.")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters.")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
    message = "Password must contain at least one digit, one lowercase letter, one uppercase letter, one special character, and no whitespace.")
    private String password;
}
