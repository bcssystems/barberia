package com.bcsystems.barberia_api.repository;

import com.bcsystems.barberia_api.domain.Cita;
import com.bcsystems.barberia_api.domain.en.EstadoCita;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Integer> {
    Page<Cita> findByEmpleadoIdEmpleado(Integer idEmpleado, Pageable pageable);
    Page<Cita> findByClienteIdCliente(Integer idCliente, Pageable pageable);
    Page<Cita> findByEstado(EstadoCita estado, Pageable pageable);
    Page<Cita> findByFechaInicioBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    List<Cita> findByEmpleadoIdEmpleadoAndFechaInicioBetweenAndEstadoNot(
            Integer idEmpleado, LocalDateTime start, LocalDateTime end, EstadoCita estado);

    List<Cita> findByEstadoAndFechaFinBefore(EstadoCita estado, LocalDateTime limite);

    @Query("SELECT COUNT(c) FROM Cita c WHERE c.fechaInicio BETWEEN :start AND :end")
    Long countByFechaInicioBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(c) FROM Cita c WHERE c.empleado.idEmpleado = :idEmpleado AND c.fechaInicio BETWEEN :start AND :end")
    Long countByEmpleadoIdEmpleadoAndFechaInicioBetween(@Param("idEmpleado") Integer idEmpleado, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT c.empleado.idEmpleado, c.empleado.nombre, COUNT(c) FROM Cita c WHERE c.fechaInicio BETWEEN :start AND :end GROUP BY c.empleado.idEmpleado, c.empleado.nombre")
    List<Object[]> countCitasByEmpleadoBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}