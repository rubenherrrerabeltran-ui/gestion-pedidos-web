-- ============================================================================
-- SENA | Evidencia GA7-220501096-AA2-EV02 | Datos de prueba (entorno local)
-- Ejecutar después de 01_esquema.sql. Las contraseñas se guardan como hash
-- PBKDF2 generado por co.sena.gestionpedidos.util.ClaveUtil.
-- Credenciales SOLO para pruebas locales (cámbielas en un entorno real):
--   admin@gestionpedidos.local     / Admin2026*     (ADMINISTRADOR)
--   laura.vendedora@gestionpedidos.local / Vendedor2026*  (VENDEDOR)
--   carlos.cliente@gestionpedidos.local  / Cliente2026*   (CLIENTE)
-- ============================================================================
USE sena_gestion_pedidos;

INSERT INTO usuario (nombre, correo, password_hash, rol) VALUES
 ('Administrador del sistema', 'admin@gestionpedidos.local', 'pbkdf2$120000$1ddwVi2xLeTUNIqPdHnQAQ==$n/KPxngLX5nteR/5s/4psSG21ooP5bB7GGi3OhemcBE=', 'ADMINISTRADOR'),
 ('Laura Gómez', 'laura.vendedora@gestionpedidos.local', 'pbkdf2$120000$e9TTWoIhWVUwlK3t0J3oGQ==$MBhjVH8tFR3K2K7OhFz2pIZYT+dpLHZxjUFh2VbGyUM=', 'VENDEDOR'),
 ('Carlos Ramírez', 'carlos.cliente@gestionpedidos.local', 'pbkdf2$120000$Wlkk7tVVliaixkqvmSqJkg==$g3DTYFZtN77CiWzRjjAYIDguj9nxqc/FfUcSd7rTiYQ=', 'CLIENTE');

INSERT INTO producto (nombre_producto, precio, stock, activo) VALUES
 ('Café de origen Huila 500 g', 28000.00, 40, TRUE),
 ('Panela orgánica 1 kg', 6500.00, 60, TRUE),
 ('Chocolate de mesa 250 g', 9800.00, 25, TRUE),
 ('Arepas de maíz x 10', 7200.00, 35, TRUE),
 ('Queso campesino 500 g', 14500.00, 18, TRUE);
