package ar.edu.tup.programacion3.entities;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class Base {
    @Id
    @EqualsAndHashCode.Include
    private Long id;
    private Boolean eliminado;
    private LocalDateTime createdAt;

}
