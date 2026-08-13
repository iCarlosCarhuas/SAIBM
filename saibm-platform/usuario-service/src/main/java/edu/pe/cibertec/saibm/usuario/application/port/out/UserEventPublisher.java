package edu.pe.cibertec.saibm.usuario.application.port.out; import edu.pe.cibertec.saibm.usuario.domain.event.UserEvent; public interface UserEventPublisher {void publish(UserEvent event);}
