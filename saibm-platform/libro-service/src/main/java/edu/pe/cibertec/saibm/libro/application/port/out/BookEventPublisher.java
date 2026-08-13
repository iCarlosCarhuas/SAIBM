package edu.pe.cibertec.saibm.libro.application.port.out;
import edu.pe.cibertec.saibm.libro.domain.event.BookChanged;
@FunctionalInterface public interface BookEventPublisher { void publish(BookChanged event); }
