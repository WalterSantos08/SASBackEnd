package com.sas.sas_backend.service;

import com.sas.sas_backend.dtos.AgendamentoExameDto;
import com.sas.sas_backend.dtos.ExameDto;
import com.sas.sas_backend.dtos.response.ExameAgendamentoResponse;
import com.sas.sas_backend.exceptions.exame.ExameNotFoundException;
import com.sas.sas_backend.mappers.ExameAgendamentoMapper;
import com.sas.sas_backend.models.Agendamento;
import com.sas.sas_backend.models.Exame;
import com.sas.sas_backend.models.enumerated.StatusExame;
import com.sas.sas_backend.repository.ExameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExameService {

    private final ExameRepository exameRepository;
    private final AgendamentoService agendamentoService;
    private final ExameAgendamentoMapper exameAgendamentoMapper;

    @Transactional
    public ExameAgendamentoResponse agendarExame(AgendamentoExameDto dto) {
        // Cria o agendamento através do AgendamentoService
        Agendamento agendamento = agendamentoService.criarAgendamentoParaExame(dto) ;

        // Criação do exame (regras específicas de exame)
        Exame exame = new Exame();
        exame.setTipoExame(dto.tipoExame());
        exame.setDescricao(dto.descricao());
        exame.setStatus(StatusExame.AGENDADO);
        exame.setAgendamento(agendamento);

        // Estabelece a relação bidirecional
        agendamento.setExame(exame);

        exameRepository.save(exame);

        return exameAgendamentoMapper.toResponse(exame, agendamento);
    }

    public List<ExameDto> listarExames() {
        List<Exame> exames = exameRepository.findAll();
        if (exames.isEmpty()) {
            throw new ExameNotFoundException("Exames não encontrados");
        }
        return exames.stream().map(exameAgendamentoMapper::toExameDto).toList();
    }

    public void removerExame(String id) {
        exameRepository.findById(id).orElseThrow(() -> new ExameNotFoundException(id));
        exameRepository.deleteById(id);
    }

    public ExameDto atualizarExame(String id, ExameDto dto) {
        Exame exameExistente = exameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exame não encontrado."));
        Exame exame = exameAgendamentoMapper.toExame(dto);
        exame.setId(exameExistente.getId());
        return exameAgendamentoMapper.toExameDto(exameRepository.save(exame));
    }
}