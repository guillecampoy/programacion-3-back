# HU-04 - PedidoRepository con consultas JPQL

## Resumen

**Épica:** Repositorios  
**Prioridad:** Alta  
**Story Points:** 10  
**Proyecto:** backend Java/JPA/Hibernate/H2

## Historia de usuario

**Como** desarrollador  
**Quiero** contar con PedidoRepository con buscarPorUsuario() y buscarPorEstado()  
**Para** obtener pedidos filtrados sin escribir JPQL fuera del repositorio

## Alcance funcional

- `PedidoRepository`.
- `buscarPorUsuario(Long idUsuario)`.
- `buscarPorEstado(Estado estado)`.
- JPQL tipado con parámetros nombrados.

## Criterios de aceptación

- `PedidoRepository` extiende `BaseRepository<Pedido>` y llama a `super(Pedido.class)`.
- `buscarPorUsuario()` filtra por `usuario.id = :uid` y `eliminado = false`; retorna `List<Pedido>`.
- `buscarPorEstado()` filtra por `estado = :estado` y `eliminado = false`; retorna `List<Pedido>`.
- Ambos usan `List<Pedido>`.
- Cada método cierra el `EntityManager` en un bloque `finally`.
- Cada método incluye un comentario explicando la consulta JPQL.

## Guía de implementación sugerida

- Importar `Estado` correctamente.
- Usar `em.createQuery(jpql, Pedido.class)`.
- No repetir JPQL en `Main`.
- No filtrar en memoria si el criterio debe estar en JPQL.
- Mantener nombres de parámetros `uid` y `estado` claros.

## Validación esperada

- Pedido por usuario devuelve solo de ese usuario.
- Pedido eliminado no aparece.
- Filtro por estado devuelve solo estado elegido.
- Estado sin pedidos devuelve lista vacía.
- No hay casteos manuales.

## Definition of Done

- La historia compila y ejecuta localmente.
- Se cumplen todos los criterios de aceptación.
- No se introducen cambios fuera del alcance sin justificación explícita.
- Los nombres de rutas, paquetes y archivos respetan la estructura del proyecto existente.
- Queda registro en README o comentario técnico cuando la consigna lo pide.
- La revisión humana inspeccionó el diff y ejecutó la validación mínima.

## Prompt sugerido para la sesión agentica

```text
Actuá como par de desarrollo. Revisá el proyecto actual y aplicá únicamente la historia HU-04 - PedidoRepository con consultas JPQL.

Contexto: proyecto backend Java/JPA/Hibernate/H2. Ya existe una base funcional que debe extenderse, no reescribirse.

Objetivo:
Como desarrollador, quiero contar con PedidoRepository con buscarPorUsuario() y buscarPorEstado(), para obtener pedidos filtrados sin escribir JPQL fuera del repositorio.

Criterios de aceptación obligatorios:
1. `PedidoRepository` extiende `BaseRepository<Pedido>` y llama a `super(Pedido.class)`.
2. `buscarPorUsuario()` filtra por `usuario.id = :uid` y `eliminado = false`; retorna `List<Pedido>`.
3. `buscarPorEstado()` filtra por `estado = :estado` y `eliminado = false`; retorna `List<Pedido>`.
4. Ambos usan `List<Pedido>`.
5. Cada método cierra el `EntityManager` en un bloque `finally`.
6. Cada método incluye un comentario explicando la consulta JPQL.

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
