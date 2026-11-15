<%@ tag description="Tarjeta de indicador para el panel de inicio" pageEncoding="UTF-8" body-content="scriptless" %>
<%@ attribute name="titulo" required="true" %>
<%@ attribute name="valor" required="true" %>
<%@ attribute name="enlace" required="false" %>
<div class="tarjeta">
    <span class="tarjeta-titulo">${titulo}</span>
    <strong class="tarjeta-valor">${valor}</strong>
    <span class="tarjeta-detalle"><jsp:doBody/></span>
    <% Object enlaceTarjeta = jspContext.getAttribute("enlace"); if (enlaceTarjeta != null && !enlaceTarjeta.toString().isEmpty()) { %><a class="tarjeta-enlace" href="${pageContext.request.contextPath}${enlace}">Ver detalle &rarr;</a><% } %>
</div>
