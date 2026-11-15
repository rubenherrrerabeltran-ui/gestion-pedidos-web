<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="gp" uri="https://sena.edu.co/gestionpedidos/funciones" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<% String tituloPagina = "Inicio"; %>
<%@ include file="/WEB-INF/vistas/comun/encabezado.jspf" %>
<jsp:useBean id="fechaActual" class="java.util.Date" scope="page"/>
<section class="encabezado-pagina">
    <div>
        <h1>Hola, ${gp:esc(sessionScope.usuarioSesion.nombre)}</h1>
        <p class="ayuda">Rol: <strong>${sessionScope.usuarioSesion.rol}</strong> ·
            Fecha del servidor: <%= new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(fechaActual) %></p>
    </div>
</section>
<section class="tarjetas">
    <ui:tarjeta titulo="Productos" valor="${totalProductos}" enlace="/productos">
        ${productosStockBajo} con stock bajo (menos de 10)
    </ui:tarjeta>
    <ui:tarjeta titulo="Pedidos" valor="${totalPedidos}" enlace="/pedidos">
        ${pedidosPendientes} pendiente(s) por pagar
    </ui:tarjeta>
    <ui:tarjeta titulo="Ventas (sin cancelados)" valor="${gp:moneda(totalVendido)}" enlace="/pedidos?estado=PAGADO">
        Suma de los pedidos registrados
    </ui:tarjeta>
    <ui:tarjeta titulo="Usuarios" valor="${totalUsuarios}" enlace="${sessionScope.usuarioSesion.rol == 'ADMINISTRADOR' ? '/usuarios' : null}">
        Administradores, vendedores y clientes
    </ui:tarjeta>
</section>
<section class="accesos">
    <a class="boton boton-principal" href="${pageContext.request.contextPath}/pedidos?accion=nuevo">+ Nuevo pedido</a>
    <a class="boton" href="${pageContext.request.contextPath}/productos?accion=nuevo">+ Nuevo producto</a>
    <a class="boton" href="${pageContext.request.contextPath}/productos?texto=caf%C3%A9">Buscar «café» (GET)</a>
</section>
<%@ include file="/WEB-INF/vistas/comun/pie.jspf" %>
