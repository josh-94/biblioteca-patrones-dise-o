import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación PostgreSQL de LibroRepository.
 * Usa el Builder de Libro para construir objetos desde ResultSet.
 */
public class PostgresLibroRepository implements LibroRepository {

    private final Connection conn;

    PostgresLibroRepository(Connection conn) { this.conn = conn; }

    @Override
    public Optional<Libro> findByIsbn(String isbn) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM libros WHERE isbn = ?")) {
            ps.setString(1, isbn);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapLibro(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    @Override
    public List<Libro> findDisponibles() {
        String sql = """
            SELECT DISTINCT l.* FROM libros l
            JOIN ejemplares e ON l.id_libro = e.id_libro
            WHERE e.estado = 'DISPONIBLE' AND l.activo = TRUE
            """;
        List<Libro> lista = new ArrayList<>();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapLibro(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    @Override
    public Libro save(Libro libro) {
        String sql = """
            INSERT INTO libros (isbn, titulo, editorial, anio_publicacion, edicion, descripcion, ubicacion)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (isbn) DO UPDATE SET titulo=EXCLUDED.titulo RETURNING id_libro
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, libro.isbn()); ps.setString(2, libro.titulo());
            ps.setString(3, libro.editorial()); ps.setInt(4, libro.anioPublicacion());
            ps.setString(5, libro.edicion()); ps.setString(6, libro.descripcion());
            ps.setString(7, libro.ubicacion());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Libro.Builder(libro.isbn(), libro.titulo())
                    .id(rs.getInt(1)).editorial(libro.editorial())
                    .anioPublicacion(libro.anioPublicacion()).edicion(libro.edicion())
                    .descripcion(libro.descripcion()).ubicacion(libro.ubicacion())
                    .build();
            }
        } catch (SQLException e) { e.printStackTrace(); }
        throw new RuntimeException("No se pudo guardar el libro");
    }

    @Override
    public boolean delete(int id) {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE libros SET activo=FALSE WHERE id_libro=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public int insertarEjemplar(int idLibro, String codigoBarras) {
        String sql = """
            INSERT INTO ejemplares (id_libro, codigo_barras)
            VALUES (?, ?)
            ON CONFLICT (codigo_barras) DO NOTHING RETURNING id_ejemplar
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idLibro); ps.setString(2, codigoBarras);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
            // Si ya existía (ON CONFLICT), buscar el id existente
            try (PreparedStatement ps2 = conn.prepareStatement(
                    "SELECT id_ejemplar FROM ejemplares WHERE codigo_barras=?")) {
                ps2.setString(1, codigoBarras);
                ResultSet rs2 = ps2.executeQuery();
                if (rs2.next()) return rs2.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        throw new RuntimeException("No se pudo registrar el ejemplar");
    }

    /** Builder usado para mapear ResultSet → Libro de forma legible */
    private Libro mapLibro(ResultSet rs) throws SQLException {
        return new Libro.Builder(rs.getString("isbn"), rs.getString("titulo"))
            .id(rs.getInt("id_libro")).editorial(rs.getString("editorial"))
            .anioPublicacion(rs.getInt("anio_publicacion")).edicion(rs.getString("edicion"))
            .descripcion(rs.getString("descripcion")).ubicacion(rs.getString("ubicacion"))
            .build();
    }
}
