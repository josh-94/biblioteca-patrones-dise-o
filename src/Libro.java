/**
 * Libro: tiene campos obligatorios (isbn, titulo) y varios opcionales.
 * El patrón Builder evita constructores con demasiados parámetros y
 * hace el código de creación legible y seguro.
 */
public class Libro {

    private final int    id;
    private final String isbn;
    private final String titulo;
    private final String editorial;
    private final int    anioPublicacion;
    private final String edicion;       // opcional
    private final String descripcion;   // opcional
    private final String ubicacion;     // opcional (estante físico)

    private Libro(Builder b) {
        this.id              = b.id;
        this.isbn            = b.isbn;
        this.titulo          = b.titulo;
        this.editorial       = b.editorial;
        this.anioPublicacion = b.anioPublicacion;
        this.edicion         = b.edicion;
        this.descripcion     = b.descripcion;
        this.ubicacion       = b.ubicacion;
    }

    public int    id()              { return id; }
    public String isbn()            { return isbn; }
    public String titulo()          { return titulo; }
    public String editorial()       { return editorial; }
    public int    anioPublicacion() { return anioPublicacion; }
    public String edicion()         { return edicion; }
    public String descripcion()     { return descripcion; }
    public String ubicacion()       { return ubicacion; }

    @Override
    public String toString() {
        return String.format("Libro{id=%d, isbn='%s', titulo='%s', editorial='%s', anio=%d}",
            id, isbn, titulo, editorial, anioPublicacion);
    }

    // ── BUILDER ────────────────────────────────────────────────
    public static class Builder {
        private final String isbn;
        private final String titulo;
        private int    id              = 0;
        private String editorial       = "";
        private int    anioPublicacion = 0;
        private String edicion         = null;
        private String descripcion     = null;
        private String ubicacion       = null;

        public Builder(String isbn, String titulo) {
            if (isbn   == null || isbn.isBlank())   throw new IllegalArgumentException("ISBN es obligatorio");
            if (titulo == null || titulo.isBlank()) throw new IllegalArgumentException("Título es obligatorio");
            this.isbn   = isbn;
            this.titulo = titulo;
        }

        public Builder id(int id)                     { this.id              = id;          return this; }
        public Builder editorial(String editorial)     { this.editorial       = editorial;   return this; }
        public Builder anioPublicacion(int anio)       { this.anioPublicacion = anio;        return this; }
        public Builder edicion(String edicion)         { this.edicion         = edicion;     return this; }
        public Builder descripcion(String descripcion) { this.descripcion     = descripcion; return this; }
        public Builder ubicacion(String ubicacion)     { this.ubicacion       = ubicacion;   return this; }

        public Libro build() { return new Libro(this); }
    }
}
