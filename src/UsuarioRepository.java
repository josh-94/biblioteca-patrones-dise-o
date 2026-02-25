import java.util.List;
import java.util.Optional;

public interface UsuarioRepository {
    Optional<Usuario> findByCodigo(String codigo);
    List<Usuario>     findAll();
    Usuario           save(Usuario usuario);
}
