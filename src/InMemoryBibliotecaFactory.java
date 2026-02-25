import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Concrete Factory — InMemory (para pruebas / demos sin base de datos).
 * También usa los Builders para construir objetos consistentes.
 */
public class InMemoryBibliotecaFactory implements BibliotecaRepositoryFactory {

    @Override
    public LibroRepository crearLibroRepository() {
        return new LibroRepository() {
            private final List<Libro> libros = new ArrayList<>();
            private int seq = 1;

            public Optional<Libro> findByIsbn(String isbn) {
                return libros.stream().filter(l -> l.isbn().equals(isbn)).findFirst();
            }
            public List<Libro> findDisponibles() { return new ArrayList<>(libros); }
            public Libro save(Libro l) {
                Libro saved = new Libro.Builder(l.isbn(), l.titulo())
                    .id(seq++).editorial(l.editorial()).anioPublicacion(l.anioPublicacion())
                    .edicion(l.edicion()).descripcion(l.descripcion()).ubicacion(l.ubicacion())
                    .build();
                libros.add(saved); return saved;
            }
            public boolean delete(int id) { return libros.removeIf(l -> l.id() == id); }
            public int insertarEjemplar(int idLibro, String codigoBarras) { return 1; }
        };
    }

    @Override
    public UsuarioRepository crearUsuarioRepository() {
        return new UsuarioRepository() {
            private final List<Usuario> usuarios = new ArrayList<>();
            private int seq = 1;

            public Optional<Usuario> findByCodigo(String codigo) {
                return usuarios.stream().filter(u -> u.codigo().equals(codigo)).findFirst();
            }
            public List<Usuario> findAll() { return new ArrayList<>(usuarios); }
            public Usuario save(Usuario u) {
                Usuario saved = new Usuario.Builder(u.codigo(), u.nombres(), u.apellidos(), u.email())
                    .id(seq++).telefono(u.telefono()).rol(u.rol()).carrera(u.carrera())
                    .build();
                usuarios.add(saved); return saved;
            }
        };
    }

    @Override
    public PrestamoRepository crearPrestamoRepository() {
        return new PrestamoRepository() {
            private final List<Prestamo> prestamos = new ArrayList<>();
            private int seq = 1;

            public Prestamo registrar(int idUsuario, int idEjemplar) {
                Prestamo p = new Prestamo(seq++, idUsuario,
                    LocalDate.now(), LocalDate.now().plusDays(7), "ACTIVO");
                prestamos.add(p); return p;
            }
            public boolean devolver(int id) {
                return prestamos.stream().anyMatch(p -> p.id() == id);
            }
            public List<Prestamo> buscar(ConsultaPrestamoBuilder consulta) {
                return new ArrayList<>(prestamos);
            }
        };
    }

    @Override
    public MultaRepository crearMultaRepository() {
        return new MultaRepository() {
            private final List<Multa> multas = new ArrayList<>();
            private int seq = 1;

            public Optional<Multa> findByPrestamo(int id) {
                return multas.stream().filter(m -> m.idPrestamo() == id).findFirst();
            }
            public Multa calcularYGuardar(int idPrestamo) {
                Multa m = new Multa(seq++, idPrestamo, 3, 3.0, false);
                multas.add(m); return m;
            }
            public boolean pagar(int id) {
                return multas.stream().anyMatch(m -> m.id() == id);
            }
        };
    }
}
