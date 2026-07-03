package com.bcsystems.barberia_api.service;

import com.bcsystems.barberia_api.dto.DashboardDTO;
import com.bcsystems.barberia_api.repository.CitaRepository;
import com.bcsystems.barberia_api.repository.MovimientoInventarioRepository;
import com.bcsystems.barberia_api.repository.VentaDetalleRepository;
import com.bcsystems.barberia_api.repository.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

    private final VentaRepository ventaRepository;
    private final CitaRepository citaRepository;
    private final VentaDetalleRepository ventaDetalleRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    public DashboardService(VentaRepository ventaRepository, CitaRepository citaRepository,
                             VentaDetalleRepository ventaDetalleRepository,
                             MovimientoInventarioRepository movimientoInventarioRepository) {
        this.ventaRepository = ventaRepository;
        this.citaRepository = citaRepository;
        this.ventaDetalleRepository = ventaDetalleRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
    }

    @Transactional(readOnly = true)
    public DashboardDTO getDashboard() {
        LocalDate hoy = LocalDate.now();
        
        LocalDateTime inicioDia = hoy.atStartOfDay();
        LocalDateTime finDia = hoy.atTime(LocalTime.MAX);
        
        LocalDateTime inicioSemana = hoy.minusDays(7).atStartOfDay();
        LocalDateTime finDiaSemana = hoy.atTime(LocalTime.MAX);

        Double ventasDia = ventaRepository.sumTotalByFechaBetween(inicioDia, finDia);
        Double ventasSemana = ventaRepository.sumTotalByFechaBetween(inicioSemana, finDiaSemana);
        LocalDate primerDiaMes = hoy.withDayOfMonth(1);
        Double ventasMes = ventaRepository.sumTotalByFechaBetween(primerDiaMes.atStartOfDay(), finDiaSemana);

        Long citasDia = citaRepository.countByFechaInicioBetween(inicioDia, finDia);
        Long citasSemana = citaRepository.countByFechaInicioBetween(inicioSemana, finDiaSemana);
        Long citasMes = citaRepository.countByFechaInicioBetween(primerDiaMes.atStartOfDay(), finDiaSemana);

        // Ventas por tipo
        Double totalServicios = ventaDetalleRepository.sumServiciosByFechaBetween(inicioSemana, finDiaSemana);
        Double totalProductosVendidos = ventaDetalleRepository.sumTotalProductosByFechaBetween(inicioSemana, finDiaSemana);
        Double costoProductosVendidos = ventaDetalleRepository.sumCostoProductosByFechaBetween(inicioSemana, finDiaSemana);
        
        // Gastos de inventario (compras nuevas)
        Double gastosInventario = movimientoInventarioRepository.sumComprasByFechaBetween(inicioSemana, finDiaSemana);

        double totalServiciosVal = totalServicios != null ? totalServicios : 0.0;
        double totalProductosVal = totalProductosVendidos != null ? totalProductosVendidos : 0.0;
        double costoProductosVal = costoProductosVendidos != null ? costoProductosVendidos : 0.0;
        double gastosInventarioVal = gastosInventario != null ? gastosInventario : 0.0;

        // Utilidad bruta = Ventas - Costos de lo vendido
        double utilidadBruta = (totalServiciosVal + totalProductosVal) - costoProductosVal;
        
        // Comisiones estimadas
        double totalComisiones = calcularComisiones(inicioSemana, finDiaSemana);
        
        // Utilidad neta = Utilidad bruta - Comisiones - Gastos de inventario
        double gananciasNetas = utilidadBruta - totalComisiones - gastosInventarioVal;

        List<Object[]> productosData = ventaDetalleRepository.findProductosMasVendidosBetween(inicioSemana, finDiaSemana);
        List<DashboardDTO.ProductoVendidoDTO> productosVendidos = new ArrayList<>();
        for (Object[] row : productosData) {
            DashboardDTO.ProductoVendidoDTO p = new DashboardDTO.ProductoVendidoDTO();
            p.setIdProducto((Integer) row[0]);
            p.setNombre((String) row[1]);
            p.setCantidadVendida(((Number) row[2]).intValue());
            p.setTotalVendido(((Number) row[3]).doubleValue());
            productosVendidos.add(p);
        }

        List<Object[]> citasEmpleadoData = citaRepository.countCitasByEmpleadoBetween(inicioSemana, finDiaSemana);
        List<DashboardDTO.CitasEmpleadoDTO> citasPorEmpleado = new ArrayList<>();
        for (Object[] row : citasEmpleadoData) {
            DashboardDTO.CitasEmpleadoDTO c = new DashboardDTO.CitasEmpleadoDTO();
            c.setIdEmpleado((Integer) row[0]);
            c.setNombreEmpleado((String) row[1]);
            c.setTotalCitas(((Number) row[2]).longValue());
            c.setTotalServicios(totalServiciosVal);
            citasPorEmpleado.add(c);
        }

        DashboardDTO dto = new DashboardDTO();
        dto.setVentasDia(ventasDia != null ? ventasDia : 0.0);
        dto.setVentasSemana(ventasSemana != null ? ventasSemana : 0.0);
        dto.setVentasMes(ventasMes != null ? ventasMes : 0.0);
        dto.setTotalCitasDia(citasDia != null ? citasDia : 0L);
        dto.setTotalCitasSemana(citasSemana != null ? citasSemana : 0L);
        dto.setTotalCitasMes(citasMes != null ? citasMes : 0L);
        dto.setGananciasNetas(gananciasNetas);
        dto.setTotalComisiones(totalComisiones);
        dto.setProductosMasVendidos(productosVendidos);
        dto.setCitasPorEmpleado(citasPorEmpleado);

        return dto;
    }

    private double calcularComisiones(LocalDateTime inicio, LocalDateTime fin) {
        Double total = ventaDetalleRepository.sumComisionesByFechaBetween(inicio, fin);
        return total != null ? total : 0.0;
    }
}