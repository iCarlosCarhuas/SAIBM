package edu.pe.cibertec.saibm.membresia.domain.model;
import java.time.Instant; import java.util.UUID;
public record MembershipHistory(UUID id,String userId,UUID assignmentId,String action,Instant occurredAt,String correlationId){}
