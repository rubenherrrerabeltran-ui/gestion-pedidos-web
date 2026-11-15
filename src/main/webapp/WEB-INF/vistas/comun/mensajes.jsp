<%--
  Página incluida con la acción jsp:include (inclusión dinámica en tiempo de ejecución).
  Muestra una sola vez el mensaje guardado en la sesión por el servlet (patrón Post/Redirect/Get)
  y el error de validación enviado en la petición.
--%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="co.sena.gestionpedidos.web.util.Html" %>
<%
    String mensaje = (String) session.getAttribute("mensajeFlash");
    String tipo = (String) session.getAttribute("tipoMensajeFlash");
    if (mensaje != null) {
        session.removeAttribute("mensajeFlash");
        session.removeAttribute("tipoMensajeFlash");
%>
<div class="alerta alerta-<%= "error".equals(tipo) ? "error" : "exito" %>"><%= Html.escapar(mensaje) %></div>
<%
    }
    Object errorValidacion = request.getAttribute("error");
    if (errorValidacion != null) {
%>
<div class="alerta alerta-error"><%= Html.escapar(errorValidacion) %></div>
<% } %>
