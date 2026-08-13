package edu.pe.cibertec.saibm.libro.domain.event;
import java.time.Instant;
import java.util.UUID;
public record BookChanged(UUID eventId,UUID bookId,String type,Instant occurredAt) { }
