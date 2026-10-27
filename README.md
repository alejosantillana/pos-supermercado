# Sistema de Punto de Venta (POS) - Supermercado Central

Sistema integral de punto de venta (POS) y administración desarrollado en **Java SE (Swing + JDBC)** conectado a base de datos relacional **Oracle Database Express Edition (XE)**. Cumple con los lineamientos de arquitectura en capas, control de acceso basado en roles (RBAC) y reglas de negocio para operaciones de caja y tesorería.

---

## 1. Arquitectura del Sistema

El software implementa una arquitectura desacoplada en tres capas principales:

```text
com.supermercado.pos/
├── dominio/        # Entidades del modelo de negocio (Turno, Venta, Producto, Usuario, Rol, etc.)
├── dao/            # Capa de persistencia JDBC nativa (Transacciones manuales, PreparedStatement)
├── servicio/       # Lógica de negocio y validación de reglas (AuthService, TurnoService, VentaService, ReversionService)
├── ui/             # Interfaz gráfica de usuario en Java Swing (Ventanas, Diálogos modales y Tablas)
├── util/           # Utilidades compartidas (Conexión JDBC, PBKDF2 Hashing, Gestión de Sesión)
├── excepcion/      # Jerarquía de excepciones de negocio (ReglaNegocioException, AutorizacionException)
├── Main.java       # Punto de entrada de la aplicación interactiva
└── Pruebas.java    # Suite de pruebas automatizadas sin librerías externas (Fase 10)
```

---

## 2. Requisitos Previos

* **Java Development Kit (JDK):** Versión 21 o superior.
* **Base de Datos:** Oracle Database XE (18c, 21c o compatible) ejecutándose localmente en el puerto `1521` con SID/Servicio `XE`.
* **Driver JDBC:** Oracle JDBC Driver (`ojdbc11.jar` o `ojdbc8.jar`) ubicado en la carpeta `lib/`.

---

## 3. Configuración de Base de Datos

1. Los scripts DDL y DML se encuentran en el directorio `sql/`:
   * `sql/01_esquema.sql`: Creación de tablas relacionales, secuencias/identities y llaves foráneas.
   * `sql/02_datos_iniciales.sql`: Carga de catálogo de productos base, cajas y roles del sistema.

2. Configurar las credenciales en el archivo `config.properties` ubicado en la raíz del proyecto:
   ```properties
   db.url=jdbc:oracle:thin:@localhost:1521:XE
   db.user=system
   db.password=ADMIN10
   ```

---

## 4. Credenciales de Usuarios de Prueba

El sistema inicializa automáticamente los usuarios de prueba con contraseñas seguras aplicando PBKDF2 con sal individual:

| Usuario | Contraseña | Rol Asignado | Nivel Jerárquico | Permisos Principales |
| :--- | :--- | :--- | :---: | :--- |
| `admin` | `Admin123!` | `ADMIN` | 4 | Administración total (Usuarios, Roles, Permisos, Productos y Cajas) |
| `ana` | `Ana123!` | `SUPERVISOR` | 2 | Autorización de reversiones (`REVERSION_AUTORIZAR`), retiros a bóveda |
| `carlos` | `Carlos123!` | `CAJERO` | 1 | Apertura/cierre de turno, escaneo de productos, cobros y corte a bóveda |

---

## 5. Compilación y Ejecución

### Opción A: Desde el IDE (IntelliJ IDEA)
1. Abrir la carpeta raíz del proyecto en IntelliJ IDEA.
2. Asegurarse de que `lib/ojdbc11.jar` esté agregado como librería del módulo (**File -> Project Structure -> Libraries**).
3. Para iniciar la aplicación gráfica: ejecutar la clase `com.supermercado.pos.Main`.
4. Para ejecutar las pruebas de validación de negocio: ejecutar la clase `com.supermercado.pos.Pruebas`.

### Opción B: Desde la Consola (Windows CMD / PowerShell)
```bash
# 1. Compilar el proyecto completo hacia la carpeta out
javac -cp "lib/ojdbc11.jar;src" -d out src/com/supermercado/pos/dominio/*.java src/com/supermercado/pos/dao/*.java src/com/supermercado/pos/servicio/*.java src/com/supermercado/pos/util/*.java src/com/supermercado/pos/excepcion/*.java src/com/supermercado/pos/ui/*.java src/com/supermercado/pos/*.java

# 2. Ejecutar la aplicación principal
java -cp "out;lib/ojdbc11.jar;." com.supermercado.pos.Main

# 3. Ejecutar la suite de pruebas automatizadas
java -cp "out;lib/ojdbc11.jar;." com.supermercado.pos.Pruebas
```

---

## 6. Cobertura de Reglas de Negocio (RN)

* **RN-01 (Caja Exclusiva):** No se permite que dos cajeros operen simultáneamente sobre la misma caja en un turno activo.
* **RN-02 (Cajero de Turno Único):** Un cajero solo puede tener un único turno abierto a la vez.
* **RN-03 (Fondo Inicial Obligatorio):** Apertura de turno exige un fondo inicial estrictamente mayor a Q 0.00.
* **RN-04 (Pagos Múltiples y Mixtos):** Admite cobros combinando efectivo y tarjeta. El vuelto/cambio solo se calcula y entrega sobre el excedente en efectivo.
* **RN-05 (Retiros a Bóveda / Cortes Intermedios):** Requiere autorización explícita de un supervisor mediante contraseña y motivo; valida que no se exceda el saldo en gaveta ni se viole el fondo mínimo operativo.
* **RN-06 y RN-07 (Reversiones y Jerarquía de Autorización):** Un cajero no puede autorizarse a sí mismo. Requiere credenciales de un usuario con permiso `REVERSION_AUTORIZAR` y nivel jerárquico estrictamente mayor.
* **RN-08 (Arqueo Ciego en Cierre):** El cajero ingresa físicamente el efectivo contado sin conocer el monto teórico esperado por el sistema. El cálculo de diferencias (faltante o sobrante) se efectúa tras la confirmación contable.
* **RN-09 (Anti-escalamiento de Privilegios):** Ningún usuario puede modificar sus propios roles ni otorgar permisos o roles de nivel igual o superior al suyo.

---

## 7. Catálogo de Pantallas Implementadas (12 Módulos)

1. **Pantalla #1 (Login):** Autenticación con usuario y contraseña oculta.
2. **Pantalla #2 (Menú Principal):** Navegación condicionada por RBAC; botones habilitados dinámicamente según permisos del usuario.
3. **Pantalla #3 (Apertura de Turno):** Selección de cajas disponibles y registro de fondo inicial.
4. **Pantalla #4 (Punto de Venta / Carrito):** Ingreso por código de barras, cálculo en tiempo real de subtotales, IVA y total.
5. **Pantalla #5 (Diálogo de Cobro):** Registro de pagos en efectivo, tarjeta o mixtos con cálculo dinámico de saldo pendiente.
6. **Pantalla #6 (Ticket de Venta):** Desglose formal monoespaciado con fecha, hora, turno, caja, ítems y desglose de formas de pago.
7. **Pantalla #7 (Corte Intermedio a Bóveda):** Retiro parcial de efectivo con autorización de supervisor.
8. **Pantalla #8 (Consulta de Ventas y Reversiones):** Búsqueda de tickets, desglose de líneas y reversión parcial o total.
9. **Pantalla #9 (Autorización de Supervisor):** Modal de escalamiento de privilegios por credenciales puntuales.
10. **Pantalla #10 (Cierre de Turno y Arqueo Ciego):** Captura ciega de efectivo y reporte de balance (cuadre/descuadre).
11. **Pantalla #11 (Catálogo de Productos y Stock):** Auditoría de inventario en tiempo real, ajuste de existencias y alta de productos.
12. **Pantalla #12 (Administración de Usuarios y Matriz de Roles/Permisos):** Gestión de cuentas activas/inactivas y matriz interactiva de permisos por rol.

---

## 8. Resultados de la Suite de Pruebas (`Pruebas.java`)

```text
==========================================================
      EJECUTOR DE PRUEBAS AUTOMATIZADAS - SISTEMA POS     
==========================================================
  [OK] 1. Abrir turno con fondo 0 -> Capturó: ReglaNegocioException (El fondo inicial debe ser mayor a cero)
  [OK] 2. Abrir turno con caja ocupada -> Capturó: ReglaNegocioException (La caja seleccionada ya está abierta)
  [OK] 3. Cobro con pago insuficiente -> Capturó: ReglaNegocioException (Pago insuficiente...)
  [OK] 4. Pago mixto efectivo + tarjeta (Cambio solo sobre el efectivo)
  [OK] 5. Reversión autorizada por un cajero -> Capturó: AutorizacionException (No tiene rango de supervisor)
  [OK] 6. Reversión autorizada por el mismo usuario -> Capturó: AutorizacionException (No se puede autorizar a sí mismo)
  [OK] 7a. Reversión parcial inicial exitosa
  [OK] 7b. Reversión parcial que supera la cantidad vendida -> Capturó: ReglaNegocioException
  [OK] 8. Cierre con faltante de Q20 (diferencia == -20.00)
==========================================================
RESULTADO FINAL: 9/9 pruebas superadas (Fallos: 0)
==========================================================
```