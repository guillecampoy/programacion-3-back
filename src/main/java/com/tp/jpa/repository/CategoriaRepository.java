package com.tp.jpa.repository;

import com.tp.jpa.model.Categoria;

public class CategoriaRepository extends BaseRepository<Categoria> {
    public CategoriaRepository() {
        super(Categoria.class);
    }
}
