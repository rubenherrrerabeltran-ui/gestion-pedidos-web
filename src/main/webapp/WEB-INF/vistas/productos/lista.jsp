<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, co.sena.gestionpedidos.modelo.Producto" %>
<%@ taglib prefix="gp" uri="https://sena.edu.co/gestionpedidos/funciones" %>
<% String tituloPagina = "Productos"; %>
<%@ include file="/WEB-INF/vistas/comun/encabezado.jspf" %>
<%
    @SuppressWarnings("unchecked")
    List<Producto> productos = (List<Producto>) request.getAttribute("productos");
%>
<section class="encabezado-pagina">
    <div>
        <h1>Productos</h1>
        <p class="ayuda">Inventario de productos. La búsqueda usa <strong>GET</strong>: el texto viaja en la URL
            (<code>?texto=...</code>) porque solo consulta datos.</p>
    </div>
    <a class="boton boton-principal" href="${pageContext.request.contextPath}/productos?accion=nuevo">+ Nuevo producto</a>
</section>

<form class="buscador" method="get" action="${pageContext.request.contextPath}/productos">
    <input type="search" name="texto" value="${gp:esc(param.texto)}" placeholder="Buscar por nombre…">
    <button type="submit" class="boton">Buscar</button>
    <% if (request.getParameter("texto") != null && !request.getParameter("texto").isBlank()) { %>
        <a class="boton boton-claro" href="${pageContext.request.contextPath}/productos">Ver todos</a>
    <% } %>
</form>

<table class="tabla">
    <thead>
    <tr><th>ID</th><th>Nombre</th><th class="num">Precio</th><th class="num">Stock</th><th>Estado</th><th>Acciones</th></tr>
    </thead>
    <tbody>
    <% for (Producto producto : productos) {
           pageContext.setAttribute("p", producto); %>
    <tr>
        <td>${p.idProducto}</td>
        <td>${gp:esc(p.nombreProducto)}</td>
        <td class="num">${gp:moneda(p.precio)}</td>
        <td class="num ${p.stock lt 10 ? 'stock-bajo' : ''}">${p.stock}</td>
        <td>${p.activo ? 'Activo' : 'Inactivo'}</td>
        <td class="acciones">
            <a class="boton boton-pequeno" href="${pageContext.request.contextPath}/productos?accion=editar&amp;id=${p.idProducto}">Editar</a>
            <form method="post" action="${pageContext.request.contextPath}/productos"
                  onsubmit="return confirm('¿Eliminar el producto ${p.idProducto}?');">
                <input type="hidden" name="accion" value="eliminar">
                <input type="hidden" name="id" value="${p.idProducto}">
                <button type="submit" class="boton boton-pequeno boton-peligro">Eliminar</button>
            </form>
        </td>
    </tr>
    <% } %>
    <% if (productos.isEmpty()) { %>
    <tr><td colspan="6" class="vacio">No se encontraron productos.</td></tr>
    <% } %>
    </tbody>
</table>
<p class="ayuda">Total: <%= productos.size() %> producto(s)</p>
<%@ include file="/WEB-INF/vistas/comun/pie.jspf" %>
