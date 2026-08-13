package edu.pe.cibertec.saibm.catalog.book;

import edu.pe.cibertec.saibm.catalog.security.AdminAuthorizer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/books")
public class BookController {
    private final BookApplicationService service;
    private final BookWriteService writes;
    private final AdminAuthorizer authorizer;

    public BookController(BookApplicationService service, BookWriteService writes, AdminAuthorizer authorizer) {
        this.service = service;
        this.writes = writes;
        this.authorizer = authorizer;
    }

    @GetMapping
    public PageResponse<BookResponse> search(@RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new InvalidRequestException("page must be non-negative and size must be between 1 and 100");
        }
        return service.search(q.trim(), page, size);
    }

    @GetMapping("/{id}")
    public BookResponse find(@PathVariable Integer id) {
        return service.findActive(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookResponse create(@Valid @RequestBody BookCreateRequest request, HttpServletRequest httpRequest) {
        authorizer.require(httpRequest);
        return writes.create(request);
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}")
    public BookResponse update(@PathVariable Integer id, @Valid @RequestBody BookUpdateRequest request,
            HttpServletRequest httpRequest) {
        authorizer.require(httpRequest);
        return writes.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id, HttpServletRequest httpRequest) {
        authorizer.require(httpRequest);
        writes.delete(id);
    }
}
