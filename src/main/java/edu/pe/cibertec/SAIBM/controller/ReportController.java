package edu.pe.cibertec.SAIBM.controller;

import edu.pe.cibertec.SAIBM.dto.PedidoDetalleLibroDto;
import edu.pe.cibertec.SAIBM.dto.PedidoDetalleReservaDto;
import edu.pe.cibertec.SAIBM.entity.LibroEntity;
import edu.pe.cibertec.SAIBM.entity.ReservaEntity;
import edu.pe.cibertec.SAIBM.service.LibroService;
import edu.pe.cibertec.SAIBM.service.ReservaService;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ReportController {

    @Autowired
    private LibroService libroService;

    @Autowired
    private ReservaService reservaService;

    @GetMapping("/generar/pdf/{usuarioId}")
    public void generarPDF(@PathVariable("usuarioId") Integer usuarioId, HttpServletResponse response) throws Exception {

        List<ReservaEntity> reservas = reservaService.conseguirPorUsuario(usuarioId);

        List<PedidoDetalleReservaDto> detallesDto = reservas.stream()
                .map(reserva -> new PedidoDetalleReservaDto(
                        reserva.getUsuario().getId(),
                        reserva.getUsuario().getNombre(),
                        reserva.getUsuario().getApellido(),
                        reserva.getUsuario().getDni(),
                        reserva.getUsuario().getMembresia().getNombreMembresia(),
                        reserva.getLibro().getId(),
                        reserva.getLibro().getNombreLibro(),
                        java.sql.Date.valueOf(reserva.getFechaReserva()),
                        java.sql.Date.valueOf(reserva.getFechaExpiracion()),
                        reservas.size() // num_lib = total libros
                ))
                .collect(Collectors.toList());

        InputStream jasperStream = getClass().getResourceAsStream("/jasperReports/reportjs.jasper");

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(detallesDto);

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperStream, null, dataSource);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=reservas_usuario_" + usuarioId + ".pdf");

        JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
    }
    @GetMapping("/generar/reporteLibros")
    public void generarReporteLibros(HttpServletResponse response) throws Exception {
        List<LibroEntity> libros = libroService.conseguirTodo();

        List<PedidoDetalleLibroDto> detallesDto = libros.stream()
                .map(libro -> new PedidoDetalleLibroDto(
                        libro.getId(),
                        libro.getNombreLibro(),
                        libro.getStock(),
                        libro.getAutor()
                ))
                .collect(Collectors.toList());

        InputStream jasperStream = getClass().getResourceAsStream("/jasperReports/reportlibros.jasper");

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(detallesDto);

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperStream, null, dataSource);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=reporte_libros.pdf");

        JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
    }

}
