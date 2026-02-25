import java.util.List;
import java.util.Optional;

public interface LibroRepository {
    Optional<Libro> findByIsbn(String isbn);
    List<Libro>     findDisponibles();
    Libro           save(Libro libro);
    boolean         delete(int id);
    int             insertarEjemplar(int idLibro, String codigoBarras);
}
