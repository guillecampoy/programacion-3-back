package com.tp.jpa.repository;

import ar.edu.tup.programacion3.entities.Categoria;

public class CategoriaRepository extends BaseRepository<Categoria> {
    public CategoriaRepository() {
        super(Categoria.class);
    }
}
