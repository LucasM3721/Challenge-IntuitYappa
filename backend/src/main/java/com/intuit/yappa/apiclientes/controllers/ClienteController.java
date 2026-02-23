package com.intuit.yappa.apiclientes.controllers;

import com.intuit.yappa.apiclientes.models.dtos.ClienteDTO;
import com.intuit.yappa.apiclientes.services.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "API para ABM de Clientes - Challenge Intuit / Yappa")
@CrossOrigin(origins = "*")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    @Operation(summary = "Obtiene todos los clientes")
    public ResponseEntity<List<ClienteDTO>> getAll() {
        return ResponseEntity.ok(clienteService.getAllClientes());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un cliente por su ID")
    public ResponseEntity<ClienteDTO> getClienteById(
            @Parameter(description = "ID del cliente", required = true) @PathVariable Integer id) {
        return ResponseEntity.ok(clienteService.getClienteById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Busqueda por nombre (caracteres centrales) mediante Stored Procedure")
    public ResponseEntity<List<ClienteDTO>> search(@RequestParam String name) {
        return ResponseEntity.ok(clienteService.searchClientesByName(name));
    }

    @PostMapping
    @Operation(summary = "Crea un nuevo cliente")
    public ResponseEntity<ClienteDTO> create(@Valid @RequestBody ClienteDTO clienteDTO) {
        ClienteDTO created = clienteService.createCliente(clienteDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un cliente")
    public ResponseEntity<ClienteDTO> updateCliente(
            @Parameter(description = "ID del cliente a actualizar", required = true) @PathVariable Integer id,
            @Valid @RequestBody ClienteDTO clienteDTO) {
        return ResponseEntity.ok(clienteService.updateCliente(id, clienteDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un cliente")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        clienteService.deleteCliente(id);
        return ResponseEntity.noContent().build();
    }
}
