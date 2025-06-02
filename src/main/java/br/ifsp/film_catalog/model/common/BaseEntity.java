package br.ifsp.film_catalog.model.common; // A common package is a good place for this

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * An abstract base class for all JPA entities.
 * It provides the ID field and a standard implementation for equals() and hashCode()
 * to avoid boilerplate code in every entity.
 */
@MappedSuperclass // Declares this as a superclass for entities, not an entity itself.
public abstract class BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Getter
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    /**
     * The equals method checks for equality based on the entity's ID.
     * Two entities are considered equal if they are of the same class and have the same ID.
     * @param o The object to compare with.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // Check that the object is not null and is an instance of BaseEntity.
        // Using 'instanceof' is often safer with JPA proxies than using 'getClass()'.
        if (!(o instanceof BaseEntity that)) return false;
        // If id is null for either object, they cannot be equal.
        // The business key should be used for equality if no ID is assigned yet.
        return id != null && id.equals(that.id);
    }

    /**
     * The hashCode method.
     * It's a best practice to return a consistent hash code. For an entity,
     * this implementation prevents the hash code from changing when the ID is generated
     * by the database, which is important when working with collections like Sets.
     * @return The hash code of the object.
     */
    @Override
    public int hashCode() {
        // Using a prime number helps in distributing hash codes more evenly.
        // It ensures the hash code is consistent whether the entity is new (id=null) or persisted.
        return 31;
    }
}