package br.ifsp.film_catalog.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

import br.ifsp.film_catalog.model.common.BaseEntity;
import br.ifsp.film_catalog.model.enums.RoleName;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ROLES")
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 20, name = "role_name")
    private RoleName roleName;

    @ManyToMany(mappedBy = "roles", fetch = FetchType.EAGER)
    private Set<User> users;
}


