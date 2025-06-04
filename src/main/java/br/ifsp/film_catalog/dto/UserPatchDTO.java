package br.ifsp.film_catalog.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Optional;
import java.util.Set;

/**
 * DTO for partially updating a User.
 * All fields are optional. Validation annotations primarily serve as documentation
 * for the expected format if the Optional is present.
 * Actual validation of the Optional's content is typically done in the service layer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPatchDTO {

    @Size(max = 255, message = "Name cannot exceed 255 characters.")
    private String name;

    @Email(message = "Email should be a valid email format.")
    @Size(max = 255, message = "Email cannot exceed 255 characters.")
    private String email;

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters.")
    private String username;

    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters.")
    private String password;

    private Set<Long> roleIds;
}
