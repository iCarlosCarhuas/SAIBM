
CREATE DATABASE IF NOT EXISTS SAIBM;
USE SAIBM;


CREATE TABLE IF NOT EXISTS usuarios (
    usuario_id INT AUTO_INCREMENT PRIMARY KEY,
    rol_id INT not null,
    membresia_id INT not null,
    perfil_id INT not null,
    nombre VARCHAR(50) NOT NULL,
    apellido VARCHAR(50) NOT NULL,
    dni INT UNIQUE NOT NULL,
    correo VARCHAR(100) UNIQUE NOT NULL,
    contraseña VARCHAR(100) NOT NULL,
    CONSTRAINT fk_rolId FOREIGN KEY (rol_id) references rol(rol_id),
    CONSTRAINT fk_membresiaId  FOREIGN KEY (membresia_id) references membresia(membresia_id),
    CONSTRAINT fk_perfilId FOREIGN KEY (perfil_id) references perfil(perfil_id)
);

select * from usuarios

create table perfil
(
    perfil_id     int auto_increment
        primary key,
    nombre varchar(255) null
);

create table if not exists rol (
	rol_id INT auto_increment primary key,
	nombre_rol VARCHAR(250) null,
	ruta_rol VARCHAR(50) null,
	descripcion_rol VARCHAR(50) null
);



create table acceso
(
    acceso_id        int auto_increment
        primary key,
    perfil_id int not null,
    rol_id    int not null,
    constraint FKcnmr5fad558jr7b2gug2v34rl
        foreign key (rol_id) references rol (rol_id),
    constraint FKtgv6rbky8xhmsgvi5qil93g5m
        foreign key (perfil_id) references perfil (perfil_id)
);


-- Crear tabla de libros
CREATE TABLE IF NOT EXISTS libros (
    libro_id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_libro VARCHAR(255) NOT NULL,
    descripcion TEXT,
    stock INT DEFAULT 0,
    autor VARCHAR(255),
    imagen VARCHAR(300)
);


create table if not exists membresia (
	membresia_id INT auto_increment primary key,
	nombre_membresia VARCHAR(50) NOT NULL
)

-- Crear tabla de reserva
CREATE TABLE IF NOT EXISTS reserva (
    reserva_id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    libro_id INT NOT NULL,
    fecha_reserva DATE,
    fecha_expiracion DATE,

    CONSTRAINT fk_usuarioId  FOREIGN KEY (usuario_id) references usuarios(usuario_id),
    CONSTRAINT fk_libroId  FOREIGN KEY (libro_id) references libros(libro_id)

);

-- -----------------------------------------------------------------------
-- Insertar usuarios comunes


INSERT INTO perfil (perfil_id, nombre) VALUES
(1, 'Administrador'),
(2, 'Cliente')

INSERT INTO rol (rol_id, nombre_rol, ruta_rol, descripcion_rol ) VALUES
(1, 'ROLE_ADMIN', "/admin", "Acceso completo al sistema"),
(2, 'ROLE_USER', "/libro", "Acceso a reserva de libros")

INSERT INTO acceso (perfil_id, rol_id) VALUES
(1, 1),
(2, 2)

INSERT INTO membresia (membresia_id, nombre_membresia) VALUES
(null, 'premium'),
(null, 'super premium'),
(null, 'super premium plus'),
(null, 'null');

INSERT INTO usuarios (rol_id, membresia_id,perfil_id,nombre, apellido, dni, correo, contraseña) VALUES
(2,1,2,'Luis', 'Gonzales', 12345678, 'luis@mail.com', '1234' ),
(2,3,2,'María', 'Ramirez', 23456789, 'maria@mail.com', 'abcd'),
(2,2,2,'Carlos', 'Lopez', 34567890, 'carlos@mail.com', 'pass1'),
(2,1,2,'Lucía', 'Torres', 45678901, 'lucia@mail.com', 'lucia123'),
(2,1,2,'Pedro', 'Fernandez', 56789012, 'pedro@mail.com', 'p1234'),
(2,3,2,'Sofía', 'Rojas', 67890123, 'sofia@mail.com', 's1234');
-- Insertar admin
INSERT INTO usuarios (rol_id, membresia_id,perfil_id,nombre, apellido, dni, correo, contraseña) VALUES
(1,4,1, 'Admin','Root', 99999999, 'admin@mail.com', 'admin123');


-- Actualización de membresía
UPDATE usuarios
SET membresia = 'super premium plus'
WHERE usuario_id = 6;

-- Insertar libros
INSERT INTO libros (nombre_libro, descripcion, stock, autor, imagen) VALUES
('El Principito', 'Un libro clásico sobre la amistad y la imaginación.', 10, 'Antoine de Saint-Exupéry', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS4yNX5KPlXD1aSiv2wwgEfhBrckONyp99jkg&s'),
('Cien años de soledad', 'Una novela sobre la historia de la familia Buendía.', 5, 'Gabriel García Márquez', 'https://www.penguinlibros.com/pe/3613111/cien-anos-de-soledad-edicion-ilustrada.jpg'),
('Don Quijote de la Mancha', 'Una obra literaria fundamental del Siglo de Oro español.', 7, 'Miguel de Cervantes', 'https://www.crisol.com.pe/media/catalog/product/cache/f6d2c62455a42b0d712f6c919e880845/9/7/9788466243711_cmxsq1fjdkbkrmap.jpg'),
('La Odisea', 'Poema épico que narra el viaje de Odiseo de regreso a Ítaca.', 3, 'Homero', 'https://editorialverbum.es/wp-content/uploads/2020/07/La-odisea.jpg'),
('1984', 'Novela distópica sobre una sociedad controlada por el Gran Hermano.', 6, 'George Orwell','https://m.media-amazon.com/images/I/612ADI+BVlL._AC_UF1000,1000_QL80_.jpg');

-- Procedimiento para eliminar un usuario y sus reservas, y actualizar stock
DELIMITER //
CREATE PROCEDURE usp_eliminar_Usuario_Y_Reserva_actualizar_stock(IN p_usuario_id INT)
BEGIN
    -- Actualiza el stock de libros reservados
    UPDATE libros l
    JOIN reserva r ON l.libro_id = r.libro_id
    SET l.stock = l.stock + 1
    WHERE r.usuario_id = p_usuario_id;

    -- Elimina las reservas del usuario
    DELETE FROM reserva
    WHERE usuario_id = p_usuario_id;

    -- Elimina el usuario
    DELETE FROM usuarios
    WHERE usuario_id = p_usuario_id;
END;
//
DELIMITER ;

-- Procedimiento para eliminar un libro y sus reservas
DELIMITER //
CREATE PROCEDURE usp_eliminar_Libro_Y_Reserva(IN p_libro_id INT)
BEGIN
    -- Elimina las reservas del libro
    DELETE FROM reserva
    WHERE libro_id = p_libro_id;

    -- Elimina el libro
    DELETE FROM libros
    WHERE libro_id = p_libro_id;
END;
//
DELIMITER ;

-- Login básico
SELECT * FROM usuarios WHERE correo = 'luis@mail.com' AND contraseña = '1234';
