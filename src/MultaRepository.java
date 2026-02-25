import java.util.Optional;

public interface MultaRepository {
    Optional<Multa> findByPrestamo(int idPrestamo);
    Multa           calcularYGuardar(int idPrestamo);
    boolean         pagar(int idMulta);
}
