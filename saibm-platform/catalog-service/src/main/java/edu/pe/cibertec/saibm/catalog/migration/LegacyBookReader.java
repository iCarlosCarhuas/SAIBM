package edu.pe.cibertec.saibm.catalog.migration;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

/** Read-only legacy adapter used only by the explicit backfill command. */
public class LegacyBookReader {
    private final JdbcTemplate jdbc;

    public LegacyBookReader(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<LegacyBookRow> read() {
        return jdbc.query("select libro_id, nombre_libro, descripcion, stock, autor, imagen "
                + "from libros", (rs, rowNum) -> new LegacyBookRow(rs.getObject("libro_id", Integer.class),
                        rs.getString("nombre_libro"), rs.getString("descripcion"), rs.getObject("stock", Integer.class),
                        rs.getString("autor"), rs.getString("imagen")));
    }
}
