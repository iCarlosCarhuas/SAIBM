package edu.pe.cibertec.SAIBM.entity.security;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Table(name = "acceso")
@Entity
public class AccesoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "acceso_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "perfil_id", nullable = false)
    private PerfilEntity perfil;

    @ManyToOne
    @JoinColumn(name = "rol_id", nullable = false)
    private RolEntity rol;
}