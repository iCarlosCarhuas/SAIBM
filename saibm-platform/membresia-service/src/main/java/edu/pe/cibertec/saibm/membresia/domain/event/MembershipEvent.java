package edu.pe.cibertec.saibm.membresia.domain.event;
import java.time.Instant; import java.util.UUID; import java.util.Map;
public record MembershipEvent(UUID eventId,String eventType,int eventVersion,Instant occurredAt,String correlationId,String causationId,String producer,Map<String,Object> payload){}
