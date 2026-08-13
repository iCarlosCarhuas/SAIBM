package edu.pe.cibertec.saibm.inventario.application.port.out; import java.util.UUID; public interface InboxPort {boolean firstSeen(UUID eventId,String eventType);}
