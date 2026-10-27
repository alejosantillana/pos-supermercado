CREATE TABLE permiso (
                         id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                         codigo VARCHAR2(50) NOT NULL UNIQUE,
                         descripcion VARCHAR2(200)
);

CREATE TABLE rol (
                     id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                     nombre VARCHAR2(50) NOT NULL UNIQUE,
                     nivel NUMBER(2) NOT NULL
);

CREATE TABLE rol_permiso (
                             rol_id NUMBER NOT NULL REFERENCES rol(id),
                             permiso_id NUMBER NOT NULL REFERENCES permiso(id),
                             PRIMARY KEY (rol_id, permiso_id)
);

CREATE TABLE usuario (
                         id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                         username VARCHAR2(50) NOT NULL UNIQUE,
                         password_hash VARCHAR2(200) NOT NULL,
                         nombre_completo VARCHAR2(120),
                         activo NUMBER(1) DEFAULT 1 NOT NULL
);

CREATE TABLE usuario_rol (
                             usuario_id NUMBER NOT NULL REFERENCES usuario(id),
                             rol_id NUMBER NOT NULL REFERENCES rol(id),
                             PRIMARY KEY (usuario_id, rol_id)
);

CREATE TABLE usuario_permiso (
                                 usuario_id NUMBER NOT NULL REFERENCES usuario(id),
                                 permiso_id NUMBER NOT NULL REFERENCES permiso(id),
                                 PRIMARY KEY (usuario_id, permiso_id)
);

CREATE TABLE caja (
                      id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                      numero NUMBER(4) NOT NULL UNIQUE,
                      nombre VARCHAR2(60),
                      activa NUMBER(1) DEFAULT 1 NOT NULL
);

CREATE TABLE turno (
                       id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       caja_id NUMBER NOT NULL REFERENCES caja(id),
                       cajero_id NUMBER NOT NULL REFERENCES usuario(id),
                       apertura TIMESTAMP NOT NULL,
                       cierre TIMESTAMP,
                       fondo_inicial NUMBER(12,2) NOT NULL,
                       efectivo_declarado NUMBER(12,2),
                       efectivo_esperado NUMBER(12,2),
                       diferencia NUMBER(12,2),
                       estado VARCHAR2(10) DEFAULT 'ABIERTO' NOT NULL CHECK (estado IN ('ABIERTO', 'CERRADO'))
);

-- RN-01: una sola caja abierta por vez
CREATE UNIQUE INDEX ux_turno_caja_abierto
    ON turno (CASE WHEN estado = 'ABIERTO' THEN caja_id END);

-- Solución Ejercicio 1.A: un cajero solo puede tener un turno abierto a la vez
CREATE UNIQUE INDEX ux_turno_cajero_abierto
    ON turno (CASE WHEN estado = 'ABIERTO' THEN cajero_id END);

CREATE TABLE movimiento_caja (
                                 id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 turno_id NUMBER NOT NULL REFERENCES turno(id),
                                 tipo VARCHAR2(20) NOT NULL CHECK (tipo IN ('FONDO_INICIAL', 'RETIRO_BOVEDA', 'REVERSION_EFECTIVO')),
                                 monto NUMBER(12,2) NOT NULL CHECK (monto >= 0),
                                 autorizado_por NUMBER REFERENCES usuario(id),
                                 referencia VARCHAR2(40),
                                 fecha TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL
);

CREATE TABLE producto (
                          id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                          codigo_barras VARCHAR2(30) NOT NULL UNIQUE,
                          nombre VARCHAR2(120) NOT NULL,
                          precio NUMBER(12,2) NOT NULL,
                          porcentaje_impuesto NUMBER(5,2) DEFAULT 12 NOT NULL,
                          stock NUMBER(8) DEFAULT 0 NOT NULL CHECK (stock >= 0),
                          activo NUMBER(1) DEFAULT 1 NOT NULL
);

CREATE TABLE venta (
                       id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       turno_id NUMBER NOT NULL REFERENCES turno(id),
                       fecha TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
                       total NUMBER(12,2) NOT NULL,
                       estado VARCHAR2(25) DEFAULT 'COMPLETADA' NOT NULL CHECK (estado IN ('COMPLETADA', 'PARCIALMENTE_REVERSADA', 'ANULADA'))
);

CREATE TABLE venta_detalle (
                               id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                               venta_id NUMBER NOT NULL REFERENCES venta(id),
                               producto_id NUMBER NOT NULL REFERENCES producto(id),
                               cantidad NUMBER(6) NOT NULL CHECK (cantidad > 0),
                               cantidad_reversada NUMBER(6) DEFAULT 0 NOT NULL,
                               precio_unitario NUMBER(12,2) NOT NULL,
                               subtotal NUMBER(12,2) NOT NULL
);

CREATE TABLE pago (
                      id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                      venta_id NUMBER NOT NULL REFERENCES venta(id),
                      metodo VARCHAR2(10) NOT NULL CHECK (metodo IN ('EFECTIVO', 'TARJETA')),
                      monto NUMBER(12,2) NOT NULL,
                      recibido NUMBER(12,2) NOT NULL,
                      cambio NUMBER(12,2) DEFAULT 0 NOT NULL
);

CREATE TABLE reversion (
                           id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           detalle_id NUMBER NOT NULL REFERENCES venta_detalle(id),
                           cantidad NUMBER(6) NOT NULL,
                           monto NUMBER(12,2) NOT NULL,
                           motivo VARCHAR2(200) NOT NULL,
                           solicitado_por NUMBER NOT NULL REFERENCES usuario(id),
                           autorizado_por NUMBER NOT NULL REFERENCES usuario(id),
                           fecha TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL
);