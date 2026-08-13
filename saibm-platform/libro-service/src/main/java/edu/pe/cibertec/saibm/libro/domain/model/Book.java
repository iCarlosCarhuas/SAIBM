package edu.pe.cibertec.saibm.libro.domain.model;
import java.util.UUID;
import edu.pe.cibertec.saibm.libro.domain.exception.InvalidBookException;
public record Book(UUID id,String title,String description,String author,String imageUrl,boolean active) {
 public static Book create(UUID id,String title,String description,String author,String imageUrl){return new Book(id,required(title,"title"),text(description),required(author,"author"),text(imageUrl),true);}
 public Book update(String title,String description,String author,String imageUrl){return new Book(id,required(title,"title"),text(description),required(author,"author"),text(imageUrl),active);}
 public Book deactivate(){return new Book(id,title,description,author,imageUrl,false);}
 private static String required(String value,String field){if(value==null||value.isBlank())throw new InvalidBookException(field+" must not be blank");return value.trim();}
 private static String text(String value){return value==null?"":value.trim();}
}
