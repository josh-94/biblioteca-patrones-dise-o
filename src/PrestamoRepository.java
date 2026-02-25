import java.util.List;

public interface PrestamoRepository {
    Prestamo       registrar(int idUsuario, int idEjemplar);
    boolean        devolver(int idPrestamo);
    List<Prestamo> buscar(ConsultaPrestamoBuilder consulta);
}
