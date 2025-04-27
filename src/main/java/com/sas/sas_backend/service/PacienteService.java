package com.sas.sas_backend.service;

import com.sas.sas_backend.dtos.PacienteDto;
import com.sas.sas_backend.exceptions.paciente.CredentialsNotMatchException;
import com.sas.sas_backend.exceptions.paciente.PacienteAlreadyExistsException;
import com.sas.sas_backend.exceptions.paciente.PacienteNotFoundException;
import com.sas.sas_backend.mappers.ExameMapper;
import com.sas.sas_backend.mappers.PacienteMapper;
import com.sas.sas_backend.models.Paciente;
import com.sas.sas_backend.models.enumerated.RolesEnum;
import com.sas.sas_backend.repository.PacienteRepository;
import com.sas.sas_backend.utils.password.HashPassword;
import com.sas.sas_backend.utils.password.PasswordHashResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final PacienteMapper pacienteMapper;
    private final TokenService tokenService;
    private final ExameMapper exameMapper;

    public PacienteDto buscarPorCpf(String cpf) {

        Paciente pacienteCPF = pacienteRepository.findByCpf(cpf).orElseThrow(() -> new PacienteNotFoundException(" CPF:" + cpf));
        return pacienteMapper.toPacienteDto(pacienteCPF);

    }

    public String loginPaciente(String email, String password) {
        Optional<Paciente> user = pacienteRepository.findByEmail(email);
        if(!user.isPresent()) {
           throw new CredentialsNotMatchException("");
        }

        boolean passwordMatch = HashPassword.checkPassword(password, user.get().getSenha(), user.get().getSalt());
        if(!passwordMatch) {
            throw new CredentialsNotMatchException("");
        }
        return tokenService.gerarToken(user.get().getId(), RolesEnum.PACIENTE.getNome());
    }

    @Transactional
    public PacienteDto cadastrarPaciente(PacienteDto dto) {
        if (pacienteRepository.findByEmailOrCpf(dto.email(), dto.cpf())) {
            throw new PacienteAlreadyExistsException("Email ou CPF já cadastrado !");
        }

        Paciente paciente = pacienteMapper.toPaciente(dto);

        PasswordHashResponse response = HashPassword.hashPassword(paciente.getSenha());

        paciente.setSenha(response.hash());
        paciente.setSalt(response.salt());

        Paciente savedPaciente = pacienteRepository.save(paciente);

        return pacienteMapper.toPacienteDto(savedPaciente);
    }

    @Transactional
    public void alterarSenha(String id, String senhaAtual, String novaSenha) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new PacienteNotFoundException("Paciente não encontrado"));

        boolean senhaCorreta = HashPassword.checkPassword(senhaAtual, paciente.getSenha(), paciente.getSalt());
        if (!senhaCorreta) {
            throw new CredentialsNotMatchException("Senha atual incorreta");
        }

        PasswordHashResponse novaHash = HashPassword.hashPassword(novaSenha);
        paciente.setSenha(novaHash.hash());
        paciente.setSalt(novaHash.salt());

        pacienteRepository.save(paciente);
    }



    public List<PacienteDto> buscarTodos() {
        List<Paciente> listPaciente = pacienteRepository.findAll();
        if (listPaciente.isEmpty()) {
            throw new PacienteNotFoundException("Lista não encontrada");
        }
        return listPaciente.stream().map(pacienteMapper::toPacienteDto).toList();

    }

    public PacienteDto atualizarPaciente(String id, PacienteDto dto) {
        Paciente pacienteExistente = pacienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado."));
        Paciente paciente = pacienteMapper.toPaciente(dto);
        paciente.setId(pacienteExistente.getId());
        return pacienteMapper.toPacienteDto(pacienteRepository.save(paciente));
    }

    public void deletarPaciente(String id) {
        pacienteRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado."));
        pacienteRepository.deleteById(id);

    }
}
