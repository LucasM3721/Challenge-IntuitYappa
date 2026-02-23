package com.intuit.yappa.apiclientes;

import com.intuit.yappa.apiclientes.exceptions.BadRequestException;
import com.intuit.yappa.apiclientes.exceptions.ResourceNotFoundException;
import com.intuit.yappa.apiclientes.models.dtos.ClienteDTO;
import com.intuit.yappa.apiclientes.models.entities.Cliente;
import com.intuit.yappa.apiclientes.repositories.ClienteRepository;
import com.intuit.yappa.apiclientes.services.ClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente cliente1;
    private ClienteDTO clienteDTO;

    @BeforeEach
    void setUp() {
        cliente1 = new Cliente();
        cliente1.setId(1);
        cliente1.setNombre("Juan");
        cliente1.setApellido("Perez");
        cliente1.setCuit("20-12345678-9");
        cliente1.setEmail("juan@example.com");
        cliente1.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        cliente1.setTelefonoCelular("1122334455");
        cliente1.setRazonSocial("RS Juan");

        clienteDTO = new ClienteDTO();
        clienteDTO.setNombre("JuanDTO");
        clienteDTO.setApellido("PerezDTO");
        clienteDTO.setCuit("20-12345678-9");
        clienteDTO.setEmail("juan@example.com");
        clienteDTO.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        clienteDTO.setTelefonoCelular("1122334455");
        clienteDTO.setRazonSocial("RS Juan");
    }

    // GetAll

    @Test
    void testGetAllClientes() {
        when(clienteRepository.findAll()).thenReturn(Arrays.asList(cliente1));
        List<ClienteDTO> result = clienteService.getAllClientes();
        assertEquals(1, result.size());
        assertEquals("Juan", result.get(0).getNombre());
    }

    @Test
    void testGetAllClientes_Empty() {
        when(clienteRepository.findAll()).thenReturn(Collections.emptyList());
        List<ClienteDTO> result = clienteService.getAllClientes();
        assertTrue(result.isEmpty());
    }

    // GetById

    @Test
    void testGetClienteById_Success() {
        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente1));
        ClienteDTO result = clienteService.getClienteById(1);
        assertEquals("Juan", result.getNombre());
        assertEquals("Perez", result.getApellido());
        assertEquals("20-12345678-9", result.getCuit());
    }

    @Test
    void testGetClienteById_NotFound() {
        when(clienteRepository.findById(2)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> clienteService.getClienteById(2));
    }

    // Search

    @Test
    void testSearchClientesByName_ReturnsResults() {
        when(clienteRepository.searchClientesByName("Juan")).thenReturn(Arrays.asList(cliente1));
        List<ClienteDTO> result = clienteService.searchClientesByName("Juan");
        assertEquals(1, result.size());
        assertEquals("Juan", result.get(0).getNombre());
    }

    @Test
    void testSearchClientesByName_NoResults() {
        when(clienteRepository.searchClientesByName("XYZ")).thenReturn(Collections.emptyList());
        List<ClienteDTO> result = clienteService.searchClientesByName("XYZ");
        assertTrue(result.isEmpty());
    }

    // Create

    @Test
    void testCreateCliente_Success() {
        when(clienteRepository.findByCuit(anyString())).thenReturn(Optional.empty());
        when(clienteRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente1);

        ClienteDTO result = clienteService.createCliente(clienteDTO);
        assertNotNull(result);
        assertEquals("Juan", result.getNombre());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    void testCreateCliente_DuplicateCuit() {
        when(clienteRepository.findByCuit(clienteDTO.getCuit())).thenReturn(Optional.of(cliente1));
        BadRequestException ex = assertThrows(BadRequestException.class, () -> clienteService.createCliente(clienteDTO));
        assertEquals("El CUIT ingresado ya se encuentra registrado.", ex.getMessage());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void testCreateCliente_DuplicateEmail() {
        when(clienteRepository.findByCuit(anyString())).thenReturn(Optional.empty());
        when(clienteRepository.findByEmail(clienteDTO.getEmail())).thenReturn(Optional.of(cliente1));
        BadRequestException ex = assertThrows(BadRequestException.class, () -> clienteService.createCliente(clienteDTO));
        assertEquals("El email ingresado ya se encuentra registrado.", ex.getMessage());
        verify(clienteRepository, never()).save(any());
    }

    // Update

    @Test
    void testUpdateCliente_Success() {
        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente1));
        when(clienteRepository.findByCuit(anyString())).thenReturn(Optional.of(cliente1)); // mismo cliente
        when(clienteRepository.findByEmail(anyString())).thenReturn(Optional.of(cliente1)); // mismo cliente
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente1);

        ClienteDTO result = clienteService.updateCliente(1, clienteDTO);
        assertNotNull(result);
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    void testUpdateCliente_NotFound() {
        when(clienteRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> clienteService.updateCliente(99, clienteDTO));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void testUpdateCliente_DuplicateCuitFromOtherCliente() {
        Cliente otroCliente = new Cliente();
        otroCliente.setId(2);
        otroCliente.setCuit("20-12345678-9");

        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente1));
        when(clienteRepository.findByCuit(clienteDTO.getCuit())).thenReturn(Optional.of(otroCliente));

        assertThrows(BadRequestException.class, () -> clienteService.updateCliente(1, clienteDTO));
    }

    @Test
    void testUpdateCliente_DuplicateEmailFromOtherCliente() {
        Cliente otroCliente = new Cliente();
        otroCliente.setId(2);
        otroCliente.setEmail("juan@example.com");

        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente1));
        when(clienteRepository.findByCuit(anyString())).thenReturn(Optional.empty());
        when(clienteRepository.findByEmail(clienteDTO.getEmail())).thenReturn(Optional.of(otroCliente));

        assertThrows(BadRequestException.class, () -> clienteService.updateCliente(1, clienteDTO));
    }

    @Test
    void testUpdateCliente_NullRazonSocialAndCuit() {
        clienteDTO.setRazonSocial(null);
        clienteDTO.setCuit(null);

        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente1));
        when(clienteRepository.findByCuit(null)).thenReturn(Optional.empty());
        when(clienteRepository.findByEmail(anyString())).thenReturn(Optional.of(cliente1));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente1);

        ClienteDTO result = clienteService.updateCliente(1, clienteDTO);
        assertNotNull(result);
        assertEquals("RS Juan", result.getRazonSocial()); // no se pisó
        assertEquals("20-12345678-9", result.getCuit());  // no se pisó
    }

    // Delete

    @Test
    void testDeleteCliente_Success() {
        when(clienteRepository.existsById(1)).thenReturn(true);
        doNothing().when(clienteRepository).deleteById(1);

        assertDoesNotThrow(() -> clienteService.deleteCliente(1));
        verify(clienteRepository, times(1)).deleteById(1);
    }

    @Test
    void testDeleteCliente_NotFound() {
        when(clienteRepository.existsById(99)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> clienteService.deleteCliente(99));
        verify(clienteRepository, never()).deleteById(any());
    }
}
