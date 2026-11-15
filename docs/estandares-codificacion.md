# Estándares de codificación — Módulo web (Servlets y JSP)

Basado en las *Code Conventions for the Java Programming Language* (Oracle) y en la *Google Java Style Guide*,
adaptados al español para el proyecto Sistema de Gestión de Pedidos.

## 1. Nombramiento

| Elemento | Convención | Ejemplos del proyecto |
|---|---|---|
| Paquetes | minúsculas, sin guiones ni guiones bajos, dominio invertido | `co.sena.gestionpedidos.dao.impl`, `co.sena.gestionpedidos.servicio` |
| Clases | PascalCase, sustantivo | `ConexionBD`, `ProductoDaoJdbc`, `PedidoServicio`, `MenuConsola` |
| Interfaces | PascalCase, nombre del contrato | `CrudDao`, `ProductoDao`, `PedidoDao` |
| Implementaciones | interfaz + tecnología | `ProductoDaoJdbc implements ProductoDao` |
| Enumeraciones | PascalCase; constantes en MAYÚSCULAS | `EstadoPedido.PENDIENTE`, `Rol.ADMINISTRADOR` |
| Métodos | camelCase, inicia con verbo | `insertar`, `buscarPorId`, `listarTodos`, `actualizarEstado`, `registrarPedido` |
| Variables y atributos | camelCase, descriptivos, sin abreviaturas | `precioUnitario`, `idProducto`, `cantidadPorProducto`, `conexionBD` |
| Constantes | MAYÚSCULAS_CON_GUION_BAJO, `static final` | `SQL_INSERTAR`, `ITERACIONES`, `LONGITUD_MAXIMA_NOMBRE` |
| Servlets, filtros y listeners | sufijo del tipo de componente | `ProductoServlet`, `AutenticacionFiltro`, `ContextoAplicacionListener` |
| URL de los servlets | minúscula, sustantivo en plural | `/productos`, `/usuarios`, `/pedidos`, `/login` |
| Parámetros HTTP y `name` de los campos | camelCase, igual al atributo del modelo | `nombreProducto`, `idCliente`, `nuevoEstado` |
| Páginas JSP | minúscula, en carpeta del módulo dentro de `WEB-INF/vistas` | `productos/lista.jsp`, `pedidos/detalle.jsp` |
| Fragmentos y etiquetas | `.jspf` para fragmentos; `.tag` para archivos de etiquetas | `comun/encabezado.jspf`, `tags/estado.tag` |
| Pruebas | clase `NombreClaseTest`; método describe el comportamiento | `ProductoDaoJdbcTest.eliminarBorraElRegistro` |
| Tablas y columnas SQL | snake_case en minúscula | `detalle_pedido`, `precio_unitario` |

## 2. Formato

- Codificación UTF-8, sangría de 4 espacios (sin tabuladores), líneas de máximo 120 caracteres.
- Llaves en estilo K&R (la llave de apertura en la misma línea).
- Un archivo por clase pública; importaciones explícitas (sin `*`).
- Orden de miembros: constantes, atributos, constructores, métodos públicos, métodos privados.

## 3. Documentación

- Javadoc en toda clase pública y en los métodos públicos (`@param`, `@return`, `@throws`).
- Comentarios en línea solo para explicar el *porqué* de una decisión.

## 4. Acceso a datos (JDBC)

- Solo `PreparedStatement` con parámetros `?`; nunca concatenar datos del usuario en el SQL.
- Recursos (`Connection`, `PreparedStatement`, `ResultSet`) en *try-with-resources*.
- Operaciones sobre varias tablas en transacción (`setAutoCommit(false)`, `commit`, `rollback`).
- La `SQLException` se envuelve en `DatosException` con un mensaje comprensible.
- Las credenciales se leen de `db.properties` o de variables de entorno, nunca del código.
- Las contraseñas se almacenan como hash PBKDF2 con sal aleatoria.

## 5. Web (Servlets y JSP)

- Patrón MVC: el servlet (controlador) procesa la petición y llama al servicio; la JSP (vista) solo muestra datos.
- Las JSP están en `WEB-INF/vistas` para que no se abran sin pasar por el servlet.
- GET solo para consultas; POST para crear, actualizar, eliminar o enviar contraseñas, seguido de redirección
  (Post/Redirect/Get) para no reenviar el formulario al recargar.
- Todo texto ingresado por usuarios se muestra escapado con la función `gp:esc` (prevención de XSS).
- Codificación UTF-8 en filtros, JSP (`pageEncoding`) y `web.xml`.

## 6. Control de versiones

- Rama principal `main`; un commit por unidad lógica de trabajo.
- Formato Conventional Commits: `tipo(alcance): descripción en imperativo`.
  Tipos usados: `feat`, `fix`, `test`, `docs`, `chore`, `refactor`.
- `.gitignore` excluye `target/`, archivos del IDE y credenciales locales.
