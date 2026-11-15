package co.sena.gestionpedidos.web.listener;

import co.sena.gestionpedidos.config.ConexionBD;
import co.sena.gestionpedidos.dao.PedidoDao;
import co.sena.gestionpedidos.dao.ProductoDao;
import co.sena.gestionpedidos.dao.UsuarioDao;
import co.sena.gestionpedidos.dao.impl.PedidoDaoJdbc;
import co.sena.gestionpedidos.dao.impl.ProductoDaoJdbc;
import co.sena.gestionpedidos.dao.impl.UsuarioDaoJdbc;
import co.sena.gestionpedidos.servicio.PedidoServicio;
import co.sena.gestionpedidos.servicio.ProductoServicio;
import co.sena.gestionpedidos.servicio.UsuarioServicio;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Se ejecuta una vez al desplegar la aplicación: crea la conexión JDBC, los DAO y los
 * servicios, y los deja en el ámbito de aplicación ({@link ServletContext}) para que
 * todos los servlets los compartan.
 *
 * @author Rubén Darío Herrera Beltrán
 */
@WebListener
public class ContextoAplicacionListener implements ServletContextListener {

    /** Nombre del atributo de aplicación con el servicio de productos. */
    public static final String ATRIBUTO_PRODUCTO_SERVICIO = "productoServicio";
    /** Nombre del atributo de aplicación con el servicio de usuarios. */
    public static final String ATRIBUTO_USUARIO_SERVICIO = "usuarioServicio";
    /** Nombre del atributo de aplicación con el servicio de pedidos. */
    public static final String ATRIBUTO_PEDIDO_SERVICIO = "pedidoServicio";
    /** Nombre del atributo de aplicación con la URL de la base de datos (sin parámetros). */
    public static final String ATRIBUTO_URL_BD = "urlBaseDatos";

    @Override
    public void contextInitialized(ServletContextEvent evento) {
        ServletContext contexto = evento.getServletContext();
        ConexionBD conexionBD = ConexionBD.desdeConfiguracion();

        UsuarioDao usuarioDao = new UsuarioDaoJdbc(conexionBD);
        ProductoDao productoDao = new ProductoDaoJdbc(conexionBD);
        PedidoDao pedidoDao = new PedidoDaoJdbc(conexionBD);

        contexto.setAttribute(ATRIBUTO_PRODUCTO_SERVICIO, new ProductoServicio(productoDao));
        contexto.setAttribute(ATRIBUTO_USUARIO_SERVICIO, new UsuarioServicio(usuarioDao));
        contexto.setAttribute(ATRIBUTO_PEDIDO_SERVICIO, new PedidoServicio(pedidoDao, usuarioDao));
        contexto.setAttribute(ATRIBUTO_URL_BD, conexionBD.getUrlSinParametros());
        contexto.log("Gestión de Pedidos iniciada. Base de datos: " + conexionBD.getUrlSinParametros()
                + " | conexión " + (conexionBD.probarConexion() ? "correcta" : "NO disponible"));
    }
}
