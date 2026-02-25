import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Properties;

// ================================================================
// SISTEMA DE BIBLIOTECA UNIVERSITARIA
// Patrones: Abstract Factory + Factory Method + Builder
// Java 17 | PostgreSQL via JDBC
// ================================================================
//
//  RESUMEN DE PATRONES
//  ┌─────────────────────┬─────────────────────────────────────────────────────┐
//  │ Patrón              │ Dónde se aplica                                     │
//  ├─────────────────────┼─────────────────────────────────────────────────────┤
//  │ Abstract Factory    │ BibliotecaRepositoryFactory → familias de repos     │
//  │ Factory Method      │ crearXxxRepository() dentro de cada factory concreta│
//  │ Builder             │ Libro.Builder, Usuario.Builder,                     │
//  │                     │ ConsultaPrestamoBuilder (criterios de búsqueda)      │
//  └─────────────────────┴─────────────────────────────────────────────────────┘

/**
 * Punto de entrada principal.
 * Demuestra los tres patrones de diseño juntos.
 */
public class BibliotecaApp {

    public static void main(String[] args) {

        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║   SISTEMA BIBLIOTECA — 3 PATRONES DE DISEÑO     ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");

        // ── ABSTRACT FACTORY ────────────────────────────────────────
        // Cambiar de implementación en una sola línea:
        //   Producción → crearFactoryPostgres()
        //   Pruebas    → new InMemoryBibliotecaFactory()
        BibliotecaRepositoryFactory factory = crearFactoryPostgres();
        BibliotecaService servicio = new BibliotecaService(factory);

        // ── BUILDER: Libro con todos los campos ─────────────────────
        System.out.println("─── PATRÓN BUILDER ────────────────────────────────\n");

        Libro libro1 = servicio.registrarLibro(
            new Libro.Builder("978-0-13-468599-1", "The Pragmatic Programmer")
                .editorial("Addison-Wesley")
                .anioPublicacion(2019)
                .edicion("20th Anniversary Edition")
                .ubicacion("Estante A-12")
                .descripcion("Guía esencial del desarrollo de software profesional")
        );
        System.out.println("✔ " + libro1);
        System.out.println("   Edición:   " + libro1.edicion());
        System.out.println("   Ubicación: " + libro1.ubicacion());

        // BUILDER: Libro mínimo (solo obligatorios — sin edición ni ubicación)
        Libro libro2 = servicio.registrarLibro(
            new Libro.Builder("978-607-07-0011-4", "Diseño de Bases de Datos")
                .editorial("Alfaomega")
                .anioPublicacion(2022)
        );
        System.out.println("✔ " + libro2);

        // BUILDER: Usuario estudiante con datos completos
        Usuario estudiante = servicio.registrarUsuario(
            new Usuario.Builder("2024-0015", "María", "Torres", "m.torres@uni.edu")
                .telefono("987654321")
                .rol("ESTUDIANTE")
                .carrera("Ingeniería de Sistemas")
        );
        System.out.println("\n✔ " + estudiante);
        System.out.println("   Carrera:   " + estudiante.carrera());
        System.out.println("   Teléfono:  " + estudiante.telefono());

        // BUILDER: Docente (solo obligatorios + rol, sin teléfono)
        Usuario docente = servicio.registrarUsuario(
            new Usuario.Builder("DOC-0042", "Carlos", "Mendoza", "c.mendoza@uni.edu")
                .rol("DOCENTE")
                .carrera("Facultad de Ingeniería")
        );
        System.out.println("✔ " + docente);

        // ── FACTORY METHOD: registrar préstamo ──────────────────────
        System.out.println("\n─── FACTORY METHOD (AbstractFactory + repos) ──────\n");

        // Registrar un ejemplar físico del libro (necesario para el préstamo)
        int idEjemplar = servicio.registrarEjemplar(libro1.id(), "EJ-001");

        Prestamo prestamo = servicio.realizarPrestamo("2024-0015", idEjemplar);
        System.out.println("✔ Préstamo registrado: id=" + prestamo.id()
            + ", devolución esperada: " + prestamo.fechaDevolucionEsperada());

        // ── BUILDER DE CONSULTAS ─────────────────────────────────────
        System.out.println("\n─── BUILDER PARA CONSULTAS DINÁMICAS ──────────────\n");

        // Préstamos activos de un usuario
        List<Prestamo> activosEstudiante = servicio.consultarPrestamos(
            new ConsultaPrestamoBuilder()
                .porUsuario(estudiante.id())
                .conEstado("ACTIVO")
        );
        System.out.println("✔ Préstamos activos del estudiante:   " + activosEstudiante.size());

        // Solo vencidos en el último mes
        List<Prestamo> vencidosUltimoMes = servicio.consultarPrestamos(
            new ConsultaPrestamoBuilder()
                .soloVencidos()
                .entreFehas(LocalDate.now().minusMonths(1), LocalDate.now())
        );
        System.out.println("✔ Préstamos vencidos (último mes):    " + vencidosUltimoMes.size());

        // Todos los préstamos sin filtro
        List<Prestamo> todos = servicio.consultarPrestamos(new ConsultaPrestamoBuilder());
        System.out.println("✔ Total de préstamos en el sistema:   " + todos.size());

        // Devolución
        servicio.devolverLibro(prestamo.id());
        System.out.println("\n✔ Devolución registrada. Multa calculada si aplica.");

        // ── RESUMEN ──────────────────────────────────────────────────
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║              PATRONES UTILIZADOS                 ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.println("║ Abstract Factory → BibliotecaRepositoryFactory   ║");
        System.out.println("║                    Postgres vs InMemory           ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.println("║ Factory Method   → crearLibroRepository()        ║");
        System.out.println("║                    crearUsuarioRepository() etc.  ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.println("║ Builder          → Libro.Builder (isbn, titulo)  ║");
        System.out.println("║                    Usuario.Builder (4 campos req) ║");
        System.out.println("║                    ConsultaPrestamoBuilder        ║");
        System.out.println("║                    (WHERE dinámico encadenado)    ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
    }

    // ── FACTORY HELPERS ──────────────────────────────────────────────

    /** Conexión real a PostgreSQL — lee credenciales de db.properties */
    static BibliotecaRepositoryFactory crearFactoryPostgres() {
        try {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream("db.properties")) {
                props.load(fis);
            } catch (IOException e) {
                throw new RuntimeException(
                    "No se encontró db.properties. Copia db.properties.example a db.properties y configura tus credenciales.", e);
            }
            Connection conn = DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.user"),
                props.getProperty("db.password"));
            crearTablasSiNoExisten(conn);
            return new PostgresBibliotecaFactory(conn);
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo conectar a PostgreSQL", e);
        }
    }

    /** Crea las tablas si todavía no existen en la base de datos */
    static void crearTablasSiNoExisten(Connection conn) throws SQLException {
        String[] ddl = {
            """
            CREATE TABLE IF NOT EXISTS roles (
                id_rol        SERIAL PRIMARY KEY,
                nombre        VARCHAR(50)  NOT NULL UNIQUE,
                descripcion   VARCHAR(200),
                max_libros    INT NOT NULL DEFAULT 3,
                dias_prestamo INT NOT NULL DEFAULT 7
            )
            """,
            "INSERT INTO roles (nombre, descripcion) SELECT 'ESTUDIANTE','Estudiante' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nombre='ESTUDIANTE')",
            "INSERT INTO roles (nombre, descripcion) SELECT 'DOCENTE','Docente'     WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nombre='DOCENTE')",
            "INSERT INTO roles (nombre, descripcion) SELECT 'ADMIN','Administrador' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nombre='ADMIN')",
            """
            CREATE TABLE IF NOT EXISTS carreras (
                id_carrera SERIAL PRIMARY KEY,
                nombre     VARCHAR(150) NOT NULL,
                facultad   VARCHAR(150) NOT NULL,
                activo     BOOLEAN DEFAULT TRUE
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS usuarios (
                id_usuario     SERIAL PRIMARY KEY,
                codigo         VARCHAR(20)  NOT NULL UNIQUE,
                nombres        VARCHAR(100) NOT NULL,
                apellidos      VARCHAR(100) NOT NULL,
                email          VARCHAR(150) NOT NULL UNIQUE,
                telefono       VARCHAR(20),
                id_rol         INT NOT NULL REFERENCES roles(id_rol),
                id_carrera     INT REFERENCES carreras(id_carrera),
                activo         BOOLEAN DEFAULT TRUE,
                fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS libros (
                id_libro         SERIAL PRIMARY KEY,
                isbn             VARCHAR(20)  NOT NULL UNIQUE,
                titulo           VARCHAR(300) NOT NULL,
                editorial        VARCHAR(150),
                anio_publicacion INT,
                edicion          VARCHAR(50),
                descripcion      TEXT,
                ubicacion        VARCHAR(100),
                activo           BOOLEAN DEFAULT TRUE,
                fecha_registro   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS ejemplares (
                id_ejemplar       SERIAL PRIMARY KEY,
                id_libro          INT NOT NULL REFERENCES libros(id_libro),
                codigo_barras     VARCHAR(50) NOT NULL UNIQUE,
                estado            VARCHAR(30) NOT NULL DEFAULT 'DISPONIBLE',
                fecha_adquisicion DATE,
                observaciones     TEXT
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS prestamos (
                id_prestamo               SERIAL PRIMARY KEY,
                id_usuario                INT NOT NULL REFERENCES usuarios(id_usuario),
                fecha_prestamo            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                fecha_devolucion_esperada DATE NOT NULL,
                fecha_devolucion_real     DATE,
                estado                   VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
                observaciones            TEXT,
                registrado_por           INT REFERENCES usuarios(id_usuario)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS detalle_prestamo (
                id_detalle        SERIAL PRIMARY KEY,
                id_prestamo       INT NOT NULL REFERENCES prestamos(id_prestamo) ON DELETE CASCADE,
                id_ejemplar       INT NOT NULL REFERENCES ejemplares(id_ejemplar),
                estado_devolucion VARCHAR(30) DEFAULT 'PENDIENTE'
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS multas (
                id_multa    SERIAL PRIMARY KEY,
                id_prestamo INT NOT NULL REFERENCES prestamos(id_prestamo),
                id_usuario  INT NOT NULL REFERENCES usuarios(id_usuario),
                dias_retraso INT NOT NULL DEFAULT 0,
                monto_total  NUMERIC(8,2) NOT NULL DEFAULT 0,
                pagado       BOOLEAN DEFAULT FALSE,
                fecha_pago   DATE
            )
            """
        };

        try (Statement st = conn.createStatement()) {
            for (String sql : ddl) {
                st.execute(sql);
            }
        }
        System.out.println("✔ Esquema de base de datos verificado/creado.");
    }
}
