package edu.pe.cibertec.SAIBM.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class PedidoDetalleReservaDto {

    private Integer reserva_id;
    private Integer usuario_id;
    private Integer libro_id;
    private java.util.Date fecha_reserva;
    private java.util.Date fecha_expiracion;
    private Integer rol_id;
    private Integer membresia_id;
    private Integer perfil_id;
    private String nombre;
    private String apellido;
    private Integer dni;
    private String nombre_membresia;
    private String nombre_libro;
    private String descripcion;
    private String stock;
    private String autor;
    private String num_lib;
    //private double num_lib;
    public PedidoDetalleReservaDto(Integer usuario_id, String nombre, String apellido,
                                   Integer dni, String nombre_membresia,
                                   Integer libro_id, String nombre_libro,
                                   Date fecha_reserva, Date fecha_expiracion,
                                   double num_lib) {
        this.usuario_id = usuario_id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.nombre_membresia = nombre_membresia;
        this.libro_id = libro_id;
        this.nombre_libro = nombre_libro;
        this.fecha_reserva = fecha_reserva;
        this.fecha_expiracion = fecha_expiracion;
        //this.num_lib = num_lib;
        this.num_lib = String.valueOf(num_lib); // casteado a string
    }

}