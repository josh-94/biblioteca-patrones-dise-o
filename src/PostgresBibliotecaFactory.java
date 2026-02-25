import java.sql.Connection;

/**
 * Concrete Factory — PostgreSQL.
 * Cada crearXxx() es el Factory Method que instancia el repositorio concreto.
 */
public class PostgresBibliotecaFactory implements BibliotecaRepositoryFactory {

    private final Connection connection;

    PostgresBibliotecaFactory(Connection connection) { this.connection = connection; }

    @Override
    public LibroRepository crearLibroRepository() {
        return new PostgresLibroRepository(connection);
    }

    @Override
    public UsuarioRepository crearUsuarioRepository() {
        return new PostgresUsuarioRepository(connection);
    }

    @Override
    public PrestamoRepository crearPrestamoRepository() {
        return new PostgresPrestamoRepository(connection);
    }

    @Override
    public MultaRepository crearMultaRepository() {
        return new PostgresMultaRepository(connection);
    }
}
