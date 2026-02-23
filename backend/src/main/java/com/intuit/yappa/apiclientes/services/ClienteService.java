package com.intuit.yappa.apiclientes.services;

import com.intuit.yappa.apiclientes.exceptions.BadRequestException;
import com.intuit.yappa.apiclientes.exceptions.ResourceNotFoundException;
import com.intuit.yappa.apiclientes.models.dtos.ClienteDTO;
import com.intuit.yappa.apiclientes.models.entities.Cliente;
import com.intuit.yappa.apiclientes.repositories.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public List<ClienteDTO> getAllClientes() {
        return clienteRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClienteDTO getClienteById(Integer id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));
        return mapToDTO(cliente);
    }

    @Transactional(readOnly = true)
    public List<ClienteDTO> searchClientesByName(String name) {
        return clienteRepository.searchClientesByName(name).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClienteDTO createCliente(ClienteDTO clienteDTO) {
        validateUniqueness(clienteDTO, null);
        Cliente cliente = mapToEntity(clienteDTO);
        Cliente savedCliente = clienteRepository.save(cliente);
        return mapToDTO(savedCliente);
    }

    @Transactional
    public ClienteDTO updateCliente(Integer id, ClienteDTO clienteDTO) {
        Cliente existingCliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));

        validateUniqueness(clienteDTO, id);

        existingCliente.setNombre(clienteDTO.getNombre());
        existingCliente.setApellido(clienteDTO.getApellido());
        existingCliente.setTelefonoCelular(clienteDTO.getTelefonoCelular());
        existingCliente.setEmail(clienteDTO.getEmail());

        Cliente updatedCliente = clienteRepository.save(existingCliente);
        return mapToDTO(updatedCliente);
    }

    @Transactional
    public void deleteCliente(Integer id) {
        if (!clienteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cliente no encontrado con id: " + id);
        }
        clienteRepository.deleteById(id);
    }

    private void validateUniqueness(ClienteDTO clienteDTO, Integer idSeleccionado) {
        Optional<Cliente> byCuit = clienteRepository.findByCuit(clienteDTO.getCuit());
        if (byCuit.isPresent() && !byCuit.get().getId().equals(idSeleccionado)) {
            throw new BadRequestException("El CUIT ingresado ya se encuentra registrado.");
        }

        Optional<Cliente> byEmail = clienteRepository.findByEmail(clienteDTO.getEmail());
        if (byEmail.isPresent() && !byEmail.get().getId().equals(idSeleccionado)) {
            throw new BadRequestException("El email ingresado ya se encuentra registrado.");
        }
    }

    private ClienteDTO mapToDTO(Cliente cliente) {
        return ClienteDTO.builder()
                .id(cliente.getId())
                .nombre(cliente.getNombre())
                .apellido(cliente.getApellido())
                .razonSocial(cliente.getRazonSocial())
                .cuit(cliente.getCuit())
                .fechaNacimiento(cliente.getFechaNacimiento())
                .telefonoCelular(cliente.getTelefonoCelular())
                .email(cliente.getEmail())
                .fechaCreacion(cliente.getFechaCreacion())
                .fechaModificacion(cliente.getFechaModificacion())
                .build();
    }

    private Cliente mapToEntity(ClienteDTO dto) {
        return Cliente.builder()
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .razonSocial(dto.getRazonSocial())
                .cuit(dto.getCuit())
                .fechaNacimiento(dto.getFechaNacimiento())
                .telefonoCelular(dto.getTelefonoCelular())
                .email(dto.getEmail())
                .build();
    }
}
