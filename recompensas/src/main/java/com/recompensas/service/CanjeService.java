package com.recompensas.service;

import com.recompensas.entity.Canje;
import com.recompensas.entity.Recompensa;
import com.recompensas.entity.SaldoPuntos;
import com.recompensas.entity.MovimientoPuntos;
import com.recompensas.repository.CanjeRepository;
import com.recompensas.repository.RecompensaRepository;
import com.recompensas.repository.SaldoPuntosRepository;
import com.recompensas.repository.MovimientoPuntosRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CanjeService {

    private final CanjeRepository canjeRepository;
    private final RecompensaRepository recompensaRepository;
    private final SaldoPuntosRepository saldoPuntosRepository;
    private final MovimientoPuntosRepository movimientoPuntosRepository;

    public CanjeService(
            CanjeRepository canjeRepository,
            RecompensaRepository recompensaRepository,
            SaldoPuntosRepository saldoPuntosRepository,
            MovimientoPuntosRepository movimientoPuntosRepository) {

        this.canjeRepository = canjeRepository;
        this.recompensaRepository = recompensaRepository;
        this.saldoPuntosRepository = saldoPuntosRepository;
        this.movimientoPuntosRepository = movimientoPuntosRepository;
    }

    private String emailDeLaPeticion() {
        try {
            org.springframework.web.context.request.RequestAttributes attrs =
                    org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attrs instanceof org.springframework.web.context.request.ServletRequestAttributes sra) {
                return sra.getRequest().getHeader("X-User-Email");
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    // Consultar todos los canjes (administracion)
    public List<Canje> todosLosCanjes() {
        return canjeRepository.findAll().stream()
                .sorted((a, b) -> b.getFecha().compareTo(a.getFecha()))
                .toList();
    }

    // Consultar los canjes de un usuario
    public List<Canje> consultarCanjes(Long usuarioId) {
        return canjeRepository.findByUsuarioIdOrderByFechaDesc(usuarioId);
    }

    // Realizar un canje
    @Transactional
    public Canje realizarCanje(Long usuarioId, Long recompensaId) {

        if (usuarioId == null || recompensaId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Los datos del canje son obligatorios"
            );
        }

        // Buscar la recompensa
        Recompensa recompensa = recompensaRepository.findById(recompensaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Recompensa no encontrada"
                ));

        // Verificar que esté activa
        if (!Boolean.TRUE.equals(recompensa.getActiva())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Esta recompensa no está disponible"
            );
        }

        // Verificar que tenga stock
        if (recompensa.getStock() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No hay existencias disponibles"
            );
        }

        // Buscar el saldo del usuario
        SaldoPuntos saldo = saldoPuntosRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El usuario no tiene un saldo registrado"
                ));

        // Verificar que tenga suficientes puntos
        if (saldo.getPuntos() < recompensa.getCostoPuntos()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No tienes suficientes puntos"
            );
        }

        // Descontar los puntos
        saldo.setPuntos(saldo.getPuntos() - recompensa.getCostoPuntos());
        saldoPuntosRepository.save(saldo);

        // Descontar una unidad del stock
        recompensa.setStock(recompensa.getStock() - 1);
        recompensaRepository.save(recompensa);

        // Registrar el canje
        Canje canje = new Canje();
        canje.setUsuarioId(usuarioId);
        canje.setRecompensa(recompensa);
        canje.setPuntosUtilizados(recompensa.getCostoPuntos());
        canje.setEstado("PENDIENTE");
        canje.setUsuarioEmail(emailDeLaPeticion());

        canjeRepository.save(canje);

        // Registrar el movimiento de puntos
        MovimientoPuntos movimiento = new MovimientoPuntos();
        movimiento.setUsuarioId(usuarioId);
        movimiento.setCantidad(-recompensa.getCostoPuntos());
        movimiento.setTipo("CANJE");
        movimiento.setDescripcion("Canje de recompensa: " + recompensa.getNombre());

        movimientoPuntosRepository.save(movimiento);

        return canje;
    }

    // Aprobar un canje
    @Transactional
    public Canje aprobarCanje(Long canjeId) {

        Canje canje = canjeRepository.findById(canjeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Canje no encontrado"
                ));

        if (!"PENDIENTE".equals(canje.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solo se pueden aprobar canjes pendientes"
            );
        }

        canje.setEstado("APROBADO");

        return canjeRepository.save(canje);
    }

    // Cancelar un canje y devolver los puntos
    @Transactional
    public Canje cancelarCanje(Long canjeId) {

        Canje canje = canjeRepository.findById(canjeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Canje no encontrado"
                ));

        if (!"PENDIENTE".equals(canje.getEstado())
                && !"APROBADO".equals(canje.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Este canje no se puede cancelar"
            );
        }

        // Buscar el saldo del usuario
        SaldoPuntos saldo = saldoPuntosRepository.findByUsuarioId(canje.getUsuarioId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Saldo del usuario no encontrado"
                ));

        // Devolver los puntos
        saldo.setPuntos(saldo.getPuntos() + canje.getPuntosUtilizados());
        saldoPuntosRepository.save(saldo);

        // Restaurar el stock
        Recompensa recompensa = canje.getRecompensa();
        recompensa.setStock(recompensa.getStock() + 1);
        recompensaRepository.save(recompensa);

        // Registrar la devolución en el historial
        MovimientoPuntos movimiento = new MovimientoPuntos();
        movimiento.setUsuarioId(canje.getUsuarioId());
        movimiento.setCantidad(canje.getPuntosUtilizados());
        movimiento.setTipo("DEVOLUCION");
        movimiento.setDescripcion("Devolución de puntos por cancelar el canje " + canjeId);

        movimientoPuntosRepository.save(movimiento);

        // Cambiar el estado
        canje.setEstado("CANCELADO");

        return canjeRepository.save(canje);
    }

    // Marcar un canje como entregado
    @Transactional
    public Canje entregarCanje(Long canjeId) {

        Canje canje = canjeRepository.findById(canjeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Canje no encontrado"
                ));

        if (!"APROBADO".equals(canje.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solo se pueden entregar canjes aprobados"
            );
        }

        canje.setEstado("ENTREGADO");

        return canjeRepository.save(canje);
    }
}