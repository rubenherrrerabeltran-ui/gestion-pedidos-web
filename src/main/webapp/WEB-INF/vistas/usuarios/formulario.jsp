<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="co.sena.gestionpedidos.modelo.Rol" %>
<%@ taglib prefix="gp" uri="https://sena.edu.co/gestionpedidos/funciones" %>
<jsp:useBean id="usuario" class="co.sena.gestionpedidos.modelo.Usuario" scope="request"/>
<%
    boolean esNuevo = usuario.getIdUsuario() == null;
    String tituloPagina = esNuevo ? "Nuevo usuario" : "Editar usuario";
%>
<%@ include file="/WEB-INF/vistas/comun/encabezado.jspf" %>
<section class="encabezado-pagina">
    <div>
        <h1><%= tituloPagina %></h1>
        <p class="ayuda"><%= esNuevo ? "La contraseña es obligatoria (mínimo 8 caracteres)."
                : "Deje la contraseña vacía para conservar la actual." %></p>
    </div>
</section>
<form class="formulario tarjeta-formulario" method="post" action="${pageContext.request.contextPath}/usuarios">
    <input type="hidden" name="accion" value="guardar">
    <input type="hidden" name="id" value="${usuario.idUsuario}">

    <label for="nombre">Nombre completo</label>
    <input type="text" id="nombre" name="nombre" required maxlength="100" value="${gp:esc(usuario.nombre)}">

    <label for="correo">Correo electrónico</label>
    <input type="email" id="correo" name="correo" required maxlength="150" value="${gp:esc(usuario.correo)}">

    <div class="fila">
        <div>
            <label for="clave">Contraseña</label>
            <input type="password" id="clave" name="clave" <%= esNuevo ? "required" : "" %> minlength="8" autocomplete="new-password">
        </div>
        <div>
            <label for="rol">Rol</label>
            <select id="rol" name="rol">
                <% for (Rol rol : Rol.values()) { %>
                <option value="<%= rol %>" <%= rol == usuario.getRol() ? "selected" : "" %>><%= rol %></option>
                <% } %>
            </select>
        </div>
    </div>
    <div class="botones">
        <button type="submit" class="boton boton-principal">Guardar</button>
        <a class="boton boton-claro" href="${pageContext.request.contextPath}/usuarios">Cancelar</a>
    </div>
</form>
<%@ include file="/WEB-INF/vistas/comun/pie.jspf" %>
