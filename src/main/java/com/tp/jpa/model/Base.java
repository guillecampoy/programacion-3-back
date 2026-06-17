package com.tp.jpa.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Objects;

@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@ToString
public abstract class Base {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Boolean eliminado;
    private LocalDateTime createdAt;

    public void setId(Long id) {
        if (id != null && id <= 0) {
            throw new IllegalArgumentException("El id debe ser mayor a 0.");
        }
        this.id = id;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        Base base = (Base) object;
        return id != null && Objects.equals(id, base.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    protected static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " no puede estar vacio.");
        }
        return value.trim();
    }

    protected static <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " no puede ser nulo.");
        }
        return value;
    }

    protected static Double requirePositive(Double value, String fieldName) {
        requireNonNull(value, fieldName);
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " debe ser mayor a 0.");
        }
        return value;
    }

    protected static int requireMin(int value, int min, String fieldName) {
        if (value < min) {
            throw new IllegalArgumentException(fieldName + " debe ser mayor o igual a " + min + ".");
        }
        return value;
    }
}
