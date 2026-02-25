// ================================================================
// SISTEMA DE BIBLIOTECA UNIVERSITARIA
// Patrones: Abstract Factory + Factory Method
// Java 17 | PostgreSQL via JDBC
// ================================================================

// ── Dependencias en pom.xml (Maven) ──────────────────────────
// <dependency>
//   <groupId>org.postgresql</groupId>
//   <artifactId>postgresql</artifactId>
//   <version>42.7.3</version>
// </dependency>

package biblioteca;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// ================================================================
// 1. MODELOS (POJOs)
// ================================================================

record Libro(int id, String isbn, String titulo, String editorial, int anioPublicacion) {}
record Usuario(int id, String codigo, String nombres, String apellidos, String email) {}
record Prestamo(int id, int idUsuario, LocalDate fechaPrestamo, LocalDate fechaDevolucionEsperada, String estado) {}
record Multa(int id, int idPrestamo, int diasRetraso, double montoTotal, boolean pagado) {}

// ================================================================
// 2. INTERFACES DE REPOSITORIO (Contratos)
// ================================================================

interface LibroRepository {
    Optional<Libro>  findByIsbn(String isbn);
    List<Libro>      findDisponibles();
    Libro            save(Libro libro);
    boolean          delete(int id);
}

interface UsuarioRepository {
    Optional<Usuario> findByCodigo(String codigo);
    List<Usuario>     findAll();
    Usuario           save(Usuario usuario);
}

interface PrestamoRepository {
    Prestamo          registrar(int idUsuario, int idEjemplar);
    boolean           devolver(int idPrestamo);
    List<Prestamo>    findActivosByUsuario(int idUsuario);
}

interface MultaRepository {
    Optional<Multa>  findByPrestamo(int idPrestamo);
    Multa            calcularYGuardar(int idPrestamo);
    boolean          pagar(int idMulta);
}

// ================================================================
// 3. ABSTRACT FACTORY
//    Crea familias de repositorios compatibles entre sí
// ================================================================

interface BibliotecaRepositoryFactory {
    LibroRepository    crearLibroRepository();
    UsuarioRepository  crearUsuarioRepository();
    PrestamoRepository crearPrestamoRepository();
    MultaRepository    crearMultaRepository();
}

// ================================================================
// 4. IMPLEMENTACIONES CONCRETAS — PostgreSQL
// ================================================================

class PostgresLibroRepository implements LibroRepository {

    private final Connection conn;

    PostgresLibroRepository(Connection conn) { this.conn = conn; }

    @Override
    public Optional<Libro> findByIsbn(String isbn) {
        String sql = "SELECT id_libro, isbn, titulo, editorial, anio_publicacion FROM libros WHERE isbn = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, isbn);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(new Libro(
                    rs.getInt("id_libro"),
                    rs.getString("isbn"),
                    rs.getString("titulo"),
                    rs.getString("editorial"),
                    rs.getInt("anio_publicacion")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    @Override
    public List<Libro> findDisponibles() {
        String sql = """
            SELECT DISTINCT l.id_libro, l.isbn, l.titulo, l.editorial, l.anio_publicacion
            FROM libros l
            JOIN ejemplares e ON l.id_libro = e.id_libro
            WHERE e.estado = 'DISPONIBLE' AND l.activo = TRUE
            """;
        List<Libro> lista = new ArrayList<>();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Libro(
                    rs.getInt("id_libro"),
                    rs.getString("isbn"),
                    rs.getString("titulo"),
                    rs.getString("editorial"),
                    rs.getInt("anio_publicacion")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    @Override
    public Libro save(Libro libro) {
        String sql = """
            INSERT INTO libros (isbn, titulo, editorial, anio_publicacion)
            VALUES (?, ?, ?, ?) RETURNING id_libro
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, libro.isbn());
            ps.setString(2, libro.titulo());
            ps.setString(3, libro.editorial());
            ps.setInt(4, libro.anioPublicacion());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Libro(rs.getInt(1), libro.isbn(), libro.titulo(), libro.editorial(), libro.anioPublicacion());
            }
        } catch (SQLException e) { e.printStackTrace(); }
        throw new RuntimeException("No se pudo guardar el libro");
    }

    @Override
    public boolean delete(int id) {
        String sql = "UPDATE libros SET activo = FALSE WHERE id_libro = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}

class PostgresUsuarioRepository implements UsuarioRepository {

    private final Connection conn;

    PostgresUsuarioRepository(Connection conn) { this.conn = conn; }

    @Override
    public Optional<Usuario> findByCodigo(String codigo) {
        String sql = "SELECT id_usuario, codigo, nombres, apellidos, email FROM usuarios WHERE codigo = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(new Usuario(
                    rs.getInt("id_usuario"),
                    rs.getString("codigo"),
                    rs.getString("nombres"),
                    rs.getString("apellidos"),
                    rs.getString("email")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    @Override
    public List<Usuario> findAll() {
        String sql = "SELECT id_usuario, codigo, nombres, apellidos, email FROM usuarios WHERE activo = TRUE";
        List<Usuario> lista = new ArrayList<>();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Usuario(
                    rs.getInt("id_usuario"), rs.getString("codigo"),
                    rs.getString("nombres"), rs.getString("apellidos"),
                    rs.getString("email")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    @Override
    public Usuario save(Usuario u) {
        String sql = """
            INSERT INTO usuarios (codigo, nombres, apellidos, email, id_rol)
            VALUES (?, ?, ?, ?, 1) RETURNING id_usuario
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.codigo());
            ps.setString(2, u.nombres());
            ps.setString(3, u.apellidos());
            ps.setString(4, u.email());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Usuario(rs.getInt(1), u.codigo(), u.nombres(), u.apellidos(), u.email());
            }
        } catch (SQLException e) { e.printStackTrace(); }
        throw new RuntimeException("No se pudo guardar el usuario");
    }
}

class PostgresPrestamoRepository implements PrestamoRepository {

    private final Connection conn;

    PostgresPrestamoRepository(Connection conn) { this.conn = conn; }

    @Override
    public Prestamo registrar(int idUsuario, int idEjemplar) {
        try {
            conn.setAutoCommit(false);

            // 1. Insertar préstamo
            String sqlPrestamo = """
                INSERT INTO prestamos (id_usuario, fecha_devolucion_esperada)
                SELECT ?, CURRENT_DATE + r.dias_prestamo
                FROM usuarios u JOIN roles r ON u.id_rol = r.id_rol
                WHERE u.id_usuario = ?
                RETURNING id_prestamo, id_usuario, fecha_prestamo, fecha_devolucion_esperada, estado
                """;
            PreparedStatement ps1 = conn.prepareStatement(sqlPrestamo);
            ps1.setInt(1, idUsuario);
            ps1.setInt(2, idUsuario);
            ResultSet rs = ps1.executeQuery();
            if (!rs.next()) throw new RuntimeException("Error creando préstamo");

            int idPrestamo = rs.getInt("id_prestamo");
            Prestamo prestamo = new Prestamo(
                idPrestamo, rs.getInt("id_usuario"),
                rs.getDate("fecha_prestamo").toLocalDate(),
                rs.getDate("fecha_devolucion_esperada").toLocalDate(),
                rs.getString("estado")
            );

            // 2. Insertar detalle
            String sqlDetalle = "INSERT INTO detalle_prestamo (id_prestamo, id_ejemplar) VALUES (?, ?)";
            PreparedStatement ps2 = conn.prepareStatement(sqlDetalle);
            ps2.setInt(1, idPrestamo);
            ps2.setInt(2, idEjemplar);
            ps2.executeUpdate();

            // 3. Cambiar estado del ejemplar
            String sqlEjemplar = "UPDATE ejemplares SET estado = 'PRESTADO' WHERE id_ejemplar = ?";
            PreparedStatement ps3 = conn.prepareStatement(sqlEjemplar);
            ps3.setInt(1, idEjemplar);
            ps3.executeUpdate();

            conn.commit();
            return prestamo;

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            throw new RuntimeException("Error registrando préstamo", e);
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @Override
    public boolean devolver(int idPrestamo) {
        String sql = """
            UPDATE prestamos
            SET estado = 'DEVUELTO', fecha_devolucion_real = CURRENT_DATE
            WHERE id_prestamo = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPrestamo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public List<Prestamo> findActivosByUsuario(int idUsuario) {
        String sql = """
            SELECT id_prestamo, id_usuario, fecha_prestamo,
                   fecha_devolucion_esperada, estado
            FROM prestamos
            WHERE id_usuario = ? AND estado = 'ACTIVO'
            """;
        List<Prestamo> lista = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(new Prestamo(
                    rs.getInt("id_prestamo"), rs.getInt("id_usuario"),
                    rs.getDate("fecha_prestamo").toLocalDate(),
                    rs.getDate("fecha_devolucion_esperada").toLocalDate(),
                    rs.getString("estado")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }
}

class PostgresMultaRepository implements MultaRepository {

    private final Connection conn;

    PostgresMultaRepository(Connection conn) { this.conn = conn; }

    @Override
    public Optional<Multa> findByPrestamo(int idPrestamo) {
        String sql = "SELECT * FROM multas WHERE id_prestamo = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPrestamo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(new Multa(
                    rs.getInt("id_multa"), rs.getInt("id_prestamo"),
                    rs.getInt("dias_retraso"), rs.getDouble("monto_total"),
                    rs.getBoolean("pagado")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    @Override
    public Multa calcularYGuardar(int idPrestamo) {
        String sql = """
            INSERT INTO multas (id_prestamo, id_usuario, dias_retraso, monto_total)
            SELECT
                p.id_prestamo,
                p.id_usuario,
                GREATEST(0, CURRENT_DATE - p.fecha_devolucion_esperada),
                calcular_multa(p.id_prestamo)
            FROM prestamos p
            WHERE p.id_prestamo = ?
            RETURNING id_multa, id_prestamo, dias_retraso, monto_total, pagado
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPrestamo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Multa(
                    rs.getInt("id_multa"), rs.getInt("id_prestamo"),
                    rs.getInt("dias_retraso"), rs.getDouble("monto_total"),
                    rs.getBoolean("pagado")
                );
            }
        } catch (SQLException e) { e.printStackTrace(); }
        throw new RuntimeException("No se pudo calcular la multa");
    }

    @Override
    public boolean pagar(int idMulta) {
        String sql = "UPDATE multas SET pagado = TRUE, fecha_pago = NOW() WHERE id_multa = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idMulta);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}

// ================================================================
// 5. CONCRETE FACTORY — PostgreSQL
//    (Factory Method: cada método crea el producto concreto)
// ================================================================

class PostgresBibliotecaFactory implements BibliotecaRepositoryFactory {

    private final Connection connection;

    PostgresBibliotecaFactory(Connection connection) {
        this.connection = connection;
    }

    // ── Factory Methods ───────────────────────────────────────
    @Override
    public LibroRepository crearLibroRepository() {
        return new PostgresLibroRepository(connection);    // Factory Method
    }

    @Override
    public UsuarioRepository crearUsuarioRepository() {
        return new PostgresUsuarioRepository(connection);  // Factory Method
    }

    @Override
    public PrestamoRepository crearPrestamoRepository() {
        return new PostgresPrestamoRepository(connection); // Factory Method
    }

    @Override
    public MultaRepository crearMultaRepository() {
        return new PostgresMultaRepository(connection);    // Factory Method
    }
}

// ================================================================
// 6. IMPLEMENTACIÓN IN-MEMORY (para pruebas / mock)
// ================================================================

class InMemoryBibliotecaFactory implements BibliotecaRepositoryFactory {

    @Override
    public LibroRepository crearLibroRepository() {
        return new LibroRepository() {
            private final List<Libro> libros = new ArrayList<>();
            private int seq = 1;
            public Optional<Libro> findByIsbn(String isbn) {
                return libros.stream().filter(l -> l.isbn().equals(isbn)).findFirst();
            }
            public List<Libro> findDisponibles() { return new ArrayList<>(libros); }
            public Libro save(Libro l) {
                Libro saved = new Libro(seq++, l.isbn(), l.titulo(), l.editorial(), l.anioPublicacion());
                libros.add(saved); return saved;
            }
            public boolean delete(int id) { return libros.removeIf(l -> l.id() == id); }
        };
    }

    @Override
    public UsuarioRepository crearUsuarioRepository() {
        return new UsuarioRepository() {
            private final List<Usuario> usuarios = new ArrayList<>();
            private int seq = 1;
            public Optional<Usuario> findByCodigo(String codigo) {
                return usuarios.stream().filter(u -> u.codigo().equals(codigo)).findFirst();
            }
            public List<Usuario> findAll() { return new ArrayList<>(usuarios); }
            public Usuario save(Usuario u) {
                Usuario saved = new Usuario(seq++, u.codigo(), u.nombres(), u.apellidos(), u.email());
                usuarios.add(saved); return saved;
            }
        };
    }

    @Override
    public PrestamoRepository crearPrestamoRepository() {
        return new PrestamoRepository() {
            private final List<Prestamo> prestamos = new ArrayList<>();
            private int seq = 1;
            public Prestamo registrar(int idUsuario, int idEjemplar) {
                Prestamo p = new Prestamo(seq++, idUsuario, LocalDate.now(), LocalDate.now().plusDays(7), "ACTIVO");
                prestamos.add(p); return p;
            }
            public boolean devolver(int id) {
                return prestamos.stream().anyMatch(p -> p.id() == id);
            }
            public List<Prestamo> findActivosByUsuario(int id) {
                return prestamos.stream().filter(p -> p.idUsuario() == id && "ACTIVO".equals(p.estado())).toList();
            }
        };
    }

    @Override
    public MultaRepository crearMultaRepository() {
        return new MultaRepository() {
            private final List<Multa> multas = new ArrayList<>();
            private int seq = 1;
            public Optional<Multa> findByPrestamo(int id) {
                return multas.stream().filter(m -> m.idPrestamo() == id).findFirst();
            }
            public Multa calcularYGuardar(int idPrestamo) {
                Multa m = new Multa(seq++, idPrestamo, 3, 3.0, false);
                multas.add(m); return m;
            }
            public boolean pagar(int id) { return multas.stream().anyMatch(m -> m.id() == id); }
        };
    }
}

// ================================================================
// 7. SERVICIOS DE APLICACIÓN (usan la factory inyectada)
// ================================================================

class BibliotecaService {

    private final LibroRepository    libroRepo;
    private final UsuarioRepository  usuarioRepo;
    private final PrestamoRepository prestamoRepo;
    private final MultaRepository    multaRepo;

    // El servicio recibe la factory → no conoce la implementación concreta
    BibliotecaService(BibliotecaRepositoryFactory factory) {
        this.libroRepo    = factory.crearLibroRepository();
        this.usuarioRepo  = factory.crearUsuarioRepository();
        this.prestamoRepo = factory.crearPrestamoRepository();
        this.multaRepo    = factory.crearMultaRepository();
    }

    public Libro registrarLibro(String isbn, String titulo, String editorial, int anio) {
        return libroRepo.save(new Libro(0, isbn, titulo, editorial, anio));
    }

    public Usuario registrarUsuario(String codigo, String nombres, String apellidos, String email) {
        return usuarioRepo.save(new Usuario(0, codigo, nombres, apellidos, email));
    }

    public Prestamo realizarPrestamo(String codigoUsuario, int idEjemplar) {
        Usuario usuario = usuarioRepo.findByCodigo(codigoUsuario)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + codigoUsuario));
        return prestamoRepo.registrar(usuario.id(), idEjemplar);
    }

    public void devolverLibro(int idPrestamo) {
        prestamoRepo.devolver(idPrestamo);
        // Si hay retraso, calcular multa automáticamente
        multaRepo.calcularYGuardar(idPrestamo);
    }

    public void pagarMulta(int idMulta) {
        multaRepo.pagar(idMulta);
    }

    public List<Libro> consultarDisponibles() {
        return libroRepo.findDisponibles();
    }
}

// ================================================================
// 8. MAIN — Demostración
// ================================================================

class Main {

    public static void main(String[] args) {

        System.out.println("=== BIBLIOTECA UNIVERSITARIA ===\n");

        // ── Cambiar entre implementaciones solo cambiando la factory ──
        // Para producción:
        //   BibliotecaRepositoryFactory factory = crearFactoryPostgres();
        // Para pruebas:
        BibliotecaRepositoryFactory factory = new InMemoryBibliotecaFactory();

        BibliotecaService servicio = new BibliotecaService(factory);

        // Registrar un libro
        Libro libro = servicio.registrarLibro(
            "978-84-206-7417-4", "Clean Code", "Prentice Hall", 2008
        );
        System.out.println("✔ Libro registrado: " + libro);

        // Registrar un usuario
        Usuario usuario = servicio.registrarUsuario(
            "2024001", "Ana", "García", "ana.garcia@universidad.edu"
        );
        System.out.println("✔ Usuario registrado: " + usuario);

        // Realizar un préstamo
        Prestamo prestamo = servicio.realizarPrestamo("2024001", 1);
        System.out.println("✔ Préstamo registrado: " + prestamo);

        // Devolver (genera multa si hay retraso)
        servicio.devolverLibro(prestamo.id());
        System.out.println("✔ Libro devuelto, multa calculada si aplica.");

        // Listar disponibles
        List<Libro> disponibles = servicio.consultarDisponibles();
        System.out.println("✔ Libros disponibles: " + disponibles.size());
    }

    /** Conexión real a PostgreSQL */
    private static BibliotecaRepositoryFactory crearFactoryPostgres() {
        try {
            Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/biblioteca_db",
                "postgres",
                "tu_password"
            );
            return new PostgresBibliotecaFactory(conn);
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo conectar a PostgreSQL", e);
        }
    }
}
