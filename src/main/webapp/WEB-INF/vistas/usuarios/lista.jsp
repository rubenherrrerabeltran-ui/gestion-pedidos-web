<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, co.sena.gestionpedidos.modelo.Usuario" %>
<%@ taglib prefix="gp" uri="https://sena.edu.co/gestionpedidos/funciones" %>
<% String tituloPagina = "Usuarios"; %>
<%@ include file="/WEB-INF/vistas/comun/encabezado.jspf" %>
<%
    @SuppressWarnings("unchecked")
    List<Usuario> usuarios = (List<Usuario>) request.getAttribute("usuarios");
%>
<section class="encabezado-pagina">
    <div>
        <h1>Usuarios</h1>
        <p class="ayuda">Módulo exclusivo del rol ADMINISTRADOR. Las contraseñas se guardan cifradas (PBKDF2).</p>
    </div>
    <a class="boton boton-principal" href="${pageContext.request.contextPath}/usuarios?accion=nuevo">+ Nuevo usuario</a>
</section>
<table class="tabla">
    <thead><tr><th>ID</th><th>Nombre</th><th>Correo</th><th>Rol</th><th>Registro</th><th>Acciones</th></tr></thead>
    <tbody>
    <% for (Usuario usuario : usuarios) {
           pageContext.setAttribute("u", usuario); %>
    <tr>
        <td>${u.idUsuario}</td>
        <td>${gp:esc(u.nombre)}</td>
        <td>${gp:esc(u.correo)}</td>
        <td><span class="rol rol-${u.rol}">${u.rol}</span></td>
        <td><%= usuario.getFechaRegistro() == null ? "-" : usuario.getFechaRegistro().toLocalDate() %></td>
        <td class="acciones">
            <a class="boton boton-pequeno" href="${pageContext.request.contextPath}/usuarios?accion=editar&amp;id=${u.idUsuario}">Editar</a>
            <form method="post" action="${pageContext.request.contextPath}/usuarios"
                  onsubmit="return confirm('¿Eliminar el usuario ${u.idUsuario}?');">
                <input type="hidden" name="accion" value="eliminar">
                <input type="hidden" name="id" value="${u.idUsuario}">
                <button type="submit" class="boton boton-pequeno boton-peligro">Eliminar</button>
            </form>
        </td>
    </tr>
    <% } %>
    </tbody>
</table>
<p class="ayuda">Total: <%= usuarios.size() %> usuario(s)</p>
<%@ include file="/WEB-INF/vistas/comun/pie.jspf" %>
