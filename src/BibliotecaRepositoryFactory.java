/**
 * Abstract Factory — define la familia de repositorios.
 * Permite cambiar de implementación (PostgreSQL ↔ InMemory) en una sola línea.
 */
public interface BibliotecaRepositoryFactory {
    LibroRepository    crearLibroRepository();
    UsuarioRepository  crearUsuarioRepository();
    PrestamoRepository crearPrestamoRepository();
    MultaRepository    crearMultaRepository();
}
