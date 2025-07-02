package edu.pe.cibertec.SAIBM.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "membresia")
public class MembresiaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "membresia_id")
    private Integer id;

    @Column(name = "nombre_membresia")
    private String nombreMembresia;
}
