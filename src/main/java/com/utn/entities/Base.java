package com.utn.entities;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@ToString
public abstract class Base {
    private Long id;
    private Boolean eliminado;
    private LocalDateTime createdAt;

}
