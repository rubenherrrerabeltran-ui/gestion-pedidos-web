-- ============================================================================
-- SENA | ADSO ficha 3186691 | Evidencias GA7-220501096-AA2-EV01 y EV02
-- PROYECTO : Sistema de gestión de pedidos - módulo web (servlets y JSP)
-- MOTOR    : MySQL 8.0.16 o posterior | InnoDB | utf8mb4
-- ORIGEN   : Modelo físico GA6-220501096-AA2-EV02/EV03 (usuario, producto,
--            pedido, detalle_pedido). Ajuste v1.1: columna usuario.rol para los
--            roles Administrador, Vendedor y Cliente definidos en el informe del
--            plan de trabajo GA7-220501096-AA1-EV01.
-- AUTOR    : Rubén Darío Herrera Beltrán
-- ============================================================================
CREATE DATABASE IF NOT EXISTS sena_gestion_pedidos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sena_gestion_pedidos;

CREATE TABLE IF NOT EXISTS usuario (
 id_usuario INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
 nombre VARCHAR(100) NOT NULL,
 correo VARCHAR(150) NOT NULL,
 password_hash VARCHAR(255) NOT NULL,
 rol VARCHAR(15) NOT NULL DEFAULT 'CLIENTE',
 fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT uq_usuario_correo UNIQUE (correo),
 CONSTRAINT chk_usuario_rol CHECK (rol IN ('ADMINISTRADOR','VENDEDOR','CLIENTE'))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS producto (
 id_producto INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
 nombre_producto VARCHAR(120) NOT NULL,
 precio DECIMAL(12,2) NOT NULL,
 stock INT UNSIGNED NOT NULL DEFAULT 0,
 activo BOOLEAN NOT NULL DEFAULT TRUE,
 CONSTRAINT chk_producto_precio CHECK (precio >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS pedido (
 id_pedido INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
 id_usuario INT UNSIGNED NOT NULL,
 fecha_pedido DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 estado VARCHAR(20) NOT NULL DEFAULT 'pendiente',
 CONSTRAINT fk_pedido_usuario FOREIGN KEY (id_usuario)
   REFERENCES usuario(id_usuario) ON UPDATE CASCADE ON DELETE RESTRICT,
 CONSTRAINT chk_pedido_estado CHECK (estado IN ('pendiente','pagado','enviado','cancelado'))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS detalle_pedido (
 id_detalle INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
 id_pedido INT UNSIGNED NOT NULL,
 id_producto INT UNSIGNED NOT NULL,
 cantidad INT UNSIGNED NOT NULL,
 precio_unitario DECIMAL(12,2) NOT NULL,
 CONSTRAINT fk_detalle_pedido FOREIGN KEY (id_pedido)
   REFERENCES pedido(id_pedido) ON UPDATE CASCADE ON DELETE CASCADE,
 CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto)
   REFERENCES producto(id_producto) ON UPDATE CASCADE ON DELETE RESTRICT,
 CONSTRAINT uq_detalle_pedido_producto UNIQUE (id_pedido,id_producto),
 CONSTRAINT chk_detalle_cantidad CHECK (cantidad > 0),
 CONSTRAINT chk_detalle_precio CHECK (precio_unitario >= 0)
) ENGINE=InnoDB;
