package com.bcsystems.barberia_api.service;

import com.bcsystems.barberia_api.domain.Cita;
import com.bcsystems.barberia_api.domain.CitaDetails;
import com.bcsystems.barberia_api.domain.Cliente;
import com.bcsystems.barberia_api.domain.Empleado;
import com.bcsystems.barberia_api.domain.Servicio;
import com.bcsystems.barberia_api.domain.Venta;
import com.bcsystems.barberia_api.domain.en.EstadoCita;
import com.bcsystems.barberia_api.dto.CitaDetailsDTO;
import com.bcsystems.barberia_api.dto.CitaDTO;
import com.bcsystems.barberia_api.repository.CitaRepository;
import com.bcsystems.barberia_api.repository.ClienteRepository;
import com.bcsystems.barberia_api.repository.EmpleadoRepository;
import com.bcsystems.barberia_api.repository.ServicioRepository;
import com.bcsystems.barberia_api.repository.VentaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CitaService {

    private final CitaRepository citaRepository;
    private final ClienteRepository clienteRepository;
    private final EmpleadoRepository empleadoRepository;
    private final ServicioRepository servicioRepository;
    private final VentaRepository ventaRepository;

    public CitaService(CitaRepository citaRepository, ClienteRepository clienteRepository,
                       EmpleadoRepository empleadoRepository, ServicioRepository servicioRepository,
                       VentaRepository ventaRepository) {
        this.citaRepository = citaRepository;
        this.clienteRepository = clienteRepository;
        this.empleadoRepository = empleadoRepository;
        this.servicioRepository = servicioRepository;
        this.ventaRepository = ventaRepository;
    }

    @Transactional(readOnly = true)
    public Page<CitaDTO> findAll(Pageable pageable) {
        return citaRepository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<CitaDTO> findById(Integer id) {
        return citaRepository.findById(id).map(this::toDTO);
    }

    @Transactional
    public CitaDTO save(CitaDTO dto) {
        Cita cita = toEntity(dto);
        System.out.println("Detalles: " + dto.getDetalles());
        Cita saved = citaRepository.save(cita);
        return toDTO(saved);
    }

    @Transactional
    public CitaDTO update(Integer id, CitaDTO dto) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        if (dto.getIdCliente() != null) {
            Cliente cliente = clienteRepository.findById(dto.getIdCliente())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            cita.setCliente(cliente);
        }
        if (dto.getIdEmpleado() != null) {
            Empleado empleado = empleadoRepository.findById(dto.getIdEmpleado())
                    .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
            cita.setEmpleado(empleado);
        }
        if (dto.getFechaInicio() != null) {
            cita.setFechaInicio(dto.getFechaInicio());
        }
        if (dto.getFechaFin() != null) {
            cita.setFechaFin(dto.getFechaFin());
        }
        if (dto.getEstado() != null) {
            cita.setEstado(dto.getEstado());
        }

        if (dto.getDetalles() != null) {
            cita.getDetalles().clear();
            for (CitaDetailsDTO detailDTO : dto.getDetalles()) {
                CitaDetails detail = new CitaDetails();
                detail.setCita(cita);
                if (detailDTO.getIdServicio() != null) {
                    Servicio servicio = servicioRepository.findById(detailDTO.getIdServicio())
                            .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
                    detail.setServicio(servicio);
                }
                detail.setPrecio(detailDTO.getPrecio());
                cita.getDetalles().add(detail);
            }
        }

        return toDTO(citaRepository.save(cita));
    }

    @Transactional
    public void delete(Integer id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        cita.setEstado(EstadoCita.CANCELADA);
        citaRepository.save(cita);

        List<Venta> ventas = ventaRepository.findByCitaIdCita(id, Pageable.unpaged()).getContent();
        for (Venta venta : ventas) {
            venta.setCita(null);
            ventaRepository.save(venta);
        }
    }

    @Transactional(readOnly = true)
    public Page<CitaDTO> findByEmpleado(Integer idEmpleado, Pageable pageable) {
        return citaRepository.findByEmpleadoIdEmpleado(idEmpleado, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<CitaDTO> findByCliente(Integer idCliente, Pageable pageable) {
        return citaRepository.findByClienteIdCliente(idCliente, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<CitaDTO> findByEstado(EstadoCita estado, Pageable pageable) {
        return citaRepository.findByEstado(estado, pageable).map(this::toDTO);
    }

    private CitaDTO toDTO(Cita cita) {
        List<CitaDetailsDTO> detallesDTO = new ArrayList<>();
        if (cita.getDetalles() != null) {
            detallesDTO = cita.getDetalles().stream()
                    .map(this::toDetailsDTO)
                    .collect(Collectors.toList());
        }

        CitaDTO dto = new CitaDTO();
        dto.setIdCita(cita.getIdCita());
        dto.setIdCliente(cita.getCliente() != null ? cita.getCliente().getIdCliente() : null);
        dto.setNombreCliente(cita.getCliente() != null ? cita.getCliente().getNombre() : null);
        dto.setIdEmpleado(cita.getEmpleado() != null ? cita.getEmpleado().getIdEmpleado() : null);
        dto.setNombreEmpleado(cita.getEmpleado() != null ? cita.getEmpleado().getNombre() : null);
        dto.setFechaInicio(cita.getFechaInicio());
        dto.setFechaFin(cita.getFechaFin());
        dto.setEstado(cita.getEstado());
        dto.setDetalles(detallesDTO);
        return dto;
    }

    private CitaDetailsDTO toDetailsDTO(CitaDetails details) {
        CitaDetailsDTO dto = new CitaDetailsDTO();
        dto.setIdCitaDetails(details.getIdCitaDetails());
        dto.setIdCita(details.getCita() != null ? details.getCita().getIdCita() : null);
        dto.setIdServicio(details.getServicio() != null ? details.getServicio().getIdServicio() : null);
        dto.setNombreServicio(details.getServicio() != null ? details.getServicio().getNombre() : null);
        dto.setPrecio(details.getPrecio());
        return dto;
    }

    private Cita toEntity(CitaDTO dto) {
        Cita cita = new Cita();
        
        if (dto.getIdCliente() != null) {
            Cliente cliente = clienteRepository.findById(dto.getIdCliente())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            cita.setCliente(cliente);
        }
        if (dto.getIdEmpleado() != null) {
            Empleado empleado = empleadoRepository.findById(dto.getIdEmpleado())
                    .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
            cita.setEmpleado(empleado);
        }
        cita.setFechaInicio(dto.getFechaInicio());
        cita.setFechaFin(dto.getFechaFin());
        cita.setEstado(dto.getEstado() != null ? dto.getEstado() : EstadoCita.PENDIENTE);

        if (dto.getDetalles() != null && !dto.getDetalles().isEmpty()) {
            List<CitaDetails> detalles = new ArrayList<>();
            for (CitaDetailsDTO detailDTO : dto.getDetalles()) {
                CitaDetails detail = new CitaDetails();
                detail.setCita(cita);
                if (detailDTO.getIdServicio() != null) {
                    Servicio servicio = servicioRepository.findById(detailDTO.getIdServicio())
                            .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
                    detail.setServicio(servicio);
                }
                detail.setPrecio(detailDTO.getPrecio());
                detalles.add(detail);
            }
            cita.setDetalles(detalles);
        }

        return cita;
    }
}