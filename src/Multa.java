public record Multa(
    int id,
    int idPrestamo,
    int diasRetraso,
    double montoTotal,
    boolean pagado
) {}
