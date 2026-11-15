# Sistema de Gestión de Pedidos — Módulo web (Servlets y JSP)

Módulo web del proyecto formativo **Sistema de Gestión de Pedidos** (SENA, Análisis y Desarrollo de Software, ficha 3186691).
Los **formularios HTML** se procesan con **servlets** (métodos **GET** y **POST**), las páginas se generan con **JSP**
(directivas, scriptlets, expresiones, declaraciones, acciones estándar, EL, archivos de etiquetas y funciones EL) y los
datos se guardan en **MySQL** por medio de **JDBC**, reutilizando las capas del módulo `gestion-pedidos-jdbc` (evidencia AA2-EV01).

> Evidencia **GA7-220501096-AA2-EV02 — Módulos de software codificados y probados**
> Autor: **Rubén Darío Herrera Beltrán**

## Tecnologías

| Componente | Versión |
|---|---|
| Java (JDK) | 21 o superior |
| Jakarta Servlet / Jakarta Pages (JSP) | 6.1 / 4.0 (Jakarta EE 11) |
| Apache Tomcat | 11.0 |
| MySQL Server / Connector/J | 8.0.16 o superior / 9.7.0 |
| Maven | 3.9 o superior |
| Pruebas | JUnit 6.1.3, H2 2.4.240 (modo MySQL), Tomcat embebido 11.0.26 |

## Estructura

```
gestion-pedidos-web/
├── database/                         # 01_esquema.sql y 02_datos_prueba.sql
├── src/main/java/co/sena/gestionpedidos/
│   ├── config, modelo, dao, dao/impl, servicio, util, excepcion   # capas del módulo JDBC (AA2-EV01)
│   └── web/
│       ├── servlet/   # LoginServlet, InicioServlet, ProductoServlet, UsuarioServlet, PedidoServlet, ServletBase
│       ├── filtro/    # CodificacionFiltro (UTF-8), AutenticacionFiltro (sesión)
│       ├── listener/  # ContextoAplicacionListener (crea conexión y servicios)
│       └── util/      # Html (escape XSS y formato de moneda)
├── src/main/webapp/
│   ├── index.jsp, css/estilos.css
│   └── WEB-INF/
│       ├── web.xml
│       ├── vistas/    # JSP: login, inicio, error, productos/*, usuarios/*, pedidos/*, comun/*
│       ├── tags/      # archivos de etiquetas: estado.tag, tarjeta.tag
│       └── tlds/      # gestionpedidos.tld (funciones EL gp:esc y gp:moneda)
└── src/test/java/...  # 30 pruebas: DAO, servicios y servlets con peticiones HTTP reales
```

## Formularios, servlets y métodos HTTP

| Formulario / acción | Método | Servlet | Parámetros |
|---|---|---|---|
| Iniciar sesión | POST | `LoginServlet` (`/login`) | `correo`, `clave` |
| Buscar productos | GET | `ProductoServlet` (`/productos`) | `texto` |
| Abrir formulario nuevo / editar | GET | `ProductoServlet`, `UsuarioServlet` | `accion=nuevo` · `accion=editar&id=` |
| Guardar producto / usuario | POST | `ProductoServlet`, `UsuarioServlet` | `accion=guardar`, `id`, campos del formulario |
| Eliminar producto / usuario / pedido | POST | servlet del módulo | `accion=eliminar`, `id` |
| Filtrar pedidos por estado | GET | `PedidoServlet` (`/pedidos`) | `estado` |
| Registrar pedido | POST | `PedidoServlet` | `accion=registrar`, `idCliente`, `producto[]`, `cantidad[]` |
| Cambiar estado del pedido | POST | `PedidoServlet` | `accion=estado`, `id`, `nuevoEstado` |

GET se usa para consultar (los parámetros quedan en la URL y se pueden guardar como enlace); POST para todo lo que
modifica datos o envía contraseñas. Después de cada POST el servlet redirige (patrón Post/Redirect/Get).

## Puesta en marcha

1. Crear la base de datos: `mysql -u root -p < database/01_esquema.sql` y `mysql -u root -p < database/02_datos_prueba.sql`.
2. Configurar la conexión en `src/main/resources/db.properties` o con las variables `DB_URL`, `DB_USUARIO`, `DB_CLAVE`.
3. `mvn clean package` genera `target/gestion-pedidos.war`; copiarlo a la carpeta `webapps` de Tomcat 11.
4. Abrir `http://localhost:8080/gestion-pedidos` e ingresar con un usuario de prueba (ver `02_datos_prueba.sql`).
5. `mvn test` ejecuta las pruebas (no requiere MySQL ni Tomcat instalados).

## Estándares de codificación

Ver [`docs/estandares-codificacion.md`](docs/estandares-codificacion.md).
