package com.tp.jpa.repository;

import com.tp.jpa.model.Categoria;

public class CategoriaRepository extends BaseRepository<Categoria> {
  /** Crea el repositorio de categorias. */
  public CategoriaRepository() {
    super(Categoria.class);
  }
}
