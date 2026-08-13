package edu.pe.cibertec.saibm.usuario.infrastructure.migration; import java.util.List; public record UserBackfillReport(boolean reconciled,int sourceCount,int targetCount,List<String> errors){}
