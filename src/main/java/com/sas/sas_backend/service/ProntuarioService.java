package com.sas.sas_backend.service;

import com.sas.sas_backend.dtos.ProntuarioDto;
import com.sas.sas_backend.dtos.response.ProntuarioResponse;
import com.sas.sas_backend.exceptions.prontuario.DuplicateRegistrationException;
import com.sas.sas_backend.mappers.ProntuarioMapper;
import com.sas.sas_backend.models.Paciente;
import com.sas.sas_backend.models.ProfissionalDeSaude;
import com.sas.sas_backend.models.Prontuario;
import com.sas.sas_backend.repository.PacienteRepository;
import com.sas.sas_backend.repository.ProfissionalDeSaudeRepository;
import com.sas.sas_backend.repository.ProntuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProntuarioService {

    private final ProntuarioRepository prontuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfissionalDeSaudeRepository profissionalDeSaudeRepository;
    private final ProntuarioMapper prontuarioMapper;

    @Transactional
    public ProntuarioResponse create(ProntuarioDto prontuarioDto) {

        if (prontuarioDto.pacienteCpf() == null || prontuarioDto.profissionalNumero() == null) {
            throw new IllegalArgumentException("CPF do paciente e número do profissional são obrigatórios.");
        }

        prontuarioRepository.findByPaciente_Cpf(prontuarioDto.pacienteCpf())
                .ifPresent(p -> {
                    throw new DuplicateRegistrationException("Paciente já possui prontuário cadastrado.");
                });

        Paciente paciente = pacienteRepository.findByCpf(prontuarioDto.pacienteCpf()).orElseThrow(() ->
                new RuntimeException(""));

        ProfissionalDeSaude profissionalDeSaude = profissionalDeSaudeRepository.findByNumeroRegistro
                        (prontuarioDto.profissionalNumero())
                .orElseThrow(() -> new RuntimeException(""));

        Prontuario prontuario = prontuarioMapper.toEntity(prontuarioDto);
        prontuario.setPaciente(paciente);
        prontuario.setProfissionalDeSaude(profissionalDeSaude);
        Prontuario savedProntuario = prontuarioRepository.save(prontuario);
        return prontuarioMapper.toResponse(savedProntuario);

    }

    public ProntuarioResponse buscarPorCpf(String cpf) {
        Prontuario prontuario = prontuarioRepository.findByPaciente_Cpf(cpf)
                .orElseThrow(() -> new RuntimeException(""));
        return prontuarioMapper.toResponse(prontuario);

    }
}
