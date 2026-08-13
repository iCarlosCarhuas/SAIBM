package edu.pe.cibertec.saibm.usuario.rest;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import edu.pe.cibertec.saibm.usuario.application.port.in.UserUseCase;
import edu.pe.cibertec.saibm.usuario.domain.exception.UserNotFoundException;
import edu.pe.cibertec.saibm.usuario.infrastructure.in.rest.*;

@WebMvcTest(UserController.class)
@Import(UserExceptionHandler.class)
class UserRestTest {

    @Autowired MockMvc mvc;
    @MockBean UserUseCase use;
    @MockBean TransitionalWriteAuthorizer auth;
    final UUID id = UUID.randomUUID();
    final UserUseCase.UserView view = new UserUseCase.UserView(id, "Ana", "Torres", "12345678",
            "ana@example.com", true, "42");

    @Test
    void ownerReadExposesPiiAllowlistOnly() throws Exception {
        when(auth.actor(any())).thenReturn(new UserUseCase.Actor(id.toString(), false));
        when(use.find(eq(id), any())).thenReturn(view);

        mvc.perform(get("/api/v1/users/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.role").doesNotExist())
                .andExpect(jsonPath("$.membershipId").doesNotExist());
    }

    @Test
    void foreignReadReturnsNotFound() throws Exception {
        when(auth.actor(any())).thenReturn(new UserUseCase.Actor("other", false));
        when(use.find(eq(id), any())).thenThrow(new UserNotFoundException(id));

        mvc.perform(get("/api/v1/users/" + id)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void profileWriteRejectsAuthenticationFieldsAndUnsignedRequests() throws Exception {
        doThrow(new WriteAuthorizationException()).when(auth).require(any());
        mvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Ana\",\"lastName\":\"Torres\",\"dni\":\"12345678\","
                        + "\"email\":\"ana@example.com\",\"password\":\"secret\"}"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403));
        verifyNoInteractions(use);
    }

    @Test
    void signedProfileUpdateReturnsLifecycleView() throws Exception {
        reset(auth);
        when(auth.actor(any())).thenReturn(new UserUseCase.Actor(id.toString(), false));
        when(use.update(eq(id), any(), any(), any())).thenReturn(view);

        mvc.perform(put("/api/v1/users/" + id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Ana\",\"lastName\":\"Torres\",\"dni\":\"12345678\","
                        + "\"email\":\"ana@example.com\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.active").value(true));
    }
}
