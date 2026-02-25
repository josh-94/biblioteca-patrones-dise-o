# Sistema de Biblioteca Universitaria

**Integrantes:** Jeshua Cabanillas · José Palma · Hector Calla


Aplicación de consola en Java que simula la gestión de préstamos de una biblioteca universitaria, implementando tres patrones de diseño clásicos: **Abstract Factory**, **Factory Method** y **Builder**.

---

## Patrones de diseño implementados

| Patrón | Dónde se aplica |
|---|---|
| **Abstract Factory** | `BibliotecaRepositoryFactory` define una familia de repositorios; `PostgresBibliotecaFactory` e `InMemoryBibliotecaFactory` son las fábricas concretas. Cambiar de implementación (base de datos real ↔ memoria) requiere modificar una sola línea. |
| **Factory Method** | Cada método `crearLibroRepository()`, `crearUsuarioRepository()`, etc. dentro de las fábricas concretas es un Factory Method que instancia el repositorio correspondiente. |
| **Builder** | `Libro.Builder` y `Usuario.Builder` permiten construir objetos con campos obligatorios y opcionales de forma legible, evitando constructores con demasiados parámetros. `ConsultaPrestamoBuilder` construye filtros de búsqueda SQL dinámicos de forma encadenada. |

---

## Estructura del proyecto

```
├── src/
│   ├── Libro.java                       # Modelo + Builder
│   ├── Usuario.java                     # Modelo + Builder
│   ├── Prestamo.java                    # Record
│   ├── Multa.java                       # Record
│   ├── ConsultaPrestamoBuilder.java     # Builder de consultas dinámicas
│   ├── LibroRepository.java             # Interfaz
│   ├── UsuarioRepository.java           # Interfaz
│   ├── PrestamoRepository.java          # Interfaz
│   ├── MultaRepository.java             # Interfaz
│   ├── BibliotecaRepositoryFactory.java # Abstract Factory
│   ├── PostgresLibroRepository.java     # Implementación PostgreSQL
│   ├── PostgresUsuarioRepository.java   # Implementación PostgreSQL
│   ├── PostgresPrestamoRepository.java  # Implementación PostgreSQL
│   ├── PostgresMultaRepository.java     # Implementación PostgreSQL
│   ├── PostgresBibliotecaFactory.java   # Fábrica concreta PostgreSQL
│   ├── InMemoryBibliotecaFactory.java   # Fábrica concreta en memoria (pruebas)
│   ├── BibliotecaService.java           # Capa de servicio
│   └── BibliotecaApp.java               # Main + configuración de conexión
├── files/
│   ├── biblioteca_schema.sql            # Schema SQL de referencia
│   └── biblioteca_erd.mermaid           # Diagrama entidad-relación
├── db.properties.example                # Plantilla de configuración de BD
├── run.bat                              # Script para compilar y ejecutar
└── postgresql-42.7.3.jar                # Driver JDBC PostgreSQL
```

---

## Requisitos

- Java 17 o superior
- PostgreSQL 14 o superior (con una base de datos llamada `biblioteca`)
- Driver JDBC: `postgresql-42.7.3.jar` (incluido en el repo)

---

## Configuración

1. Copia el archivo de ejemplo y edítalo con tus credenciales:
   ```
   copy db.properties.example db.properties
   ```
   Contenido de `db.properties`:
   ```properties
   db.url=jdbc:postgresql://localhost:5432/biblioteca
   db.user=postgres
   db.password=TU_CLAVE_AQUI
   ```

2. La aplicación crea las tablas automáticamente al iniciar si no existen.

---

## Compilar y ejecutar

**Windows (usando el script):**
```
run.bat
```

**Manual:**
```powershell
javac -encoding UTF-8 -cp postgresql-42.7.3.jar src\*.java -d out
java -cp "out;postgresql-42.7.3.jar" BibliotecaApp
```

---

## Funcionalidad demostrada

Al ejecutar la app se muestra paso a paso:

1. **Builder** — Creación de libros y usuarios con distintos niveles de detalle (campos obligatorios y opcionales).
2. **Factory Method** — Registro de un préstamo usando los repositorios creados por la fábrica.
3. **Builder de consultas** — Búsqueda de préstamos con filtros dinámicos encadenados (por usuario, estado, fechas, vencidos).
4. Registro de devolución y cálculo automático de multa.


