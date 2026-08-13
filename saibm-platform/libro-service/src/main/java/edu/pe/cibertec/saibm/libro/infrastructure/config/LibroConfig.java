package edu.pe.cibertec.saibm.libro.infrastructure.config;
import org.springframework.context.annotation.*; import edu.pe.cibertec.saibm.libro.application.port.in.BookUseCase; import edu.pe.cibertec.saibm.libro.application.port.out.*; import edu.pe.cibertec.saibm.libro.application.usecase.BookService;
@Configuration public class LibroConfig { @Bean BookUseCase books(BookRepositoryPort r,BookEventPublisher e){return new BookService(r,e);} @Bean BookEventPublisher events(){return e->{};} }
