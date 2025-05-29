package br.ifsp.film_catalog.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequestDTO {

    @Pattern(regexp = "ROLE_ADMIN|ROLE_USER", flags = Pattern.Flag.CASE_INSENSITIVE, message = "Invalid content rating value. Must be one of: ROLE_USER, ROLE_ADMIN.")
    private String roleName;
}
