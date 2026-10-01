package com.bcsystems.barberia_api.service;

import com.bcsystems.barberia_api.domain.Cita;
import com.bcsystems.barberia_api.domain.CitaDetails;
import com.bcsystems.barberia_api.domain.Cliente;
import com.bcsystems.barberia_api.domain.Empleado;
import com.bcsystems.barberia_api.domain.Servicio;
import com.bcsystems.barberia_api.domain.Venta;
import com.bcsystems.barberia_api.domain.VentaDetalle;
import com.bcsystems.barberia_api.domain.en.EstadoCita;
import com.bcsystems.barberia_api.dto.CitaDetailsDTO;
import com.bcsystems.barberia_api.dto.CitaDTO;
import com.bcsystems.barberia_api.repository.CitaRepository;
import com.bcsystems.barberia_api.repository.ClienteRepository;
import com.bcsystems.barberia_api.repository.EmpleadoRepository;
import com.bcsystems.barberia_api.repository.ServicioRepository;
import com.bcsystems.barberia_api.repository.VentaRepository;
import com.bcsystems.barberia_api.service.WhatsAppService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final WhatsAppService whatsAppService;

    public CitaService(CitaRepository citaRepository, ClienteRepository clienteRepository,
                       EmpleadoRepository empleadoRepository, ServicioRepository servicioRepository,
                       VentaRepository ventaRepository, WhatsAppService whatsAppService) {
        this.citaRepository = citaRepository;
        this.clienteRepository = clienteRepository;
        this.empleadoRepository = empleadoRepository;
        this.servicioRepository = servicioRepository;
        this.ventaRepository = ventaRepository;
        this.whatsAppService = whatsAppService;
    }

    /**
     * Marca como VENCIDAS las citas PENDIENTE cuya hora de fin ya paso.
     * Se ejecuta cada minuto en el servidor.
     */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void marcarCitasVencidas() {
        try {
            doMarcarVencidas();
        } catch (Exception e) {
            System.out.println("Error marcando citas vencidas: " + e.getMessage());
        }
    }

    /** Logica compartida por el job y por la lectura del listado. */
    private int doMarcarVencidas() {
        List<Cita> vencidas = citaRepository.findByEstadoAndFechaFinBefore(EstadoCita.PENDIENTE, LocalDateTime.now());
        if (vencidas.isEmpty()) {
            return 0;
        }
        for (Cita cita : vencidas) {
            cita.setEstado(EstadoCita.VENCIDA);
        }
        citaRepository.saveAll(vencidas);
        return vencidas.size();
    }

    @Transactional
    public Page<CitaDTO> findAll(Pageable pageable) {
        doMarcarVencidas();
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
        enviarConfirmacionWhatsApp(saved);
        return toDTO(saved);
    }

    private void enviarConfirmacionWhatsApp(Cita cita) {
        try {
            Cliente cliente = cita.getCliente();
            if (cliente == null || cliente.getTelefono() == null) return;

            Empleado empleado = cita.getEmpleado();
            List<String> servicios = new ArrayList<>();
            double total = 0;
            if (cita.getDetalles() != null) {
                for (CitaDetails det : cita.getDetalles()) {
                    if (det.getServicio() != null) {
                        servicios.add(det.getServicio().getNombre());
                    }
                    total += det.getPrecio() != null ? det.getPrecio() : 0;
                }
            }

            whatsAppService.sendAppointmentConfirmation(
                cliente.getTelefono(),
                cliente.getNombre(),
                empleado != null ? empleado.getNombre() : "Barberia",
                cita.getFechaInicio(),
                cita.getFechaFin(),
                servicios,
                total
            );
        } catch (Exception e) {
            System.err.println("Error al enviar confirmacion WhatsApp: " + e.getMessage());
        }
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
        
        // Si la cita se marca como COMPLETADA, crear una venta automática con los servicios
        boolean wasCompleted = "COMPLETADA".equals(cita.getEstado().name());
        if (dto.getEstado() != null && !wasCompleted && "COMPLETADA".equals(dto.getEstado().name())) {
            cita.setEstado(dto.getEstado());
            // Guardar la cita primero
            cita = citaRepository.save(cita);
            // Verificar si ya existe una venta para esta cita antes de crear
            List<Venta> ventasExistentes = ventaRepository.findByCitaIdCita(cita.getIdCita(), org.springframework.data.domain.Pageable.unpaged()).getContent();
            if (ventasExistentes.isEmpty()) {
                // Crear la venta automáticamente con los servicios de la cita
                crearVentaDesdeCita(cita);
            }
        } else if (dto.getEstado() != null) {
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
    
    private void crearVentaDesdeCita(Cita cita) {
        // Verificar si ya existe una venta para esta cita
        List<Venta> ventasExistentes = ventaRepository.findByCitaIdCita(cita.getIdCita(), org.springframework.data.domain.Pageable.unpaged()).getContent();
        if (!ventasExistentes.isEmpty()) {
            return; // Ya tiene una venta asociada
        }
        
        // Crear la venta con los servicios de la cita
        Venta venta = new Venta();
        venta.setCita(cita);
        
        List<VentaDetalle> detalles = new ArrayList<>();
        if (cita.getDetalles() != null && !cita.getDetalles().isEmpty()) {
            for (CitaDetails citaDetail : cita.getDetalles()) {
                VentaDetalle vd = new VentaDetalle();
                vd.setVenta(venta);
                vd.setServicio(citaDetail.getServicio());
                vd.setCantidad(1);
                vd.setPrecio(citaDetail.getPrecio());
                vd.setComisionPagada(false);
                detalles.add(vd);
            }
        }
        venta.setDetalles(detalles);
        
        // Calcular total
        double total = detalles.stream()
                .mapToDouble(d -> d.getPrecio() * (d.getCantidad() != null ? d.getCantidad() : 1))
                .sum();
        venta.setTotal(total);
        
        ventaRepository.save(venta);
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