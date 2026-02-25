import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder para consultas dinámicas de préstamos.
 * Construye criterios de búsqueda de forma encadenada,
 * evitando métodos findByXxx() duplicados en el repositorio.
 */
public class ConsultaPrestamoBuilder {

    private Integer   idUsuario    = null;
    private String    estado       = null;
    private LocalDate fechaDesde   = null;
    private LocalDate fechaHasta   = null;
    private boolean   soloVencidos = false;

    public ConsultaPrestamoBuilder porUsuario(int idUsuario)                    { this.idUsuario    = idUsuario; return this; }
    public ConsultaPrestamoBuilder conEstado(String estado)                     { this.estado       = estado;    return this; }
    public ConsultaPrestamoBuilder entreFehas(LocalDate desde, LocalDate hasta) { this.fechaDesde   = desde;
                                                                                  this.fechaHasta   = hasta;    return this; }
    public ConsultaPrestamoBuilder soloVencidos()                               { this.soloVencidos = true;     return this; }

    /** Genera el fragmento WHERE dinámico para la consulta SQL */
    public String buildWhere() {
        List<String> condiciones = new ArrayList<>();
        if (idUsuario  != null) condiciones.add("p.id_usuario = " + idUsuario);
        if (estado     != null) condiciones.add("p.estado = '" + estado + "'");
        if (fechaDesde != null) condiciones.add("p.fecha_prestamo >= '" + fechaDesde + "'");
        if (fechaHasta != null) condiciones.add("p.fecha_prestamo <= '" + fechaHasta + "'");
        if (soloVencidos)       condiciones.add("p.fecha_devolucion_esperada < CURRENT_DATE");
        return condiciones.isEmpty() ? "1=1" : String.join(" AND ", condiciones);
    }
}
