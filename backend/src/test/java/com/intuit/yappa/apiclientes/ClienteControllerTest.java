package com.intuit.yappa.apiclientes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intuit.yappa.apiclientes.controllers.ClienteController;
import com.intuit.yappa.apiclientes.exceptions.GlobalExceptionHandler;
import com.intuit.yappa.apiclientes.exceptions.ResourceNotFoundException;
import com.intuit.yappa.apiclientes.models.dtos.ClienteDTO;
import com.intuit.yappa.apiclientes.services.ClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClienteController.class)
@Import(GlobalExceptionHandler.class)
public class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClienteService clienteService;

    private ClienteDTO clienteDTO;

    @BeforeEach
    void setUp() {
        clienteDTO = ClienteDTO.builder()
                .id(1)
                .nombre("Juan")
                .apellido("Perez")
                .cuit("20-12345678-9")
                .email("juan@example.com")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .telefonoCelular("1122334455")
                .razonSocial("RS Juan")
                .build();
    }

    @Test
    void testGetAll() throws Exception {
        when(clienteService.getAllClientes()).thenReturn(List.of(clienteDTO));

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Juan"));
    }

    @Test
    void testGetById_Success() throws Exception {
        when(clienteService.getClienteById(1)).thenReturn(clienteDTO);

        mockMvc.perform(get("/api/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cuit").value("20-12345678-9"));
    }

    @Test
    void testGetById_NotFound() throws Exception {
        when(clienteService.getClienteById(99)).thenThrow(new ResourceNotFoundException("Cliente no encontrado con id: 99"));

        mockMvc.perform(get("/api/clientes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente no encontrado con id: 99"));
    }

    @Test
    void testSearch() throws Exception {
        when(clienteService.searchClientesByName("Juan")).thenReturn(List.of(clienteDTO));

        mockMvc.perform(get("/api/clientes/search").param("name", "Juan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Juan"));
    }

    @Test
    void testCreate_Success() throws Exception {
        when(clienteService.createCliente(any(ClienteDTO.class))).thenReturn(clienteDTO);

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("juan@example.com"));
    }

    @Test
    void testCreate_ValidationError() throws Exception {
        ClienteDTO invalid = ClienteDTO.builder().nombre("").build();

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdate_Success() throws Exception {
        when(clienteService.updateCliente(eq(1), any(ClienteDTO.class))).thenReturn(clienteDTO);

        mockMvc.perform(put("/api/clientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apellido").value("Perez"));
    }

    @Test
    void testDelete_Success() throws Exception {
        doNothing().when(clienteService).deleteCliente(1);

        mockMvc.perform(delete("/api/clientes/1"))
                .andExpect(status().isNoContent());

        verify(clienteService, times(1)).deleteCliente(1);
    }

    @Test
    void testDelete_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Cliente no encontrado con id: 99"))
                .when(clienteService).deleteCliente(99);

        mockMvc.perform(delete("/api/clientes/99"))
                .andExpect(status().isNotFound());
    }
}
