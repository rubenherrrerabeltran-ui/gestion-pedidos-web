<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="gp" uri="https://sena.edu.co/gestionpedidos/funciones" %>
<% String tituloPagina = "Error"; %>
<%@ include file="/WEB-INF/vistas/comun/encabezado.jspf" %>
<%
    Object codigo = request.getAttribute("jakarta.servlet.error.status_code");
    String detalle = exception != null ? exception.getClass().getSimpleName() : "La página solicitada no existe";
%>
<section class="panel-error">
    <h1>Error <%= codigo != null ? codigo : 500 %></h1>
    <p><%= gpEscapar(detalle) %></p>
    <a class="boton boton-principal" href="${pageContext.request.contextPath}/inicio">Volver al inicio</a>
</section>
<%!
    private String gpEscapar(String texto) {
        return co.sena.gestionpedidos.web.util.Html.escapar(texto);
    }
%>
<%@ include file="/WEB-INF/vistas/comun/pie.jspf" %>
