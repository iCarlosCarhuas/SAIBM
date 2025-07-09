package edu.pe.cibertec.SAIBM.entity.security;

import edu.pe.cibertec.SAIBM.entity.MembresiaEntity;
import edu.pe.cibertec.SAIBM.service.UsuarioService;
import jakarta.persistence.*;
import lombok.Data;
@Data
@Entity
@Table(name="usuarios")
public class UsuarioEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name= "usuario_id")
        private Integer id;

        @Column(name = "nombre")
        private String nombre;

        @Column(name = "apellido")
        private String apellido;

        @Column(name = "dni")
        private Integer dni;

        @Column(name = "correo")
        private String correo;


        @Column(name = "contraseña")
        private String contraseña;

        @ManyToOne
        @JoinColumn(name = "rol_id")
        private RolEntity rol;

        @ManyToOne
        @JoinColumn(name = "membresia_id")
        private MembresiaEntity membresia;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "perfil_id")
        private PerfilEntity perfil;


}
