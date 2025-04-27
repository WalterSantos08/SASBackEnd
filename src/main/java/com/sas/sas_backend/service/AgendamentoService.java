package com.sas.sas_backend.service;

import com.sas.sas_backend.dtos.AgendamentoDto;
import com.sas.sas_backend.dtos.AgendamentoExameDto;
import com.sas.sas_backend.dtos.response.AgendamentoResponse;
import com.sas.sas_backend.mappers.AgendamentoMapper;
import com.sas.sas_backend.mappers.ExameAgendamentoMapper;
import com.sas.sas_backend.models.Agendamento;
import com.sas.sas_backend.models.Paciente;
import com.sas.sas_backend.models.ProfissionalDeSaude;
import com.sas.sas_backend.models.UnidadeDeSaude;
import com.sas.sas_backend.repository.AgendamentoRepository;
import com.sas.sas_backend.repository.PacienteRepository;
import com.sas.sas_backend.repository.ProfissionalDeSaudeRepository;
import com.sas.sas_backend.repository.UnidadeDeSaudeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgendamentoService {
    private final AgendamentoRepository agendamentoRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfissionalDeSaudeRepository profissionalRepository;
    private final UnidadeDeSaudeRepository unidadeRepository;
    private final AgendamentoMapper agendamentoMapper;

    private static final LocalTime HORA_ABERTURA = LocalTime.of(8, 0);
    private static final LocalTime HORA_FECHAMENTO = LocalTime.of(18, 0);
    private static final Duration DURACAO_MINIMA = Duration.ofMinutes(30);
    private final ExameAgendamentoMapper exameAgendamentoMapper;

    @Transactional
    public AgendamentoResponse criarAgendamento(AgendamentoDto dto) {
        validarHorarioAgendamento(dto.dataHoraInicio(), dto.dataHoraFim());
        validarDisponibilidade(dto.dataHoraInicio(), dto.dataHoraFim(),
                dto.pacienteCpf(), dto.profissionalNumero());

        Paciente paciente = buscarPaciente(dto.pacienteCpf());
        ProfissionalDeSaude profissional = buscarProfissional(dto.profissionalNumero());
        UnidadeDeSaude unidade = buscarUnidade(dto.unidadeCnpj());

        Agendamento agendamento = agendamentoMapper.toAgendamento(dto);
        agendamento.setPaciente(paciente);
        agendamento.setProfissional(profissional);
        agendamento.setUnidadeDeSaude(unidade);

        return agendamentoMapper.toResponse(agendamentoRepository.save(agendamento));
    }

    @Transactional
    public Agendamento criarAgendamentoParaExame(AgendamentoExameDto dto) {
      Agendamento agendamento = exameAgendamentoMapper.toResponse(dto);
      Agendamento savedAgendamento = agendamentoRepository.save(agendamento);
        return agendamentoRepository.save(savedAgendamento);
    }


    public List<Agendamento> buscarAgendamentosDoDia(String profissionalNumero, String pacienteCpf, LocalDate data) {
        LocalDateTime inicioDia = data.atTime(8, 0);
        LocalDateTime fimDia = data.atTime(18, 0);

        return agendamentoRepository.findByProfissionalNumeroRegistroOrPacienteCpfAndDataHoraInicioBetween(
                profissionalNumero,
                pacienteCpf,
                inicioDia,
                fimDia
        );
    }

    private void validarHorarioAgendamento(LocalDateTime inicio, LocalDateTime fim) {
        if (fim.isBefore(inicio)) {
            throw new RuntimeException("Horário de término deve ser após o início");
        }
        if (Duration.between(inicio, fim).compareTo(DURACAO_MINIMA) < 0) {
            throw new RuntimeException("Duração mínima de " + DURACAO_MINIMA.toMinutes() + " minutos");
        }
        if (inicio.toLocalTime().isBefore(HORA_ABERTURA) || fim.toLocalTime().isAfter(HORA_FECHAMENTO)) {
            throw new RuntimeException("Horário fora do expediente " + HORA_ABERTURA + " às " + HORA_FECHAMENTO);
        }

    }

    private void validarDisponibilidade(LocalDateTime inicio, LocalDateTime fim,
                                        String pacienteCpf, String profissionalNumero) {
        boolean conflitoPaciente = agendamentoRepository
                .existsByPacienteCpfAndDataHoraInicioLessThanAndDataHoraFimGreaterThan(pacienteCpf, fim, inicio);

        if (conflitoPaciente) {
            throw new RuntimeException("Paciente já possui agendamento neste horário");
        }

        boolean conflitoProfissional = agendamentoRepository
                .existsByProfissionalNumeroRegistroAndDataHoraInicioLessThanAndDataHoraFimGreaterThan(profissionalNumero, fim, inicio);

        if (conflitoProfissional) {
            throw new RuntimeException("Profissional já possui agendamento neste horário");
        }

        if (agendamentoRepository.existeConflitoParaPaciente(pacienteCpf, inicio, fim)) {
            throw new RuntimeException("Paciente já possui agendamento neste horário");
        }
        if (agendamentoRepository.existeConflitoParaProfissional(profissionalNumero, inicio, fim)) {
            throw new RuntimeException("Profissional já possui agendamento neste horário");
        }
    }

    private Paciente buscarPaciente(String cpf) {
        return pacienteRepository.findByCpf(cpf)
                .orElseThrow(() -> new EntityNotFoundException("Paciente não encontrado"));
    }

    private ProfissionalDeSaude buscarProfissional(String registro) {
        return profissionalRepository.findByNumeroRegistro(registro)
                .orElseThrow(() -> new EntityNotFoundException("Profissional não encontrado"));
    }

    private UnidadeDeSaude buscarUnidade(String cnpj) {
        return unidadeRepository.findByCnpj(cnpj)
                .orElseThrow(() -> new EntityNotFoundException("Unidade não encontrada"));
    }
}