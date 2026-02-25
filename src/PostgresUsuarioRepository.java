import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación PostgreSQL de UsuarioRepository.
 * Usa el Builder de Usuario para construir objetos desde ResultSet.
 */
public class PostgresUsuarioRepository implements UsuarioRepository {

    private final Connection conn;

    PostgresUsuarioRepository(Connection conn) { this.conn = conn; }

    @Override
    public Optional<Usuario> findByCodigo(String codigo) {
        String sql = """
            SELECT u.*, r.nombre AS rol, c.nombre AS carrera
            FROM usuarios u
            JOIN roles r ON u.id_rol = r.id_rol
            LEFT JOIN carreras c ON u.id_carrera = c.id_carrera
            WHERE u.codigo = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapUsuario(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    @Override
    public List<Usuario> findAll() {
        String sql = """
            SELECT u.*, r.nombre AS rol
            FROM usuarios u
            JOIN roles r ON u.id_rol = r.id_rol
            WHERE u.activo = TRUE
            """;
        List<Usuario> lista = new ArrayList<>();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapUsuario(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    @Override
    public Usuario save(Usuario u) {
        String sql = """
            INSERT INTO usuarios (codigo, nombres, apellidos, email, telefono, id_rol)
            VALUES (?, ?, ?, ?, ?, 1)
            ON CONFLICT (email) DO UPDATE SET nombres=EXCLUDED.nombres
            RETURNING id_usuario
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.codigo()); ps.setString(2, u.nombres());
            ps.setString(3, u.apellidos()); ps.setString(4, u.email());
            ps.setString(5, u.telefono());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Usuario.Builder(u.codigo(), u.nombres(), u.apellidos(), u.email())
                    .id(rs.getInt(1)).telefono(u.telefono()).rol(u.rol()).carrera(u.carrera())
                    .build();
            }
        } catch (SQLException e) { e.printStackTrace(); }
        throw new RuntimeException("No se pudo guardar el usuario");
    }

    /** Builder usado para mapear ResultSet → Usuario */
    private Usuario mapUsuario(ResultSet rs) throws SQLException {
        return new Usuario.Builder(
                rs.getString("codigo"), rs.getString("nombres"),
                rs.getString("apellidos"), rs.getString("email"))
            .id(rs.getInt("id_usuario")).telefono(rs.getString("telefono"))
            .rol(rs.getString("rol")).carrera(rs.getString("carrera"))
            .build();
    }
}
