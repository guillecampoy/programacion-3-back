# HU-02 - CategoriaRepository y ProductoRepository

## Resumen

**Épica:** Repositorios  
**Prioridad:** Alta  
**Story Points:** 12  
**Proyecto:** backend Java/JPA/Hibernate/H2

## Historia de usuario

**Como** desarrollador  
**Quiero** contar con CategoriaRepository y ProductoRepository que extiendan BaseRepository  
**Para** operar sobre cada entidad sin reescribir el CRUD base

## Alcance funcional

- `CategoriaRepository`.
- `ProductoRepository`.
- Consulta JPQL `buscarPorCategoria`.

## Criterios de aceptación

- `CategoriaRepository` extiende `BaseRepository<Categoria>` y llama a `super(Categoria.class)`.
- `ProductoRepository` extiende `BaseRepository<Producto>` y llama a `super(Producto.class)`.
- `ProductoRepository` incluye `buscarPorCategoria(Long categoriaId)` con JPQL tipado.
- La consulta JPQL filtra por `categoria.id = :catId` y `eliminado = false`.
- El método retorna `List<Producto>`.
- El método incluye un comentario explicando la consulta JPQL.

## Guía de implementación sugerida

- No agregar métodos extra innecesarios a `CategoriaRepository`.
- Usar `em.createQuery(jpql, Producto.class)`.
- Usar parámetro nombrado `:catId`.
- Cerrar EntityManager en `finally`.
- No hacer casteos manuales.

## Validación esperada

- Compila con genéricos sin warnings relevantes.
- Buscar por categoría con productos devuelve lista.
- Buscar categoría sin productos devuelve lista vacía.
- Producto eliminado no aparece.
- Comentario JPQL presente.

## Definition of Done

- La historia compila y ejecuta localmente.
- Se cumplen todos los criterios de aceptación.
- No se introducen cambios fuera del alcance sin justificación explícita.
- Los nombres de rutas, paquetes y archivos respetan la estructura del proyecto existente.
- Queda registro en README o comentario técnico cuando la consigna lo pide.
- La revisión humana inspeccionó el diff y ejecutó la validación mínima.

## Prompt sugerido para la sesión agentica

```text
Actuá como par de desarrollo. Revisá el proyecto actual y aplicá únicamente la historia HU-02 - CategoriaRepository y ProductoRepository.

Contexto: proyecto backend Java/JPA/Hibernate/H2. Ya existe una base funcional que debe extenderse, no reescribirse.

Objetivo:
Como desarrollador, quiero contar con CategoriaRepository y ProductoRepository que extiendan BaseRepository, para operar sobre cada entidad sin reescribir el CRUD base.

Criterios de aceptación obligatorios:
1. `CategoriaRepository` extiende `BaseRepository<Categoria>` y llama a `super(Categoria.class)`.
2. `ProductoRepository` extiende `BaseRepository<Producto>` y llama a `super(Producto.class)`.
3. `ProductoRepository` incluye `buscarPorCategoria(Long categoriaId)` con JPQL tipado.
4. La consulta JPQL filtra por `categoria.id = :catId` y `eliminado = false`.
5. El método retorna `List<Producto>`.
6. El método incluye un comentario explicando la consulta JPQL.

Restricciones:
- Mantener el alcance acotado a esta historia.
- No cambiar estructura general salvo que sea imprescindible y justificado.
- No introducir frameworks no pedidos.
- Devolver resumen del diff, archivos tocados, validaciones ejecutadas y riesgos pendientes.
- No hacer commit automáticamente; dejar el cambio listo para revisión humana.
```

## Checklist de revisión humana

- [ ] Leí el diff completo.
- [ ] Ejecuté la aplicación o build correspondiente.
- [ ] Probé el caso feliz.
- [ ] Probé al menos un caso de error/validación.
- [ ] Confirmé que no se rompió una historia anterior.
- [ ] Dejé nota de deuda técnica si aplica.
