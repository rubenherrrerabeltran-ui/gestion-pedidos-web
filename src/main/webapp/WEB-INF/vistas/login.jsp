<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="gp" uri="https://sena.edu.co/gestionpedidos/funciones" %>
<% String tituloPagina = "Iniciar sesión"; %>
<%@ include file="/WEB-INF/vistas/comun/encabezado.jspf" %>
<section class="panel-login">
    <h1>Iniciar sesión</h1>
    <p class="ayuda">Ingrese con su correo y contraseña. El formulario usa el método <strong>POST</strong>
        para que la contraseña no aparezca en la URL.</p>
    <form class="formulario" method="post" action="${pageContext.request.contextPath}/login" autocomplete="off">
        <label for="correo">Correo electrónico</label>
        <input type="email" id="correo" name="correo" required maxlength="150"
               value="${gp:esc(requestScope.correo)}" placeholder="usuario@correo.com">
        <label for="clave">Contraseña</label>
        <input type="password" id="clave" name="clave" required minlength="8" placeholder="Mínimo 8 caracteres">
        <button type="submit" class="boton boton-principal">Ingresar</button>
    </form>
</section>
<%@ include file="/WEB-INF/vistas/comun/pie.jspf" %>
