# Backend - Backlog operativo TPI Programación 3

Este directorio contiene las historias de usuario para completar el proyecto backend Food Store con Java + Gradle + JPA/Hibernate + H2 en archivo. Está pensado para ejecutar sesiones separadas, historia por historia, con agente de desarrollo y revisión humana.

## Orden recomendado

1. `HU-01` a `HU-04`: repositorios y JPQL base.
2. `HU-05` a `HU-07`: ABM de categorías.
3. `HU-08` a `HU-11`: ABM de productos y consulta por categoría.
4. `HU-12` a `HU-15`: ABM de usuarios y búsqueda por mail.
5. `HU-16` a `HU-18`: pedidos, transacción atómica, estado y baja lógica.
6. `HU-19` a `HU-21`: reportes.

## Antes de empezar

Leer primero `00_contexto_restricciones.md`. Ahí están las reglas críticas de JPA, transacciones, soft delete, relaciones, estructura de paquetes y menú de consola.

## Validación de entrega backend

```bash
./gradlew clean build
./gradlew run
```

Debe compilar y ejecutar sin errores desde línea de comandos.

## Flujo de trabajo recomendado con agente + revisión humana

1. Crear una rama corta por historia.
2. Pegar al agente el bloque **Prompt sugerido para la sesión** de esta historia.
3. Pedir implementación incremental, no reescritura masiva.
4. Ejecutar validaciones locales.
5. Revisar diff humano antes de aceptar la historia.
6. Registrar en el commit: historia, alcance, validación ejecutada y deuda pendiente.

## Reglas de revisión humana

- No aceptar cambios fuera del alcance de la historia sin justificación.
- No aceptar código muerto, duplicación obvia ni helpers genéricos innecesarios.
- No aceptar que se cambien nombres de paquetes/rutas base salvo que ya estén así en el proyecto.
- Verificar que los criterios de aceptación queden demostrables desde UI o consola.
