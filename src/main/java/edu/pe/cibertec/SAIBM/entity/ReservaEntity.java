package edu.pe.cibertec.SAIBM.entity;

import edu.pe.cibertec.SAIBM.entity.security.UsuarioEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "reserva")
public class ReservaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reserva_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @ManyToOne
    @JoinColumn(name = "libro_id", nullable = false)
    private LibroEntity libro;

    @Column(name = "fecha_reserva")
    @Temporal(TemporalType.DATE)
    private LocalDate fechaReserva;

    @Column(name="fecha_expiracion")
    @Temporal(TemporalType.DATE)
    private LocalDate fechaExpiracion;

}
