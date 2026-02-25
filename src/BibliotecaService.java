import java.util.List;

/**
 * Servicio de aplicación — orquesta los repositorios.
 * Acepta Builders directamente y llama a build() internamente,
 * manteniendo la creación de objetos desacoplada del cliente.
 */
public class BibliotecaService {

    private final LibroRepository    libroRepo;
    private final UsuarioRepository  usuarioRepo;
    private final PrestamoRepository prestamoRepo;
    private final MultaRepository    multaRepo;

    public BibliotecaService(BibliotecaRepositoryFactory factory) {
        this.libroRepo    = factory.crearLibroRepository();
        this.usuarioRepo  = factory.crearUsuarioRepository();
        this.prestamoRepo = factory.crearPrestamoRepository();
        this.multaRepo    = factory.crearMultaRepository();
    }

    /** El servicio acepta un Builder, llama a build() internamente */
    public Libro registrarLibro(Libro.Builder builder) {
        return libroRepo.save(builder.build());
    }

    public Usuario registrarUsuario(Usuario.Builder builder) {
        return usuarioRepo.save(builder.build());
    }

    public Prestamo realizarPrestamo(String codigoUsuario, int idEjemplar) {
        Usuario usuario = usuarioRepo.findByCodigo(codigoUsuario)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + codigoUsuario));
        return prestamoRepo.registrar(usuario.id(), idEjemplar);
    }

    /** Consulta flexible gracias al ConsultaPrestamoBuilder */
    public List<Prestamo> consultarPrestamos(ConsultaPrestamoBuilder consulta) {
        return prestamoRepo.buscar(consulta);
    }

    public void devolverLibro(int idPrestamo) {
        prestamoRepo.devolver(idPrestamo);
        multaRepo.calcularYGuardar(idPrestamo);
    }

    public void pagarMulta(int idMulta) { multaRepo.pagar(idMulta); }

    public List<Libro> consultarDisponibles() { return libroRepo.findDisponibles(); }

    /** Registra un ejemplar físico de un libro (necesario para realizar préstamos) */
    public int registrarEjemplar(int idLibro, String codigoBarras) {
        return libroRepo.insertarEjemplar(idLibro, codigoBarras);
    }
}
