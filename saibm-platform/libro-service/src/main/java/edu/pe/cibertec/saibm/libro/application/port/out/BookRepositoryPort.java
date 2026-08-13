package edu.pe.cibertec.saibm.libro.application.port.out;
import java.util.*;
import edu.pe.cibertec.saibm.libro.domain.model.Book;
public interface BookRepositoryPort { Optional<Book> find(UUID id); Book save(Book book); Page search(String q,int page,int size);
 record Page(List<Book> content,int page,int size,long totalElements){public int totalPages(){return (int)Math.ceil((double)totalElements/size);}} }
