<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="gp" uri="https://sena.edu.co/gestionpedidos/funciones" %>
<%-- Acción jsp:useBean: toma el objeto "producto" que el servlet dejó en el ámbito request --%>
<jsp:useBean id="producto" class="co.sena.gestionpedidos.modelo.Producto" scope="request"/>
<% String tituloPagina = producto.getIdProducto() == null ? "Nuevo producto" : "Editar producto"; %>
<%@ include file="/WEB-INF/vistas/comun/encabezado.jspf" %>
<section class="encabezado-pagina">
    <div>
        <h1><%= tituloPagina %></h1>
        <% if (producto.getIdProducto() != null) { %>
        <p class="ayuda">Código del producto: <strong><jsp:getProperty name="producto" property="idProducto"/></strong></p>
        <% } else { %>
        <p class="ayuda">Los datos se envían con el método <strong>POST</strong> al servlet <code>/productos</code>.</p>
        <% } %>
    </div>
</section>
<form class="formulario tarjeta-formulario" method="post" action="${pageContext.request.contextPath}/productos">
    <input type="hidden" name="accion" value="guardar">
    <input type="hidden" name="id" value="${producto.idProducto}">

    <label for="nombreProducto">Nombre del producto</label>
    <input type="text" id="nombreProducto" name="nombreProducto" required maxlength="120"
           value="${gp:esc(producto.nombreProducto)}">

    <div class="fila">
        <div>
            <label for="precio">Precio (COP)</label>
            <input type="number" id="precio" name="precio" required min="0" step="0.01" value="${producto.precio}">
        </div>
        <div>
            <label for="stock">Stock</label>
            <input type="number" id="stock" name="stock" required min="0" step="1" value="${producto.stock}">
        </div>
    </div>

    <label class="casilla"><input type="checkbox" name="activo" ${producto.activo ? 'checked' : ''}> Producto activo</label>

    <div class="botones">
        <button type="submit" class="boton boton-principal">Guardar</button>
        <a class="boton boton-claro" href="${pageContext.request.contextPath}/productos">Cancelar</a>
    </div>
</form>
<%@ include file="/WEB-INF/vistas/comun/pie.jspf" %>
