package edu.pe.cibertec.saibm.catalog.inventory;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import edu.pe.cibertec.saibm.catalog.web.CatalogExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InventoryController.class)
@Import(CatalogExceptionHandler.class)
class InventoryControllerApiTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private InventoryService service;

    @Test
    void holdRequiresIdempotencyKeyAndReturnsStoredOperation() throws Exception {
        when(service.hold(eq(7), any(), eq("hold-1")))
                .thenReturn(new InventoryResponse(12L, "hold-1", 7, 2, InventoryState.HELD, Instant.parse("2030-01-01T00:00:00Z")));

        mvc.perform(post("/api/v1/inventory/7/hold")
                        .header("Idempotency-Key", "hold-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":2,\"expiresAt\":\"2030-01-01T00:00:00Z\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("HELD"))
                .andExpect(jsonPath("$.operationKey").value("hold-1"));
    }

    @Test
    void missingIdempotencyKeyIsProblemDetail() throws Exception {
        mvc.perform(post("/api/v1/inventory/7/hold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":2,\"expiresAt\":\"2030-01-01T00:00:00Z\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void commitConflictUsesProblemDetails() throws Exception {
        when(service.commit(7, "expired")).thenThrow(new InventoryConflictException("Hold expired"));

        mvc.perform(post("/api/v1/inventory/7/commit").header("Idempotency-Key", "expired"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(409));
    }
}
