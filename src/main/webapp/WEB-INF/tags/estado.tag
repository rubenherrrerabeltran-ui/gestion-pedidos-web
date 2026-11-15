<%@ tag description="Etiqueta de color que muestra el estado de un pedido" pageEncoding="UTF-8" body-content="empty" %>
<%@ attribute name="valor" required="true" type="co.sena.gestionpedidos.modelo.EstadoPedido" %>
<span class="estado estado-${valor.codigoBd}">${valor}</span>
