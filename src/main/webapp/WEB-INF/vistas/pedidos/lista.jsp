<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, co.sena.gestionpedidos.modelo.Pedido, co.sena.gestionpedidos.modelo.EstadoPedido" %>
<%@ taglib prefix="gp" uri="https://sena.edu.co/gestionpedidos/funciones" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<% String tituloPagina = "Pedidos"; %>
<%@ include file="/WEB-INF/vistas/comun/encabezado.jspf" %>
<%
    @SuppressWarnings("unchecked")
    List<Pedido> pedidos = (List<Pedido>) request.getAttribute("pedidos");
    String estadoElegido = request.getParameter("estado") == null ? "" : request.getParameter("estado");
%>
<section class="encabezado-pagina">
    <div>
        <h1>Pedidos</h1>
        <p class="ayuda">El filtro por estado usa <strong>GET</strong> (<code>?estado=...</code>).</p>
    </div>
    <a class="boton boton-principal" href="${pageContext.request.contextPath}/pedidos?accion=nuevo">+ Nuevo pedido</a>
</section>
<form class="buscador" method="get" action="${pageContext.request.contextPath}/pedidos">
    <select name="estado">
        <option value="">Todos los estados</option>
        <% for (EstadoPedido estado : EstadoPedido.values()) { %>
        <option value="<%= estado %>" <%= estado.name().equals(estadoElegido) ? "selected" : "" %>><%= estado %></option>
        <% } %>
    </select>
    <button type="submit" class="boton">Filtrar</button>
</form>
<table class="tabla">
    <thead><tr><th>N.º</th><th>Cliente</th><th>Fecha</th><th>Productos</th><th class="num">Total</th><th>Estado</th><th></th></tr></thead>
    <tbody>
    <% for (Pedido pedido : pedidos) {
           pageContext.setAttribute("p", pedido); %>
    <tr>
        <td>#${p.idPedido}</td>
        <td>${gp:esc(p.nombreCliente)}</td>
        <td><%= pedido.getFechaPedido().toString().replace('T', ' ') %></td>
        <td>${p.detalles.size()}</td>
        <td class="num">${gp:moneda(p.calcularTotal())}</td>
        <td><ui:estado valor="${p.estado}"/></td>
        <td><a class="boton boton-pequeno" href="${pageContext.request.contextPath}/pedidos?accion=ver&amp;id=${p.idPedido}">Ver</a></td>
    </tr>
    <% } %>
    <% if (pedidos.isEmpty()) { %>
    <tr><td colspan="7" class="vacio">No hay pedidos con ese criterio.</td></tr>
    <% } %>
    </tbody>
</table>
<%@ include file="/WEB-INF/vistas/comun/pie.jspf" %>
