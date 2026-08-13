package edu.pe.cibertec.saibm.libro.application.port.in;
import java.util.*;
public interface BookUseCase { View create(Create c); View update(UUID id,Update c); void deactivate(UUID id); View findActive(UUID id); Page search(Search q);
 record Create(String title,String description,String author,String imageUrl){} record Update(String title,String description,String author,String imageUrl){}
 record Search(String query,int page,int size){} record View(UUID id,String title,String description,String author,String imageUrl,boolean active){}
 record Page(List<View> content,int page,int size,long totalElements,int totalPages){} }
