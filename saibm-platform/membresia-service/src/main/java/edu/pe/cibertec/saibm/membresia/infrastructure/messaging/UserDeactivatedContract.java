package edu.pe.cibertec.saibm.membresia.infrastructure.messaging;
public record UserDeactivatedContract(String eventType,int eventVersion,String userId){public UserDeactivatedContract{if(!"UserDeactivated".equals(eventType)||eventVersion!=1)throw new IllegalArgumentException("Unsupported user lifecycle event");}}
