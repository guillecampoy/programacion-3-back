# HU-01 - BaseRepository con CRUD genérico

## Resumen

**Épica:** Repositorios  
**Prioridad:** Alta  
**Story Points:** 18  
**Proyecto:** backend Java/JPA/Hibernate/H2

## Historia de usuario

**Como** desarrollador  
**Quiero** contar con un BaseRepository<T> que implemente las operaciones CRUD comunes  
**Para** no repetir código de persistencia en cada repositorio específico

## Alcance funcional

- `repository/BaseRepository.java`.
- CRUD común.
- Gestión de EntityManager y transacciones.
- Uso de `Optional<T>`.
- Soft delete.

## Criterios de aceptación

- `guardar()` abre su propia transacción; usa `persist()` si el id es null o `merge()` si ya tiene id, hace commit y rollback ante excepción. Retorna la entidad gestionada, de la cual debe leerse el ID generado.
- `buscarPorId()` retorna `Optional<T>`: `Optional.of(entidad)` si existe, `Optional.empty()` si no.
- `listarActivos()` usa JPQL con `WHERE e.eliminado = false` y retorna `List<T>`.
- `eliminarLogico()` busca por ID, establece `eliminado = true`, persiste y retorna `true`; retorna `false` si no encuentra el registro.
- Cada método cierra el `EntityManager` en un bloque `finally`.

## Guía de implementación sugerida

- Guardar `Class<T> entityClass` recibido por constructor.
- Obtener `EntityManagerFactory` desde `JPAUtil`.
- Construir JPQL de `listarActivos()` con `entityClass.getSimpleName()`.
- En `eliminarLogico`, usar transacción solo si el registro existe.
- No tragar excepciones silenciosamente; rollback si la transacción está activa.

## Validación esperada

- Crear una categoría con repo y confirmar ID generado.
- Buscar id existente y no existente.
- Listar activos excluye dados de baja.
- Eliminar lógicamente y verificar que no aparece activo.
- Forzar error controlado y revisar rollback/cierre.

## Definition of Done

- La historia compila y ejecuta localmente.
- Se cumplen todos los criterios de aceptación.
- No se introducen cambios fuera del alcance sin justificación explícita.
- Los nombres de rutas, paquetes y archivos respetan la estructura del proyecto existente.
- Queda registro en README o comentario técnico cuando la consigna lo pide.
- La revisión humana inspeccionó el diff y ejecutó la validación mínima.

## Prompt sugerido para la sesión agentica

```text
Actuá como par de desarrollo. Revisá el proyecto actual y aplicá únicamente la historia HU-01 - BaseRepository con CRUD genérico.

Contexto: proyecto backend Java/JPA/Hibernate/H2. Ya existe una base funcional que debe extenderse, no reescribirse.

Objetivo:
Como desarrollador, quiero contar con un BaseRepository<T> que implemente las operaciones CRUD comunes, para no repetir código de persistencia en cada repositorio específico.

Criterios de aceptación obligatorios:
1. `guardar()` abre su propia transacción; usa `persist()` si el id es null o `merge()` si ya tiene id, hace commit y rollback ante excepción. Retorna la entidad gestionada, de la cual debe leerse el ID generado.
2. `buscarPorId()` retorna `Optional<T>`: `Optional.of(entidad)` si existe, `Optional.empty()` si no.
3. `listarActivos()` usa JPQL con `WHERE e.eliminado = false` y retorna `List<T>`.
4. `eliminarLogico()` busca por ID, establece `eliminado = true`, persiste y retorna `true`; retorna `false` si no encuentra el registro.
5. Cada método cierra el `EntityManager` en un bloque `finally`.

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
