-- ============================================================
-- SISTEMA DE BIBLIOTECA UNIVERSITARIA
-- Script SQL para PostgreSQL
-- ============================================================

-- Limpiar si ya existen
DROP TABLE IF EXISTS multas CASCADE;
DROP TABLE IF EXISTS detalle_prestamo CASCADE;
DROP TABLE IF EXISTS prestamos CASCADE;
DROP TABLE IF EXISTS ejemplares CASCADE;
DROP TABLE IF EXISTS libros_autores CASCADE;
DROP TABLE IF EXISTS libros_categorias CASCADE;
DROP TABLE IF EXISTS libros CASCADE;
DROP TABLE IF EXISTS autores CASCADE;
DROP TABLE IF EXISTS categorias CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;
DROP TABLE IF EXISTS carreras CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS configuracion_multas CASCADE;

-- ============================================================
-- TABLAS DE CONFIGURACIÓN
-- ============================================================

CREATE TABLE roles (
    id_rol       SERIAL PRIMARY KEY,
    nombre       VARCHAR(50) NOT NULL UNIQUE,  -- ESTUDIANTE, DOCENTE, ADMIN
    descripcion  VARCHAR(200),
    max_libros   INT NOT NULL DEFAULT 3,
    dias_prestamo INT NOT NULL DEFAULT 7
);

CREATE TABLE carreras (
    id_carrera  SERIAL PRIMARY KEY,
    nombre      VARCHAR(150) NOT NULL,
    facultad    VARCHAR(150) NOT NULL,
    activo      BOOLEAN DEFAULT TRUE
);

CREATE TABLE configuracion_multas (
    id_config       SERIAL PRIMARY KEY,
    monto_por_dia   NUMERIC(8,2) NOT NULL DEFAULT 1.00,
    moneda          VARCHAR(10) NOT NULL DEFAULT 'PEN',
    descripcion     VARCHAR(200),
    vigente         BOOLEAN DEFAULT TRUE,
    fecha_desde     DATE NOT NULL DEFAULT CURRENT_DATE
);

-- ============================================================
-- USUARIOS
-- ============================================================

CREATE TABLE usuarios (
    id_usuario      SERIAL PRIMARY KEY,
    codigo          VARCHAR(20) NOT NULL UNIQUE,   -- código universitario
    nombres         VARCHAR(100) NOT NULL,
    apellidos       VARCHAR(100) NOT NULL,
    email           VARCHAR(150) NOT NULL UNIQUE,
    telefono        VARCHAR(20),
    id_rol          INT NOT NULL REFERENCES roles(id_rol),
    id_carrera      INT REFERENCES carreras(id_carrera),
    activo          BOOLEAN DEFAULT TRUE,
    fecha_registro  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- CATÁLOGO DE LIBROS
-- ============================================================

CREATE TABLE categorias (
    id_categoria  SERIAL PRIMARY KEY,
    nombre        VARCHAR(100) NOT NULL UNIQUE,
    descripcion   VARCHAR(300)
);

CREATE TABLE autores (
    id_autor    SERIAL PRIMARY KEY,
    nombres     VARCHAR(100) NOT NULL,
    apellidos   VARCHAR(100) NOT NULL,
    nacionalidad VARCHAR(80)
);

CREATE TABLE libros (
    id_libro        SERIAL PRIMARY KEY,
    isbn            VARCHAR(20) NOT NULL UNIQUE,
    titulo          VARCHAR(300) NOT NULL,
    editorial       VARCHAR(150),
    anio_publicacion INT,
    edicion         VARCHAR(50),
    descripcion     TEXT,
    ubicacion       VARCHAR(100),   -- estante/sección física
    activo          BOOLEAN DEFAULT TRUE,
    fecha_registro  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE libros_autores (
    id_libro  INT NOT NULL REFERENCES libros(id_libro) ON DELETE CASCADE,
    id_autor  INT NOT NULL REFERENCES autores(id_autor) ON DELETE CASCADE,
    PRIMARY KEY (id_libro, id_autor)
);

CREATE TABLE libros_categorias (
    id_libro      INT NOT NULL REFERENCES libros(id_libro) ON DELETE CASCADE,
    id_categoria  INT NOT NULL REFERENCES categorias(id_categoria) ON DELETE CASCADE,
    PRIMARY KEY (id_libro, id_categoria)
);

-- Ejemplares físicos de cada libro
CREATE TABLE ejemplares (
    id_ejemplar     SERIAL PRIMARY KEY,
    id_libro        INT NOT NULL REFERENCES libros(id_libro),
    codigo_barras   VARCHAR(50) NOT NULL UNIQUE,
    estado          VARCHAR(30) NOT NULL DEFAULT 'DISPONIBLE'
                    CHECK (estado IN ('DISPONIBLE','PRESTADO','RESERVADO','DETERIORADO','BAJA')),
    fecha_adquisicion DATE,
    observaciones   TEXT
);

-- ============================================================
-- PRÉSTAMOS
-- ============================================================

CREATE TABLE prestamos (
    id_prestamo     SERIAL PRIMARY KEY,
    id_usuario      INT NOT NULL REFERENCES usuarios(id_usuario),
    fecha_prestamo  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_devolucion_esperada DATE NOT NULL,
    fecha_devolucion_real     DATE,
    estado          VARCHAR(20) NOT NULL DEFAULT 'ACTIVO'
                    CHECK (estado IN ('ACTIVO','DEVUELTO','VENCIDO','RENOVADO')),
    observaciones   TEXT,
    registrado_por  INT REFERENCES usuarios(id_usuario)  -- bibliotecario
);

CREATE TABLE detalle_prestamo (
    id_detalle      SERIAL PRIMARY KEY,
    id_prestamo     INT NOT NULL REFERENCES prestamos(id_prestamo) ON DELETE CASCADE,
    id_ejemplar     INT NOT NULL REFERENCES ejemplares(id_ejemplar),
    estado_devolucion VARCHAR(30) DEFAULT 'PENDIENTE'
                    CHECK (estado_devolucion IN ('PENDIENTE','DEVUELTO_BIEN','DEVUELTO_DANADO'))
);

-- ============================================================
-- MULTAS
-- ============================================================

CREATE TABLE multas (
    id_multa        SERIAL PRIMARY KEY,
    id_prestamo     INT NOT NULL REFERENCES prestamos(id_prestamo),
    id_usuario      INT NOT NULL REFERENCES usuarios(id_usuario),
    dias_retraso    INT NOT NULL DEFAULT 0,
    monto_total     NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    pagado          BOOLEAN DEFAULT FALSE,
    fecha_generada  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_pago      TIMESTAMP,
    observaciones   TEXT
);

-- ============================================================
-- ÍNDICES
-- ============================================================

CREATE INDEX idx_usuarios_codigo    ON usuarios(codigo);
CREATE INDEX idx_usuarios_email     ON usuarios(email);
CREATE INDEX idx_libros_isbn        ON libros(isbn);
CREATE INDEX idx_libros_titulo      ON libros(titulo);
CREATE INDEX idx_ejemplares_estado  ON ejemplares(estado);
CREATE INDEX idx_prestamos_usuario  ON prestamos(id_usuario);
CREATE INDEX idx_prestamos_estado   ON prestamos(estado);
CREATE INDEX idx_multas_usuario     ON multas(id_usuario);
CREATE INDEX idx_multas_pagado      ON multas(pagado);

-- ============================================================
-- DATOS INICIALES
-- ============================================================

INSERT INTO roles (nombre, descripcion, max_libros, dias_prestamo) VALUES
  ('ESTUDIANTE', 'Alumno de pregrado o postgrado', 3, 7),
  ('DOCENTE',    'Profesor universitario',         5, 30),
  ('ADMIN',      'Administrador del sistema',       0, 0),
  ('BIBLIOTECARIO', 'Personal de biblioteca',       0, 0);

INSERT INTO configuracion_multas (monto_por_dia, moneda, descripcion) VALUES
  (1.00, 'PEN', 'Multa estándar por día de retraso');

INSERT INTO categorias (nombre) VALUES
  ('Ciencias'), ('Ingeniería'), ('Humanidades'), ('Medicina'),
  ('Derecho'), ('Economía'), ('Arte'), ('Literatura');

-- ============================================================
-- VISTA: Libros con disponibilidad
-- ============================================================

CREATE OR REPLACE VIEW v_disponibilidad_libros AS
SELECT
    l.id_libro,
    l.isbn,
    l.titulo,
    l.editorial,
    COUNT(e.id_ejemplar)                                           AS total_ejemplares,
    COUNT(e.id_ejemplar) FILTER (WHERE e.estado = 'DISPONIBLE')   AS disponibles,
    COUNT(e.id_ejemplar) FILTER (WHERE e.estado = 'PRESTADO')     AS prestados
FROM libros l
LEFT JOIN ejemplares e ON l.id_libro = e.id_libro
WHERE l.activo = TRUE
GROUP BY l.id_libro, l.isbn, l.titulo, l.editorial;

-- ============================================================
-- VISTA: Préstamos activos con usuario y libro
-- ============================================================

CREATE OR REPLACE VIEW v_prestamos_activos AS
SELECT
    p.id_prestamo,
    u.codigo       AS codigo_usuario,
    u.nombres || ' ' || u.apellidos AS nombre_usuario,
    r.nombre       AS rol_usuario,
    l.titulo       AS titulo_libro,
    e.codigo_barras,
    p.fecha_prestamo,
    p.fecha_devolucion_esperada,
    CURRENT_DATE - p.fecha_devolucion_esperada AS dias_retraso
FROM prestamos p
JOIN usuarios u         ON p.id_usuario = u.id_usuario
JOIN roles r            ON u.id_rol = r.id_rol
JOIN detalle_prestamo d ON p.id_prestamo = d.id_prestamo
JOIN ejemplares e       ON d.id_ejemplar = e.id_ejemplar
JOIN libros l           ON e.id_libro = l.id_libro
WHERE p.estado = 'ACTIVO';

-- ============================================================
-- FUNCIÓN: Calcular multa de un préstamo
-- ============================================================

CREATE OR REPLACE FUNCTION calcular_multa(p_id_prestamo INT)
RETURNS NUMERIC AS $$
DECLARE
    v_fecha_esperada  DATE;
    v_dias_retraso    INT;
    v_monto_dia       NUMERIC;
BEGIN
    SELECT fecha_devolucion_esperada INTO v_fecha_esperada
    FROM prestamos WHERE id_prestamo = p_id_prestamo;

    v_dias_retraso := GREATEST(0, CURRENT_DATE - v_fecha_esperada);

    SELECT monto_por_dia INTO v_monto_dia
    FROM configuracion_multas WHERE vigente = TRUE
    ORDER BY fecha_desde DESC LIMIT 1;

    RETURN v_dias_retraso * v_monto_dia;
END;
$$ LANGUAGE plpgsql;
