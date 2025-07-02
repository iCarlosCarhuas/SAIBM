package edu.pe.cibertec.SAIBM.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "libros")
public class LibroEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "libro_id")
    private Integer id;

    @Column(name = "nombre_libro")
    private String nombreLibro;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "stock")
    private Integer stock;

    @Column(name = "autor")
    private String autor;

    @Column(name = "imagen")
    private String imagen;
}
