package edu.pe.cibertec.saibm.catalog.book;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.List;

import edu.pe.cibertec.saibm.catalog.security.AdminAuthorizer;
import edu.pe.cibertec.saibm.catalog.security.AdminAuthorizationException;
import edu.pe.cibertec.saibm.catalog.web.CatalogExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@Import(CatalogExceptionHandler.class)
class BookControllerApiTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BookApplicationService service;

    @MockBean
    private BookWriteService writes;

    @MockBean
    private AdminAuthorizer authorizer;

    @Test
    void listReturnsDtoPageAndNeverPersistenceShape() throws Exception {
        BookResponse book = new BookResponse(7, "Java", "", "Ada", "", 4, 3);
        when(service.search("java", 0, 20)).thenReturn(new PageResponse<>(List.of(book), 0, 20, 1, 1));

        mvc.perform(get("/api/v1/books").param("q", " java "))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(7))
                .andExpect(jsonPath("$.content[0].availableStock").value(3))
                .andExpect(jsonPath("$.content[0].active").doesNotExist())
                .andExpect(jsonPath("$.page").value(0));
    }

    @Test
    void invalidBookPayloadReturnsProblemDetails() throws Exception {
        mvc.perform(post("/api/v1/books")
                        .header("X-SAIBM-Internal-Admin", "test-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"author\":\"\",\"initialStock\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void missingTransitionalAuthorizationFailsClosed() throws Exception {
        doThrow(new AdminAuthorizationException()).when(authorizer).require(any());

        mvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Java\",\"author\":\"Ada\",\"initialStock\":1}"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void authorizedCreateReturnsCreatedDto() throws Exception {
        when(writes.create(any())).thenReturn(new BookResponse(8, "Java", "", "Ada", "", 2, 2));

        mvc.perform(post("/api/v1/books")
                        .header("X-SAIBM-Internal-Admin", "test-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Java\",\"author\":\"Ada\",\"initialStock\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.totalStock").value(2));
    }
}
