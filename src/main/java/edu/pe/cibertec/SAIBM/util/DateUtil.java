package edu.pe.cibertec.SAIBM.util;// DateUtil.java
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class DateUtil {

    public static LocalDate toLocalDate(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    public static LocalDate sumarMeses(LocalDate fecha, int meses) {
        return fecha.plusMonths(meses);
    }
}
