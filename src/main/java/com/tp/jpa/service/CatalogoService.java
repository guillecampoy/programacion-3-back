package com.tp.jpa.service;

import com.tp.jpa.entities.Categoria;
import com.tp.jpa.entities.Producto;
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
        String nombreNormalizado = requerirTexto(nombre, "El nombre de la categoria");
        String descripcionNormalizada = requerirTexto(descripcion, "La descripcion de la categoria");

        Categoria categoria = new Categoria();
        categoria.setNombre(nombreNormalizado);
        categoria.setDescripcion(descripcionNormalizada);
        categoria.setEliminado(false);
        categoria.setCreatedAt(LocalDateTime.now());
        return categoriaRepository.guardar(categoria);
    }

    public Categoria modificarCategoria(Long id, String nombre, String descripcion) {
        validarId(id, "categoria");
        Categoria categoria = obtenerCategoriaActiva(id);
        if (nombre != null && !nombre.isBlank()) {
            categoria.setNombre(requerirTexto(nombre, "El nombre de la categoria"));
        }
        if (descripcion != null && !descripcion.isBlank()) {
            categoria.setDescripcion(requerirTexto(descripcion, "La descripcion de la categoria"));
        }
        return categoriaRepository.guardar(categoria);
    }

    public Categoria bajaCategoria(Long id) {
        validarId(id, "categoria");
        Categoria categoria = obtenerCategoria(id);
        if (Boolean.TRUE.equals(categoria.getEliminado())) {
            throw new IllegalStateException("Error: la categoria ya se encuentra dada de baja.");
        }
        categoriaRepository.eliminarLogico(id);
        categoria.setEliminado(true);
        return categoria;
    }

    public Categoria obtenerCategoriaActiva(Long id) {
        validarId(id, "categoria");
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
        validarId(categoriaId, "categoria");
        String nombreNormalizado = requerirTexto(nombre, "El nombre del producto");
        String descripcionNormalizada = requerirTexto(descripcion, "La descripcion del producto");
        validarPrecio(precio);
        validarStock(stock);

        Categoria categoria = obtenerCategoriaActiva(categoriaId);
        Producto producto = new Producto();
        producto.setNombre(nombreNormalizado);
        producto.setDescripcion(descripcionNormalizada);
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
        validarId(id, "producto");
        if (nombre != null && !nombre.isBlank()) {
            requerirTexto(nombre, "El nombre del producto");
        }
        if (precio != null) {
            validarPrecio(precio);
        }
        if (stock != null) {
            validarStock(stock);
        }

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
        validarId(id, "producto");
        Producto producto = obtenerProducto(id);
        if (Boolean.TRUE.equals(producto.getEliminado())) {
            throw new IllegalStateException("Error: el producto ya se encuentra dado de baja.");
        }
        productoRepository.eliminarLogico(id);
        producto.setEliminado(true);
        return producto;
    }

    public Producto obtenerProductoActivo(Long id) {
        validarId(id, "producto");
        Producto producto = obtenerProducto(id);
        if (Boolean.TRUE.equals(producto.getEliminado())) {
            throw new IllegalArgumentException("Error: no existe un producto activo con el ID indicado.");
        }
        return producto;
    }

    public List<Producto> buscarProductosActivosPorCategoria(Long categoriaId) {
        validarId(categoriaId, "categoria");
        obtenerCategoriaActiva(categoriaId);
        return productoRepository.buscarPorCategoria(categoriaId);
    }

    private Categoria obtenerCategoria(Long id) {
        validarId(id, "categoria");
        return categoriaRepository.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Error: no existe una categoria activa con el ID indicado."
                ));
    }

    private Producto obtenerProducto(Long id) {
        validarId(id, "producto");
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

    private String requerirTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Error: " + campo + " es obligatorio.");
        }
        return valor.trim();
    }

    private void validarPrecio(Double precio) {
        if (precio == null || precio <= 0) {
            throw new IllegalArgumentException("Error: el precio debe ser mayor a 0.");
        }
    }

    private void validarStock(Integer stock) {
        if (stock == null || stock < 0) {
            throw new IllegalArgumentException("Error: el stock debe ser mayor o igual a 0.");
        }
    }

    private void validarId(Long id, String entidad) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Error: el ID de " + entidad + " debe ser mayor a 0.");
        }
    }
}
