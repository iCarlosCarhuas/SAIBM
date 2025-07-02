package edu.pe.cibertec.SAIBM.entity.security;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Table(name = "rol")
@Entity
public class RolEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rol_id")
    private Integer id;
    @Column(name = "nombre_rol")
    private String nombreRol;
    @Column(name = "ruta_rol")
    private String rutaRol;
    @Column(name = "descripcion_rol")
    private String descripcionRol;

    @OneToMany(mappedBy = "rol")
    private List<AccesoEntity> accesos;
}