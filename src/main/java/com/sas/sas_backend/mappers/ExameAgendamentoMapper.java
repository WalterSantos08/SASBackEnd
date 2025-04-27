package com.sas.sas_backend.mappers;

import com.sas.sas_backend.dtos.AgendamentoExameDto;
import com.sas.sas_backend.dtos.ExameDto;
import com.sas.sas_backend.dtos.response.ExameAgendamentoResponse;
import com.sas.sas_backend.models.Agendamento;
import com.sas.sas_backend.models.Exame;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = AgendamentoMapper.class)
public interface ExameAgendamentoMapper {

    ExameDto toExameDto(Exame exame);

    Exame toExame(ExameDto dto);

    @Mapping(target = "idExame", source = "exame.id")
    @Mapping(target = "tipoExame", source = "exame.tipoExame")
    @Mapping(target = "status", source = "exame.status")
    @Mapping(target = "idAgendamento", source = "agendamento.id")
    @Mapping(target = "horario", source = "agendamento.dataHoraInicio")
    @Mapping(target = "statusAgendamento", source = "agendamento.status")
    @Mapping(target = "paciente", source = "agendamento.paciente")
    @Mapping(target = "profissionalDeSaude", source = "agendamento.profissional")
    ExameAgendamentoResponse toResponse(Exame exame, Agendamento agendamento);

    Agendamento toResponse(AgendamentoExameDto dto);
}
