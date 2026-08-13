package edu.pe.cibertec.saibm.libro.application.usecase;
import java.time.Instant; import java.util.UUID;
import edu.pe.cibertec.saibm.libro.application.port.in.BookUseCase; import edu.pe.cibertec.saibm.libro.application.port.out.*; import edu.pe.cibertec.saibm.libro.domain.event.BookChanged; import edu.pe.cibertec.saibm.libro.domain.exception.BookNotFoundException; import edu.pe.cibertec.saibm.libro.domain.model.Book;
public class BookService implements BookUseCase { private final BookRepositoryPort books; private final BookEventPublisher events;
 public BookService(BookRepositoryPort b,BookEventPublisher e){books=b;events=e;} public View create(Create c){return change(Book.create(UUID.randomUUID(),c.title(),c.description(),c.author(),c.imageUrl()),"BookCreated");}
 public View update(UUID id,Update c){Book b=load(id).update(c.title(),c.description(),c.author(),c.imageUrl());return change(b,"BookUpdated");}
 public void deactivate(UUID id){Book b=load(id).deactivate();books.save(b);events.publish(new BookChanged(UUID.randomUUID(),b.id(),"BookDeactivated",Instant.now()));}
 public View findActive(UUID id){Book b=load(id);if(!b.active())throw new BookNotFoundException(id);return view(b);} public Page search(Search q){var p=books.search(q.query().trim(),q.page(),q.size());return new Page(p.content().stream().map(this::view).toList(),p.page(),p.size(),p.totalElements(),p.totalPages());}
 private Book load(UUID id){return books.find(id).orElseThrow(()->new BookNotFoundException(id));} private View change(Book b,String type){books.save(b);events.publish(new BookChanged(UUID.randomUUID(),b.id(),type,Instant.now()));return view(b);} private View view(Book b){return new View(b.id(),b.title(),b.description(),b.author(),b.imageUrl(),b.active());} }
