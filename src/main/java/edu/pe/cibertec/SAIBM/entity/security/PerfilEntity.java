package edu.pe.cibertec.SAIBM.entity.security;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Table(name = "perfil")
@Entity
public class PerfilEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "perfil_id")
    private Integer id;
    private String nombre;

    @OneToMany(mappedBy = "perfil", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AccesoEntity> accesos;
}