package com.tp.jpa.service;

import ar.edu.tup.programacion3.entities.Categoria;
import ar.edu.tup.programacion3.entities.Producto;
import com.tp.jpa.repository.CategoriaRepository;
import com.tp.jpa.repository.ProductoRepository;

import java.time.LocalDateTime;
import java.util.List;

public class CatalogoService {
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CatalogoService(CategoriaRepository categoriaRepository, ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    public List<Categoria> listarCategoriasActivas() {
        return categoriaRepository.listarActivos();
    }

    public List<Producto> listarProductosActivos() {
        return productoRepository.listarActivos();
    }

    public Categoria crearCategoria(String nombre, String descripcion) {
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre.trim());
        categoria.setDescripcion(descripcion.trim());
        categoria.setEliminado(false);
        categoria.setCreatedAt(LocalDateTime.now());
        return categoriaRepository.guardar(categoria);
    }

    public Categoria modificarCategoria(Long id, String nombre, String descripcion) {
        Categoria categoria = obtenerCategoriaActiva(id);
        if (nombre != null && !nombre.isBlank()) {
            categoria.setNombre(nombre.trim());
        }
        if (descripcion != null && !descripcion.isBlank()) {
            categoria.setDescripcion(descripcion.trim());
        }
        return categoriaRepository.guardar(categoria);
    }

    public Categoria bajaCategoria(Long id) {
        Categoria categoria = obtenerCategoria(id);
        if (Boolean.TRUE.equals(categoria.getEliminado())) {
            throw new IllegalStateException("Error: la categoria ya se encuentra dada de baja.");
        }
        categoriaRepository.eliminarLogico(id);
        categoria.setEliminado(true);
        return categoria;
    }

    public Categoria obtenerCategoriaActiva(Long id) {
        Categoria categoria = obtenerCategoria(id);
        if (Boolean.TRUE.equals(categoria.getEliminado())) {
            throw new IllegalArgumentException("Error: no existe una categoria activa con el ID indicado.");
        }
        return categoria;
    }

    public Producto crearProducto(
            Long categoriaId,
            String nombre,
            String descripcion,
            double precio,
            int stock
    ) {
        Categoria categoria = obtenerCategoriaActiva(categoriaId);
        Producto producto = new Producto();
        producto.setNombre(nombre.trim());
        producto.setDescripcion(descripcion.trim());
        producto.setPrecio(precio);
        producto.setStock(stock);
        producto.setImagen("sin-imagen");
        producto.setDisponible(true);
        producto.setEliminado(false);
        producto.setCreatedAt(LocalDateTime.now());
        producto.setCategoria(referenciaCategoria(categoria));
        return productoRepository.guardar(producto);
    }

    public Producto modificarProducto(Long id, String nombre, Double precio, Integer stock) {
        Producto producto = obtenerProductoActivo(id);
        if (nombre != null && !nombre.isBlank()) {
            producto.setNombre(nombre.trim());
        }
        if (precio != null) {
            producto.setPrecio(precio);
        }
        if (stock != null) {
            producto.setStock(stock);
        }
        return productoRepository.guardar(producto);
    }

    public Producto bajaProducto(Long id) {
        Producto producto = obtenerProducto(id);
        if (Boolean.TRUE.equals(producto.getEliminado())) {
            throw new IllegalStateException("Error: el producto ya se encuentra dado de baja.");
        }
        productoRepository.eliminarLogico(id);
        producto.setEliminado(true);
        return producto;
    }

    public Producto obtenerProductoActivo(Long id) {
        Producto producto = obtenerProducto(id);
        if (Boolean.TRUE.equals(producto.getEliminado())) {
            throw new IllegalArgumentException("Error: no existe un producto activo con el ID indicado.");
        }
        return producto;
    }

    public List<Producto> buscarProductosActivosPorCategoria(Long categoriaId) {
        obtenerCategoriaActiva(categoriaId);
        return productoRepository.buscarPorCategoria(categoriaId);
    }

    private Categoria obtenerCategoria(Long id) {
        return categoriaRepository.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Error: no existe una categoria activa con el ID indicado."
                ));
    }

    private Producto obtenerProducto(Long id) {
        return productoRepository.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Error: no existe un producto activo con el ID indicado."
                ));
    }

    private Categoria referenciaCategoria(Categoria categoria) {
        Categoria referencia = new Categoria();
        referencia.setId(categoria.getId());
        referencia.setNombre(categoria.getNombre());
        referencia.setDescripcion(categoria.getDescripcion());
        referencia.setEliminado(categoria.getEliminado());
        referencia.setCreatedAt(categoria.getCreatedAt());
        return referencia;
    }
}
