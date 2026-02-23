package com.intuit.yappa.apiclientes;

import com.intuit.yappa.apiclientes.exceptions.BadRequestException;
import com.intuit.yappa.apiclientes.exceptions.GlobalExceptionHandler;
import com.intuit.yappa.apiclientes.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GlobalExceptionHandlerTest {

    @RestController
    static class StubController {
        @GetMapping("/test/not-found")
        public void notFound() { throw new ResourceNotFoundException("not found"); }

        @GetMapping("/test/bad-request")
        public void badRequest() { throw new BadRequestException("bad request"); }

        @GetMapping("/test/server-error")
        public void serverError() { throw new RuntimeException("unexpected"); }
    }

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new StubController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testHandleResourceNotFoundException() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("not found"));
    }

    @Test
    void testHandleBadRequestException() throws Exception {
        mockMvc.perform(get("/test/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("bad request"));
    }

    @Test
    void testHandleGlobalException() throws Exception {
        mockMvc.perform(get("/test/server-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Ha ocurrido un error interno en el servidor"));
    }
}
