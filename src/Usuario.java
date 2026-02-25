/**
 * Usuario: tiene campos obligatorios (codigo, nombres, apellidos, email)
 * y opcionales (telefono, rol, carrera). El Builder permite registrar tanto
 * estudiantes básicos como docentes con datos completos sin sobrecargar constructores.
 */
public class Usuario {

    private final int    id;
    private final String codigo;
    private final String nombres;
    private final String apellidos;
    private final String email;
    private final String telefono;  // opcional
    private final String rol;       // default: ESTUDIANTE
    private final String carrera;   // opcional

    private Usuario(Builder b) {
        this.id        = b.id;
        this.codigo    = b.codigo;
        this.nombres   = b.nombres;
        this.apellidos = b.apellidos;
        this.email     = b.email;
        this.telefono  = b.telefono;
        this.rol       = b.rol;
        this.carrera   = b.carrera;
    }

    public int    id()        { return id; }
    public String codigo()    { return codigo; }
    public String nombres()   { return nombres; }
    public String apellidos() { return apellidos; }
    public String email()     { return email; }
    public String telefono()  { return telefono; }
    public String rol()       { return rol; }
    public String carrera()   { return carrera; }

    @Override
    public String toString() {
        return String.format("Usuario{id=%d, codigo='%s', nombre='%s %s', rol='%s'}",
            id, codigo, nombres, apellidos, rol);
    }

    // ── BUILDER ────────────────────────────────────────────────
    public static class Builder {
        private final String codigo;
        private final String nombres;
        private final String apellidos;
        private final String email;
        private int    id       = 0;
        private String telefono = null;
        private String rol      = "ESTUDIANTE";
        private String carrera  = null;

        public Builder(String codigo, String nombres, String apellidos, String email) {
            if (codigo    == null || codigo.isBlank())    throw new IllegalArgumentException("Código obligatorio");
            if (nombres   == null || nombres.isBlank())   throw new IllegalArgumentException("Nombres obligatorio");
            if (apellidos == null || apellidos.isBlank()) throw new IllegalArgumentException("Apellidos obligatorio");
            if (email     == null || email.isBlank())     throw new IllegalArgumentException("Email obligatorio");
            this.codigo = codigo; this.nombres = nombres;
            this.apellidos = apellidos; this.email = email;
        }

        public Builder id(int id)           { this.id       = id;  return this; }
        public Builder telefono(String tel) { this.telefono = tel; return this; }
        public Builder rol(String rol)      { this.rol      = rol; return this; }
        public Builder carrera(String car)  { this.carrera  = car; return this; }

        public Usuario build() { return new Usuario(this); }
    }
}
