package com.bcsystems.barberia_api.service;

import com.bcsystems.barberia_api.domain.Caja;
import com.bcsystems.barberia_api.domain.Configuracion;
import com.bcsystems.barberia_api.domain.MovimientoCaja;
import com.bcsystems.barberia_api.dto.CajaDTO;
import com.bcsystems.barberia_api.dto.CorteCompletoDTO;
import com.bcsystems.barberia_api.dto.CortePreviewDTO;
import com.bcsystems.barberia_api.dto.MovimientoCajaDTO;
import com.bcsystems.barberia_api.repository.CajaRepository;
import com.bcsystems.barberia_api.repository.ConfiguracionRepository;
import com.bcsystems.barberia_api.repository.MovimientoCajaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CajaService {

    private final CajaRepository cajaRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;
    private final ConfiguracionRepository configuracionRepository;
    private final ComisionService comisionService;
    private final CorteService corteService;

    public CajaService(CajaRepository cajaRepository,
                       MovimientoCajaRepository movimientoCajaRepository,
                       ConfiguracionRepository configuracionRepository,
                       ComisionService comisionService,
                       CorteService corteService) {
        this.cajaRepository = cajaRepository;
        this.movimientoCajaRepository = movimientoCajaRepository;
        this.configuracionRepository = configuracionRepository;
        this.comisionService = comisionService;
        this.corteService = corteService;
    }

    @Transactional(readOnly = true)
    public List<CajaDTO> findAll() {
        return cajaRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CajaDTO> findAbiertas() {
        return cajaRepository.findByEstado("ABIERTA").stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CajaDTO findById(Integer id) {
        return cajaRepository.findById(id).map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Caja no encontrada"));
    }

    @Transactional
    public CajaDTO save(CajaDTO dto) {
        Caja caja = new Caja();
        caja.setNombre(dto.getNombre());
        caja.setSaldoInicial(0.0);
        caja.setSaldoActual(0.0);
        caja.setEstado("CERRADA");
        return toDTO(cajaRepository.save(caja));
    }

    @Transactional
    public CajaDTO apertura(Integer id, Double saldoInicial) {
        Caja caja = cajaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Caja no encontrada"));
        if ("ABIERTA".equals(caja.getEstado())) {
            throw new RuntimeException("La caja ya esta abierta");
        }
        caja.setSaldoInicial(saldoInicial);
        caja.setSaldoActual(saldoInicial);
        caja.setFechaApertura(LocalDateTime.now());
        caja.setFechaCierre(null);
        caja.setEstado("ABIERTA");
        cajaRepository.save(caja);

        MovimientoCaja mov = new MovimientoCaja();
        mov.setCaja(caja);
        mov.setTipo("APERTURA");
        mov.setMonto(saldoInicial);
        mov.setMotivo("Apertura de caja");
        mov.setFecha(LocalDateTime.now());
        movimientoCajaRepository.save(mov);

        return toDTO(caja);
    }

    @Transactional
    public CajaDTO cierre(Integer id) {
        Caja caja = cajaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Caja no encontrada"));
        if (!"ABIERTA".equals(caja.getEstado())) {
            throw new RuntimeException("La caja no esta abierta");
        }

        LocalDateTime fechaApertura = caja.getFechaApertura();
        LocalDateTime ahora = LocalDateTime.now();

        // Generar corte completo automaticamente
        CorteCompletoDTO corteDTO = comisionService.generarCorteCompleto(fechaApertura, ahora);

        Double ingresosCaja = movimientoCajaRepository.sumByCajaAndTipoAndFechaCorteIsNull(id, "INGRESO");
        Double egresosCaja = movimientoCajaRepository.sumByCajaAndTipoAndFechaCorteIsNull(id, "EGRESO");
        Double ventasContado = movimientoCajaRepository.sumByCajaAndTipoAndFechaCorteIsNull(id, "VENTA_CONTADO");
        Double ventasCredito = movimientoCajaRepository.sumByCajaAndTipoAndFechaCorteIsNull(id, "VENTA_CREDITO");

        corteDTO.setTotalFondoCaja(caja.getSaldoInicial());
        corteDTO.setTotalIngresosCaja(ingresosCaja != null ? ingresosCaja : 0.0);
        corteDTO.setTotalEgresosCaja(egresosCaja != null ? egresosCaja : 0.0);
        double totalVentasCaja = (ventasContado != null ? ventasContado : 0.0) + (ventasCredito != null ? ventasCredito : 0.0);
        corteDTO.setSaldoEsperado(caja.getSaldoInicial() + totalVentasCaja + corteDTO.getTotalIngresosCaja() - corteDTO.getTotalEgresosCaja());
        corteDTO.setSaldoFinal(caja.getSaldoActual());

        corteService.guardarCorte(corteDTO);

        // Marcar movimientos de caja como consumidos por el corte
        List<MovimientoCaja> movimientosCaja = movimientoCajaRepository.findByCajaIdCajaAndFechaCorteIsNull(id);
        for (MovimientoCaja m : movimientosCaja) {
            m.setFechaCorte(ahora);
        }
        movimientoCajaRepository.saveAll(movimientosCaja);

        caja.setFechaCierre(ahora);
        caja.setEstado("CERRADA");
        return toDTO(cajaRepository.save(caja));
    }

    @Transactional
    public MovimientoCajaDTO ingresarEfectivo(Integer idCaja, Double monto, String motivo) {
        Caja caja = cajaRepository.findById(idCaja)
                .orElseThrow(() -> new RuntimeException("Caja no encontrada"));
        if (!"ABIERTA".equals(caja.getEstado())) {
            throw new RuntimeException("La caja no esta abierta");
        }
        caja.setSaldoActual(caja.getSaldoActual() + monto);
        cajaRepository.save(caja);

        MovimientoCaja mov = new MovimientoCaja();
        mov.setCaja(caja);
        mov.setTipo("INGRESO");
        mov.setMonto(monto);
        mov.setMotivo(motivo);
        mov.setFecha(LocalDateTime.now());
        return toMovimientoDTO(movimientoCajaRepository.save(mov));
    }

    @Transactional
    public MovimientoCajaDTO retirarEfectivo(Integer idCaja, Double monto, String motivo) {
        Caja caja = cajaRepository.findById(idCaja)
                .orElseThrow(() -> new RuntimeException("Caja no encontrada"));
        if (!"ABIERTA".equals(caja.getEstado())) {
            throw new RuntimeException("La caja no esta abierta");
        }
        if (caja.getSaldoActual() < monto) {
            throw new RuntimeException("Saldo insuficiente en caja");
        }
        caja.setSaldoActual(caja.getSaldoActual() - monto);
        cajaRepository.save(caja);

        MovimientoCaja mov = new MovimientoCaja();
        mov.setCaja(caja);
        mov.setTipo("EGRESO");
        mov.setMonto(monto);
        mov.setMotivo(motivo);
        mov.setFecha(LocalDateTime.now());
        return toMovimientoDTO(movimientoCajaRepository.save(mov));
    }

    @Transactional(readOnly = true)
    public CortePreviewDTO previewCorte(Integer idCaja) {
        Caja caja = cajaRepository.findById(idCaja)
                .orElseThrow(() -> new RuntimeException("Caja no encontrada"));

        CortePreviewDTO preview = new CortePreviewDTO();
        preview.setSaldoInicial(caja.getSaldoInicial());

        Double ventasContado = movimientoCajaRepository.sumByCajaAndTipoAndFechaCorteIsNull(idCaja, "VENTA_CONTADO");
        Double ventasCredito = movimientoCajaRepository.sumByCajaAndTipoAndFechaCorteIsNull(idCaja, "VENTA_CREDITO");
        preview.setTotalVentasContado(ventasContado != null ? ventasContado : 0.0);
        preview.setTotalVentasCredito(ventasCredito != null ? ventasCredito : 0.0);
        preview.setTotalVentas(preview.getTotalVentasContado() + preview.getTotalVentasCredito());

        Double ingresos = movimientoCajaRepository.sumByCajaAndTipoAndFechaCorteIsNull(idCaja, "INGRESO");
        preview.setTotalIngresos(ingresos != null ? ingresos : 0.0);

        Double egresos = movimientoCajaRepository.sumByCajaAndTipoAndFechaCorteIsNull(idCaja, "EGRESO");
        preview.setTotalEgresos(egresos != null ? egresos : 0.0);

        preview.setSaldoEsperado(caja.getSaldoInicial() + preview.getTotalVentas() + preview.getTotalIngresos() - preview.getTotalEgresos());
        preview.setSaldoActual(caja.getSaldoActual());
        preview.setDiferencia(preview.getSaldoEsperado() - caja.getSaldoActual());
        return preview;
    }

    @Transactional(readOnly = true)
    public List<MovimientoCajaDTO> getMovimientos(Integer idCaja) {
        return movimientoCajaRepository.findByCajaIdCajaOrderByFechaDesc(idCaja).stream()
                .map(this::toMovimientoDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Double getFondoDefault() {
        return configuracionRepository.findByClave("fondo_caja_default")
                .map(c -> Double.parseDouble(c.getValor()))
                .orElse(500.0);
    }

    private CajaDTO toDTO(Caja caja) {
        CajaDTO dto = new CajaDTO();
        dto.setIdCaja(caja.getIdCaja());
        dto.setNombre(caja.getNombre());
        dto.setSaldoInicial(caja.getSaldoInicial());
        dto.setSaldoActual(caja.getSaldoActual());
        dto.setFechaApertura(caja.getFechaApertura());
        dto.setFechaCierre(caja.getFechaCierre());
        dto.setEstado(caja.getEstado());
        return dto;
    }

    private MovimientoCajaDTO toMovimientoDTO(MovimientoCaja m) {
        MovimientoCajaDTO dto = new MovimientoCajaDTO();
        dto.setIdMovimiento(m.getIdMovimiento());
        dto.setIdCaja(m.getCaja() != null ? m.getCaja().getIdCaja() : null);
        dto.setNombreCaja(m.getCaja() != null ? m.getCaja().getNombre() : null);
        dto.setTipo(m.getTipo());
        dto.setMonto(m.getMonto());
        dto.setMotivo(m.getMotivo());
        dto.setFecha(m.getFecha());
        dto.setFechaCorte(m.getFechaCorte());
        dto.setIdVenta(m.getVenta() != null ? m.getVenta().getIdVenta() : null);
        return dto;
    }
}
