package edu.pe.cibertec.SAIBM.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PedidoDetalleLibroDto {
    private Integer libro_id;
    private String nombre_libro;
    private Integer stock;
    private String autor;
}
