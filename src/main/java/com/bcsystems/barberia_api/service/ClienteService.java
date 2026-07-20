package com.bcsystems.barberia_api.service;

import com.bcsystems.barberia_api.domain.Cliente;
import com.bcsystems.barberia_api.dto.ClienteDTO;
import com.bcsystems.barberia_api.repository.ClienteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public Page<ClienteDTO> findAll(Pageable pageable) {
        return clienteRepository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<ClienteDTO> findById(Integer id) {
        return clienteRepository.findById(id).map(this::toDTO);
    }

    @Transactional
    public ClienteDTO save(ClienteDTO dto) {
        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new RuntimeException("El nombre es obligatorio");
        }
        if (clienteRepository.existsByNombre(dto.getNombre())) {
            throw new RuntimeException("Ya existe un cliente con ese nombre");
        }
        if (dto.getTelefono() != null && !dto.getTelefono().trim().isEmpty()) {
            if (clienteRepository.existsByTelefono(dto.getTelefono())) {
                throw new RuntimeException("Ya existe un cliente con ese teléfono");
            }
        }
        Cliente cliente = toEntity(dto);
        Cliente saved = clienteRepository.save(cliente);
        return toDTO(saved);
    }

    @Transactional
    public ClienteDTO update(Integer id, ClienteDTO dto) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        if (!cliente.getNombre().equalsIgnoreCase(dto.getNombre()) && clienteRepository.existsByNombre(dto.getNombre())) {
            throw new RuntimeException("Ya existe un cliente con ese nombre");
        }
        if (dto.getTelefono() != null && !dto.getTelefono().trim().isEmpty()) {
            String telActual = cliente.getTelefono();
            if (telActual == null || !telActual.equals(dto.getTelefono())) {
                if (clienteRepository.existsByTelefono(dto.getTelefono())) {
                    throw new RuntimeException("Ya existe un cliente con ese teléfono");
                }
            }
        }
        cliente.setNombre(dto.getNombre());
        cliente.setTelefono(dto.getTelefono());
        return toDTO(clienteRepository.save(cliente));
    }

    @Transactional
    public void delete(Integer id) {
        if (!clienteRepository.existsById(id)) {
            throw new RuntimeException("Cliente no encontrado");
        }
        clienteRepository.deleteById(id);
    }

    private ClienteDTO toDTO(Cliente cliente) {
        return new ClienteDTO(
                cliente.getIdCliente(),
                cliente.getNombre(),
                cliente.getTelefono(),
                cliente.getFechaRegistro()
        );
    }

    private Cliente toEntity(ClienteDTO dto) {
        Cliente cliente = new Cliente();
        cliente.setNombre(dto.getNombre());
        cliente.setTelefono(dto.getTelefono());
        return cliente;
    }
}