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

    @Size(max = 255, message = "Name cannot exceed 255 characters if provided.")
    private Optional<String> name = Optional.empty();

    @Email(message = "Email should be a valid email format if provided.")
    @Size(max = 255, message = "Email cannot exceed 255 characters if provided.")
    private Optional<String> email = Optional.empty();

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters if provided.")
    private Optional<String> username = Optional.empty();

    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters if provided.")
    // Consider adding @Pattern for password complexity if needed
    private Optional<String> password = Optional.empty(); // For password changes

    // To update the set of roles.
    // If Optional.empty(), roles are not touched.
    // If Optional.of(Collections.emptySet()), all roles are removed.
    // If Optional.of(newRoleIdsSet), roles are replaced with the new set.
    private Optional<Set<Long>> roleIds = Optional.empty();
}
