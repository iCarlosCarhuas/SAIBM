CREATE TABLE IF NOT EXISTS libros (
    libro_id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_libro VARCHAR(255) NOT NULL,
    descripcion TEXT,
    stock INT DEFAULT 0,
    autor VARCHAR(255),
    imagen VARCHAR(300)
);

DELETE FROM libros WHERE nombre_libro = 'WP-00-A deterministic fixture';

INSERT INTO libros (nombre_libro, descripcion, stock, autor, imagen)
VALUES ('WP-00-A deterministic fixture', 'Fixture for the WP-00-A evidence gate', 4, 'SAIBM', 'fixture://wp-00-a');
