<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, co.sena.gestionpedidos.modelo.Producto, co.sena.gestionpedidos.modelo.Usuario" %>
<%@ taglib prefix="gp" uri="https://sena.edu.co/gestionpedidos/funciones" %>
<%! private static final int FILAS_PRODUCTO = 4; %>
<% String tituloPagina = "Nuevo pedido"; %>
<%@ include file="/WEB-INF/vistas/comun/encabezado.jspf" %>
<%
    @SuppressWarnings("unchecked")
    List<Producto> productos = (List<Producto>) request.getAttribute("productos");
    @SuppressWarnings("unchecked")
    List<Usuario> clientes = (List<Usuario>) request.getAttribute("clientes");
    boolean esCliente = usuarioEncabezado.getRol() == Rol.CLIENTE;
%>
<section class="encabezado-pagina">
    <div>
        <h1>Nuevo pedido</h1>
        <p class="ayuda">Cada fila envía los parámetros <code>producto</code> y <code>cantidad</code> con el método
            <strong>POST</strong>; el servlet los lee con <code>getParameterValues()</code>.</p>
    </div>
</section>
<form class="formulario tarjeta-formulario ancho" method="post" action="${pageContext.request.contextPath}/pedidos">
    <input type="hidden" name="accion" value="registrar">
    <% if (!esCliente) { %>
    <label for="idCliente">Cliente</label>
    <select id="idCliente" name="idCliente" required>
        <% for (Usuario cliente : clientes) { %>
        <option value="<%= cliente.getIdUsuario() %>"><%= co.sena.gestionpedidos.web.util.Html.escapar(cliente.getNombre()) %></option>
        <% } %>
    </select>
    <% } %>
    <table class="tabla tabla-formulario">
        <thead><tr><th>Producto</th><th class="num">Cantidad</th></tr></thead>
        <tbody>
        <% for (int fila = 1; fila <= FILAS_PRODUCTO; fila++) { %>
        <tr>
            <td>
                <select name="producto" aria-label="Producto <%= fila %>">
                    <option value="">— Seleccione —</option>
                    <% for (Producto producto : productos) { %>
                    <option value="<%= producto.getIdProducto() %>"><%= co.sena.gestionpedidos.web.util.Html.escapar(producto.getNombreProducto()) %>
                        · <%= co.sena.gestionpedidos.web.util.Html.moneda(producto.getPrecio()) %> · stock <%= producto.getStock() %></option>
                    <% } %>
                </select>
            </td>
            <td class="num"><input type="number" name="cantidad" min="1" step="1" placeholder="0" aria-label="Cantidad <%= fila %>"></td>
        </tr>
        <% } %>
        </tbody>
    </table>
    <div class="botones">
        <button type="submit" class="boton boton-principal">Registrar pedido</button>
        <a class="boton boton-claro" href="${pageContext.request.contextPath}/pedidos">Cancelar</a>
    </div>
</form>
<%@ include file="/WEB-INF/vistas/comun/pie.jspf" %>
