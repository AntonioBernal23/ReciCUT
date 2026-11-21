package com.report_service.controller;

import com.report_service.dto.*;
import com.report_service.enums.Granularity;
import com.report_service.service.ReportQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * API de reportes.
 *
 * Fijate: TODOS los endpoints son GET. Esa es la firma de un servicio de
 * lectura. Si alguna vez te encuentras escribiendo un POST aqui, algo se
 * salio de su lugar: los datos entran UNICAMENTE por Kafka.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportQueryService consultas;

    public ReportController(ReportQueryService consultas) {
        this.consultas = consultas;
    }

    @GetMapping("/summary")
    public ResponseEntity<SummaryReportDTO> resumen(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDate[] p = periodo(from, to);
        return ResponseEntity.ok(consultas.resumen(p[0], p[1]));
    }

    @GetMapping("/materials")
    public ResponseEntity<MaterialBreakdownDTO> porMaterial(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDate[] p = periodo(from, to);
        return ResponseEntity.ok(consultas.porMaterial(p[0], p[1]));
    }

    @GetMapping("/timeline")
    public ResponseEntity<TimelineReportDTO> linea(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "DAY") Granularity granularity) {
        LocalDate[] p = periodo(from, to);
        return ResponseEntity.ok(consultas.linea(p[0], p[1], granularity));
    }

    @GetMapping("/impact")
    public ResponseEntity<EnvironmentalImpactDTO> impacto(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDate[] p = periodo(from, to);
        return ResponseEntity.ok(consultas.impacto(p[0], p[1]));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<LeaderboardDTO> ranking(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(consultas.ranking(limit));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserStatsDTO> usuario(@PathVariable Long userId) {
        return ResponseEntity.ok(consultas.usuario(userId));
    }

    /** Las estadisticas del usuario autenticado, leyendo el header del gateway. */
    @GetMapping("/users/me")
    public ResponseEntity<UserStatsDTO> yo(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(consultas.usuario(userId));
    }

    @GetMapping("/centers")
    public ResponseEntity<List<CenterStatsDTO>> centros() {
        return ResponseEntity.ok(consultas.centros());
    }

    @GetMapping("/centers/{centerId}")
    public ResponseEntity<CenterStatsDTO> centro(@PathVariable Long centerId) {
        return ResponseEntity.ok(consultas.centro(centerId));
    }

    /** Si no mandan fechas, el periodo por defecto son los ultimos 30 dias. */
    private LocalDate[] periodo(LocalDate from, LocalDate to) {
        LocalDate hasta = to == null ? LocalDate.now() : to;
        LocalDate desde = from == null ? hasta.minusDays(30) : from;
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("'from' no puede ser posterior a 'to'");
        }
        return new LocalDate[]{desde, hasta};
    }
}
