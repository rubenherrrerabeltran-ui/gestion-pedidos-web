<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="co.sena.gestionpedidos.modelo.EstadoPedido" %>
<%@ taglib prefix="gp" uri="https://sena.edu.co/gestionpedidos/funciones" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="pedido" class="co.sena.gestionpedidos.modelo.Pedido" scope="request"/>
<% String tituloPagina = "Pedido #" + pedido.getIdPedido(); %>
<%@ include file="/WEB-INF/vistas/comun/encabezado.jspf" %>
<section class="encabezado-pagina">
    <div>
        <h1>Pedido #<jsp:getProperty name="pedido" property="idPedido"/> <ui:estado valor="${pedido.estado}"/></h1>
        <p class="ayuda">Cliente: <strong>${gp:esc(pedido.nombreCliente)}</strong> ·
            Fecha: <%= pedido.getFechaPedido().toString().replace('T', ' ') %></p>
    </div>
    <a class="boton boton-claro" href="${pageContext.request.contextPath}/pedidos">&larr; Volver a pedidos</a>
</section>
<table class="tabla">
    <thead><tr><th>Producto</th><th class="num">Cantidad</th><th class="num">Precio unitario</th><th class="num">Subtotal</th></tr></thead>
    <tbody>
    <% for (co.sena.gestionpedidos.modelo.DetallePedido detalle : pedido.getDetalles()) {
           pageContext.setAttribute("d", detalle); %>
    <tr>
        <td>${gp:esc(d.nombreProducto)}</td>
        <td class="num">${d.cantidad}</td>
        <td class="num">${gp:moneda(d.precioUnitario)}</td>
        <td class="num">${gp:moneda(d.calcularSubtotal())}</td>
    </tr>
    <% } %>
    </tbody>
    <tfoot><tr><th colspan="3" class="num">Total</th><th class="num">${gp:moneda(pedido.calcularTotal())}</th></tr></tfoot>
</table>
<% if (usuarioEncabezado.getRol() != Rol.CLIENTE) { %>
<section class="acciones-pedido">
    <form class="en-linea" method="post" action="${pageContext.request.contextPath}/pedidos">
        <input type="hidden" name="accion" value="estado">
        <input type="hidden" name="id" value="${pedido.idPedido}">
        <label for="nuevoEstado">Cambiar estado a</label>
        <select id="nuevoEstado" name="nuevoEstado">
            <% for (EstadoPedido estado : EstadoPedido.values()) {
                   if (pedido.getEstado().puedeCambiarA(estado)) { %>
            <option value="<%= estado %>"><%= estado %></option>
            <%     }
               } %>
        </select>
        <button type="submit" class="boton boton-principal" <%= pedido.getEstado() == EstadoPedido.ENVIADO
                || pedido.getEstado() == EstadoPedido.CANCELADO ? "disabled" : "" %>>Actualizar estado</button>
    </form>
    <form class="en-linea" method="post" action="${pageContext.request.contextPath}/pedidos"
          onsubmit="return confirm('¿Eliminar el pedido #${pedido.idPedido}? Se devolverá el stock.');">
        <input type="hidden" name="accion" value="eliminar">
        <input type="hidden" name="id" value="${pedido.idPedido}">
        <button type="submit" class="boton boton-peligro">Eliminar pedido</button>
    </form>
</section>
<% } %>
<%@ include file="/WEB-INF/vistas/comun/pie.jspf" %>
