import java.time.LocalDate;

public record Prestamo(
    int id,
    int idUsuario,
    LocalDate fechaPrestamo,
    LocalDate fechaDevolucionEsperada,
    String estado
) {}
