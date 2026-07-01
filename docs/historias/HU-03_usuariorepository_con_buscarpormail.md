# HU-03 - UsuarioRepository con buscarPorMail

## Resumen

**Épica:** Repositorios  
**Prioridad:** Alta  
**Story Points:** 8  
**Proyecto:** backend Java/JPA/Hibernate/H2

## Historia de usuario

**Como** desarrollador  
**Quiero** contar con UsuarioRepository que extienda BaseRepository<Usuario> y provea buscarPorMail()  
**Para** validar unicidad de mail y buscar usuarios sin reescribir el CRUD base

## Alcance funcional

- `UsuarioRepository`.
- Consulta JPQL tipada.
- Retorno `Optional<Usuario>`.

## Criterios de aceptación

- `UsuarioRepository` extiende `BaseRepository<Usuario>` y llama a `super(Usuario.class)`.
- `buscarPorMail()` usa JPQL con parámetro nombrado `:mail` y `WHERE e.eliminado = false`.
- Retorna `Optional<Usuario>`: `Optional.of(usuario)` si existe, `Optional.empty()` si no.
- La consulta se ejecuta con `TypedQuery<Usuario>` y devuelve el resultado mediante `getResultList()`.
- El método cierra el `EntityManager` en un bloque `finally`.
- Incluye un comentario explicando la consulta JPQL.

## Guía de implementación sugerida

- Normalizar mail con `trim()` desde el flujo de consola, no necesariamente en repo.
- No exponer contraseña en salida del menú.
- No usar `getSingleResult()` para evitar excepción por no encontrado.
- Retornar primer resultado si existe.
- Filtrar siempre eliminado=false.

## Validación esperada

- Buscar mail existente activo devuelve Optional presente.
- Buscar mail inexistente devuelve Optional.empty.
- Usuario eliminado no se devuelve.
- No se lanza excepción por resultado vacío.
- El método compila con `TypedQuery<Usuario>`.

## Definition of Done

- La historia compila y ejecuta localmente.
- Se cumplen todos los criterios de aceptación.
- No se introducen cambios fuera del alcance sin justificación explícita.
- Los nombres de rutas, paquetes y archivos respetan la estructura del proyecto existente.
- Queda registro en README o comentario técnico cuando la consigna lo pide.
- La revisión humana inspeccionó el diff y ejecutó la validación mínima.

## Prompt sugerido para la sesión agentica

```text
Actuá como par de desarrollo. Revisá el proyecto actual y aplicá únicamente la historia HU-03 - UsuarioRepository con buscarPorMail.

Contexto: proyecto backend Java/JPA/Hibernate/H2. Ya existe una base funcional que debe extenderse, no reescribirse.

Objetivo:
Como desarrollador, quiero contar con UsuarioRepository que extienda BaseRepository<Usuario> y provea buscarPorMail(), para validar unicidad de mail y buscar usuarios sin reescribir el CRUD base.

Criterios de aceptación obligatorios:
1. `UsuarioRepository` extiende `BaseRepository<Usuario>` y llama a `super(Usuario.class)`.
2. `buscarPorMail()` usa JPQL con parámetro nombrado `:mail` y `WHERE e.eliminado = false`.
3. Retorna `Optional<Usuario>`: `Optional.of(usuario)` si existe, `Optional.empty()` si no.
4. La consulta se ejecuta con `TypedQuery<Usuario>` y devuelve el resultado mediante `getResultList()`.
5. El método cierra el `EntityManager` en un bloque `finally`.
6. Incluye un comentario explicando la consulta JPQL.

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
