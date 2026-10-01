package com.bcsystems.barberia_api.service;

import com.bcsystems.barberia_api.domain.Caja;
import com.bcsystems.barberia_api.domain.Configuracion;
import com.bcsystems.barberia_api.domain.MovimientoCaja;
import com.bcsystems.barberia_api.dto.CajaDTO;
import com.bcsystems.barberia_api.dto.CorteCompletoDTO;
import com.bcsystems.barberia_api.dto.CortePreviewDTO;
import com.bcsystems.barberia_api.dto.MetodoPagoResumenDTO;
import com.bcsystems.barberia_api.dto.MovimientoCajaDTO;
import com.bcsystems.barberia_api.repository.CajaRepository;
import com.bcsystems.barberia_api.repository.ConfiguracionRepository;
import com.bcsystems.barberia_api.repository.MovimientoCajaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        return cierre(id, null, null);
    }

    /**
     * Cierra la caja guardando el corte completo.
     * @param conteo conteo real declarado por el cajero por metodo de pago (puede ser null)
     * @param usuario usuario que realizo el corte
     */
    @Transactional
    public CajaDTO cierre(Integer id, Map<String, Double> conteo, String usuario) {
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

        corteDTO.setIdCaja(caja.getIdCaja());
        corteDTO.setNombreCaja(caja.getNombre());
        corteDTO.setUsuario(usuario);
        corteDTO.setTotalFondoCaja(caja.getSaldoInicial());
        corteDTO.setTotalIngresosCaja(ingresosCaja != null ? ingresosCaja : 0.0);
        corteDTO.setTotalEgresosCaja(egresosCaja != null ? egresosCaja : 0.0);

        // El saldo esperado del cajon = fondo + efectivo cobrado - cambio + ingresos - egresos
        double efectivoVentas = sumaMovimiento(id, "VENTA_EFECTIVO");
        double anulacionesEfectivo = sumaMovimiento(id, "ANULACION_EFECTIVO");
        double cambios = sumaMovimiento(id, "CAMBIO");
        double efectivoNeto = Math.max(0, efectivoVentas - anulacionesEfectivo - cambios);

        // Desglose por metodo de pago que queda guardado en el corte
        corteDTO.setTotalEfectivo(efectivoNeto);
        corteDTO.setTotalTarjeta(Math.max(0, sumaMovimiento(id, "VENTA_TARJETA")
                - sumaMovimiento(id, "ANULACION_TARJETA")));
        corteDTO.setTotalTransferencia(Math.max(0, sumaMovimiento(id, "VENTA_TRANSFERENCIA")
                - sumaMovimiento(id, "ANULACION_TRANSFERENCIA")));

        // Operaciones por metodo de pago (conteo del sistema)
        corteDTO.setEfectivoOperaciones(operaciones(id, "VENTA_EFECTIVO"));
        corteDTO.setTarjetaOperaciones(operaciones(id, "VENTA_TARJETA"));
        corteDTO.setTransferenciaOperaciones(operaciones(id, "VENTA_TRANSFERENCIA"));

        // Conteo real declarado por el cajero
        Double efectivoReal = conteoReal(conteo, "EFECTIVO");
        Double tarjetaReal = conteoReal(conteo, "TARJETA");
        Double transferenciaReal = conteoReal(conteo, "TRANSFERENCIA");
        corteDTO.setEfectivoReal(efectivoReal);
        corteDTO.setTarjetaReal(tarjetaReal);
        corteDTO.setTransferenciaReal(transferenciaReal);

        double totalSistema = corteDTO.getTotalEfectivo() + corteDTO.getTotalTarjeta()
                + corteDTO.getTotalTransferencia();
        if (efectivoReal != null || tarjetaReal != null || transferenciaReal != null) {
            double totalReal = (efectivoReal != null ? efectivoReal : 0)
                    + (tarjetaReal != null ? tarjetaReal : 0)
                    + (transferenciaReal != null ? transferenciaReal : 0);
            corteDTO.setTotalReal(totalReal);
            corteDTO.setDiferencia(totalReal - totalSistema);
        }

        corteDTO.setSaldoEsperado(caja.getSaldoInicial() + efectivoNeto
                + corteDTO.getTotalIngresosCaja() - corteDTO.getTotalEgresosCaja());
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

    private int operaciones(Integer idCaja, String tipo) {
        Long ops = movimientoCajaRepository.countByCajaAndTipoAndFechaCorteIsNull(idCaja, tipo);
        return ops != null ? ops.intValue() : 0;
    }

    private Double conteoReal(Map<String, Double> conteo, String metodo) {
        if (conteo == null) return null;
        Object valor = conteo.get(metodo);
        if (valor instanceof Number numero) return numero.doubleValue();
        return null;
    }

    private double sumaMovimiento(Integer idCaja, String tipo) {
        Double valor = movimientoCajaRepository.sumByCajaAndTipoAndFechaCorteIsNull(idCaja, tipo);
        return valor != null ? valor : 0.0;
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

        // Desglose por metodo de pago
        double anulEfectivo = sumaMovimiento(idCaja, "ANULACION_EFECTIVO");
        double anulTarjeta = sumaMovimiento(idCaja, "ANULACION_TARJETA");
        double anulTransferencia = sumaMovimiento(idCaja, "ANULACION_TRANSFERENCIA");

        double efectivo = Math.max(0, sumaMovimiento(idCaja, "VENTA_EFECTIVO") - anulEfectivo);
        double tarjeta = Math.max(0, sumaMovimiento(idCaja, "VENTA_TARJETA") - anulTarjeta);
        double transferencia = Math.max(0, sumaMovimiento(idCaja, "VENTA_TRANSFERENCIA") - anulTransferencia);

        preview.setTotalEfectivo(efectivo);
        preview.setTotalTarjeta(tarjeta);
        preview.setTotalTransferencia(transferencia);
        preview.setTotalVentas(efectivo + tarjeta + transferencia);

        // Campos legacy: todo es contado (ya no hay creditos)
        preview.setTotalVentasContado(preview.getTotalVentas());
        preview.setTotalVentasCredito(0.0);

        List<MetodoPagoResumenDTO> porMetodo = new ArrayList<>();
        porMetodo.add(resumenMetodo(idCaja, "EFECTIVO", efectivo));
        porMetodo.add(resumenMetodo(idCaja, "TARJETA", tarjeta));
        porMetodo.add(resumenMetodo(idCaja, "TRANSFERENCIA", transferencia));
        preview.setPorMetodo(porMetodo);

        preview.setTotalIngresos(sumaMovimiento(idCaja, "INGRESO"));
        preview.setTotalEgresos(sumaMovimiento(idCaja, "EGRESO"));

        // El cajon solo refleja el efectivo (neto de cambios y anulaciones)
        double cambios = sumaMovimiento(idCaja, "CAMBIO");
        preview.setSaldoEsperado(caja.getSaldoInicial() + Math.max(0, efectivo - cambios)
                + preview.getTotalIngresos() - preview.getTotalEgresos());
        preview.setSaldoActual(caja.getSaldoActual());
        preview.setDiferencia(preview.getSaldoEsperado() - caja.getSaldoActual());
        return preview;
    }

    private MetodoPagoResumenDTO resumenMetodo(Integer idCaja, String metodo, double total) {
        Long ops = movimientoCajaRepository.countByCajaAndTipoAndFechaCorteIsNull(idCaja, "VENTA_" + metodo);
        return new MetodoPagoResumenDTO(metodo, ops != null ? ops.intValue() : 0, total);
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
