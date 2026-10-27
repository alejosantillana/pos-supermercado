-- 1. Permisos del mapa de pantallas + reversión
INSERT INTO permiso (codigo, descripcion) VALUES ('TURNO_ABRIR', 'Apertura de turno y fondo inicial');
INSERT INTO permiso (codigo, descripcion) VALUES ('VENTA_COBRAR', 'Punto de venta y cobro');
INSERT INTO permiso (codigo, descripcion) VALUES ('CORTE_INTERMEDIO', 'Retiros parciales a bóveda');
INSERT INTO permiso (codigo, descripcion) VALUES ('REVERSION_SOLICITAR', 'Solicitar anulación o reversión de producto');
INSERT INTO permiso (codigo, descripcion) VALUES ('REVERSION_AUTORIZAR', 'Autorizar reversión de producto como rol superior');
INSERT INTO permiso (codigo, descripcion) VALUES ('TURNO_CERRAR', 'Cierre final de turno y arqueo');
INSERT INTO permiso (codigo, descripcion) VALUES ('USUARIO_ADMIN', 'Administración y alta de usuarios');
INSERT INTO permiso (codigo, descripcion) VALUES ('ROL_ADMIN', 'Gestión de roles y asignación de permisos');
INSERT INTO permiso (codigo, descripcion) VALUES ('CATALOGO_ADMIN', 'Mantenimiento de productos y cajas');

-- 2. Roles con sus niveles jerárquicos
INSERT INTO rol (nombre, nivel) VALUES ('CAJERO', 1);
INSERT INTO rol (nombre, nivel) VALUES ('SUPERVISOR', 2);
INSERT INTO rol (nombre, nivel) VALUES ('GERENTE', 3);
INSERT INTO rol (nombre, nivel) VALUES ('ADMIN', 4);

-- 3. Asignación de permisos al rol ADMIN (todos)
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r, permiso p WHERE r.nombre = 'ADMIN';

-- Asignación básica para CAJERO
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r, permiso p
WHERE r.nombre = 'CAJERO' AND p.codigo IN ('TURNO_ABRIR', 'VENTA_COBRAR', 'CORTE_INTERMEDIO', 'REVERSION_SOLICITAR', 'TURNO_CERRAR');

-- Asignación para SUPERVISOR
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r, permiso p
WHERE r.nombre = 'SUPERVISOR' AND p.codigo IN ('REVERSION_AUTORIZAR', 'CORTE_INTERMEDIO');

-- 4. Usuario administrador inicial con el hash calculado
INSERT INTO usuario (username, password_hash, nombre_completo, activo)
VALUES ('admin', '120000:hvPoaR89Z9HsPcaYeihQaw==:ZUcDAfxkQf8/+UbApIFwygn2eZ+mZD7vAhkHWJbIJok=', 'Administrador del Sistema', 1);

-- Asignar rol ADMIN al usuario admin
INSERT INTO usuario_rol (usuario_id, rol_id)
SELECT u.id, r.id FROM usuario u, rol r WHERE u.username = 'admin' AND r.nombre = 'ADMIN';

-- 5. Cajas registradoras iniciales
INSERT INTO caja (numero, nombre, activa) VALUES (1, 'Caja Principal 01', 1);
INSERT INTO caja (numero, nombre, activa) VALUES (2, 'Caja Rápida 02', 1);
INSERT INTO caja (numero, nombre, activa) VALUES (3, 'Caja Abarrotes 03', 1);

-- 6. Productos de prueba con IVA del 12%
INSERT INTO producto (codigo_barras, nombre, precio, porcentaje_impuesto, stock, activo)
VALUES ('7401001001', 'Leche Entera 1L', 15.50, 12.00, 50, 1);

INSERT INTO producto (codigo_barras, nombre, precio, porcentaje_impuesto, stock, activo)
VALUES ('7401001002', 'Pan Blanco Bimbo', 22.00, 12.00, 30, 1);

INSERT INTO producto (codigo_barras, nombre, precio, porcentaje_impuesto, stock, activo)
VALUES ('7401001003', 'Gaseosa Cola 2.5L', 18.00, 12.00, 40, 1);

INSERT INTO producto (codigo_barras, nombre, precio, porcentaje_impuesto, stock, activo)
VALUES ('7401001004', 'Café Tostado Molido 400g', 35.00, 12.00, 25, 1);

INSERT INTO producto (codigo_barras, nombre, precio, porcentaje_impuesto, stock, activo)
VALUES ('7401001005', 'Arroz Blanco 1lb', 6.50, 12.00, 100, 1);

COMMIT;