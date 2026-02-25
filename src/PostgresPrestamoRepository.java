import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación PostgreSQL de PrestamoRepository.
 * Usa ConsultaPrestamoBuilder para construir WHERE dinámico.
 */
public class PostgresPrestamoRepository implements PrestamoRepository {

    private final Connection conn;

    PostgresPrestamoRepository(Connection conn) { this.conn = conn; }

    @Override
    public Prestamo registrar(int idUsuario, int idEjemplar) {
        try {
            conn.setAutoCommit(false);

            String sqlP = """
                INSERT INTO prestamos (id_usuario, fecha_devolucion_esperada)
                SELECT ?, CURRENT_DATE + r.dias_prestamo
                FROM usuarios u JOIN roles r ON u.id_rol = r.id_rol
                WHERE u.id_usuario = ?
                RETURNING id_prestamo, id_usuario, fecha_prestamo, fecha_devolucion_esperada, estado
                """;
            PreparedStatement ps1 = conn.prepareStatement(sqlP);
            ps1.setInt(1, idUsuario); ps1.setInt(2, idUsuario);
            ResultSet rs = ps1.executeQuery();
            if (!rs.next()) throw new RuntimeException("Error creando préstamo");

            int idPrestamo = rs.getInt("id_prestamo");
            Prestamo prestamo = new Prestamo(
                idPrestamo, idUsuario,
                rs.getDate("fecha_prestamo").toLocalDate(),
                rs.getDate("fecha_devolucion_esperada").toLocalDate(),
                rs.getString("estado"));

            PreparedStatement ps2 = conn.prepareStatement(
                "INSERT INTO detalle_prestamo (id_prestamo, id_ejemplar) VALUES (?, ?)");
            ps2.setInt(1, idPrestamo); ps2.setInt(2, idEjemplar);
            ps2.executeUpdate();

            PreparedStatement ps3 = conn.prepareStatement(
                "UPDATE ejemplares SET estado='PRESTADO' WHERE id_ejemplar=?");
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
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE prestamos SET estado='DEVUELTO', fecha_devolucion_real=CURRENT_DATE WHERE id_prestamo=?")) {
            ps.setInt(1, idPrestamo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /**
     * ConsultaPrestamoBuilder genera el WHERE dinámico.
     * Un solo método cubre todos los casos de búsqueda sin duplicar código.
     */
    @Override
    public List<Prestamo> buscar(ConsultaPrestamoBuilder consulta) {
        String sql = "SELECT * FROM prestamos p WHERE " + consulta.buildWhere();
        List<Prestamo> lista = new ArrayList<>();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Prestamo(
                    rs.getInt("id_prestamo"), rs.getInt("id_usuario"),
                    rs.getDate("fecha_prestamo").toLocalDate(),
                    rs.getDate("fecha_devolucion_esperada").toLocalDate(),
                    rs.getString("estado")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }
}
