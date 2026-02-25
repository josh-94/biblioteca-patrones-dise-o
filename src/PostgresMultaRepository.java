import java.sql.*;
import java.util.Optional;

/**
 * Implementación PostgreSQL de MultaRepository.
 */
public class PostgresMultaRepository implements MultaRepository {

    private final Connection conn;

    PostgresMultaRepository(Connection conn) { this.conn = conn; }

    @Override
    public Optional<Multa> findByPrestamo(int idPrestamo) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM multas WHERE id_prestamo=?")) {
            ps.setInt(1, idPrestamo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(new Multa(
                rs.getInt("id_multa"), rs.getInt("id_prestamo"),
                rs.getInt("dias_retraso"), rs.getDouble("monto_total"),
                rs.getBoolean("pagado")));
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    @Override
    public Multa calcularYGuardar(int idPrestamo) {
        String sql = """
            INSERT INTO multas (id_prestamo, id_usuario, dias_retraso, monto_total)
            SELECT p.id_prestamo, p.id_usuario,
                   GREATEST(0, CURRENT_DATE - p.fecha_devolucion_esperada),
                   calcular_multa(p.id_prestamo)
            FROM prestamos p WHERE p.id_prestamo = ?
            RETURNING id_multa, id_prestamo, dias_retraso, monto_total, pagado
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPrestamo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new Multa(
                rs.getInt("id_multa"), rs.getInt("id_prestamo"),
                rs.getInt("dias_retraso"), rs.getDouble("monto_total"),
                rs.getBoolean("pagado"));
        } catch (SQLException e) { e.printStackTrace(); }
        throw new RuntimeException("No se pudo calcular la multa");
    }

    @Override
    public boolean pagar(int idMulta) {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE multas SET pagado=TRUE, fecha_pago=NOW() WHERE id_multa=?")) {
            ps.setInt(1, idMulta);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}
