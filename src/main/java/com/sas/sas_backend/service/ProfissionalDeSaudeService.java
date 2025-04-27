package com.sas.sas_backend.service;

import com.sas.sas_backend.dtos.ProfissionalDeSaudeDto;
import com.sas.sas_backend.dtos.response.ProfissionalDeSaudeResponse;
import com.sas.sas_backend.exceptions.paciente.CredentialsNotMatchException;
import com.sas.sas_backend.exceptions.paciente.PacienteNotFoundException;
import com.sas.sas_backend.exceptions.profissionalDeSaude.ProfissionalDeSaudeAlreadyExistsException;
import com.sas.sas_backend.exceptions.profissionalDeSaude.ProfissionalDeSaudeNotFoundException;
import com.sas.sas_backend.mappers.ProfissionalDeSaudeMapper;
import com.sas.sas_backend.models.Paciente;
import com.sas.sas_backend.models.ProfissionalDeSaude;
import com.sas.sas_backend.models.UnidadeDeSaude;
import com.sas.sas_backend.models.enumerated.RolesEnum;
import com.sas.sas_backend.repository.ProfissionalDeSaudeRepository;
import com.sas.sas_backend.repository.UnidadeDeSaudeRepository;
import com.sas.sas_backend.utils.password.HashPassword;
import com.sas.sas_backend.utils.password.PasswordHashResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfissionalDeSaudeService {

    private final ProfissionalDeSaudeRepository profissionalDeSaudeRepository;
    private final UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    private final ProfissionalDeSaudeMapper profissionalDeSaudeMapper;
    private final TokenService tokenService;

    public ProfissionalDeSaudeDto buscarPorNome(String nome) {
        ProfissionalDeSaude profissional = profissionalDeSaudeRepository.findByNome(nome)
                .orElseThrow(() -> new ProfissionalDeSaudeNotFoundException("Profissional não encontrado com o nome: " + nome));
        return profissionalDeSaudeMapper.toProfissionalDeSaudeDto(profissional);
    }

    public String loginProfissional(String email, String password) {
        Optional<ProfissionalDeSaude> user = profissionalDeSaudeRepository.findByEmail(email);
        if(!user.isPresent()) {
            throw new CredentialsNotMatchException("");
        }

        boolean passwordMatch = HashPassword.checkPassword(password, user.get().getSenha(), user.get().getSalt());
        if(!passwordMatch) {
            throw new CredentialsNotMatchException("");
        }
        return tokenService.gerarToken(user.get().getId(), RolesEnum.PROFISSIONAL.getNome());
    }

    @Transactional
    public ProfissionalDeSaudeResponse cadastrarProfissionalDeSaude(ProfissionalDeSaudeDto dto) {
        profissionalDeSaudeRepository.findByNumeroRegistro(dto.numeroRegistro())
                .ifPresent(profissionalDeSaude -> {
                    throw new ProfissionalDeSaudeAlreadyExistsException("Profissional já cadastrado com o nome: " + dto.nome());
                        }
                );
        UnidadeDeSaude unidade = unidadeDeSaudeRepository.findByCnpj(dto.unidadeCnpj())
                .orElseThrow(() ->
                new ProfissionalDeSaudeAlreadyExistsException("CNPJ da unidade não encontrado: " + dto.unidadeCnpj())
        );


        ProfissionalDeSaude profissional = profissionalDeSaudeMapper.toProfissionalDeSaude(dto);
        PasswordHashResponse passwordHashResponse = HashPassword.hashPassword(dto.senha());
        profissional.setSenha(passwordHashResponse.hash());
        profissional.setSalt(passwordHashResponse.salt());
        profissional.setUnidadeDeSaude(unidade);
        ProfissionalDeSaude savedProfissional = profissionalDeSaudeRepository.save(profissional);
        return profissionalDeSaudeMapper.toResponse(savedProfissional);
    }

    @Transactional
    public void alterarSenha(String id, String senhaAtual, String novaSenha) {
        ProfissionalDeSaude profissionalDeSaude = profissionalDeSaudeRepository.findById(id)
                .orElseThrow(() -> new PacienteNotFoundException("Paciente não encontrado"));

        boolean senhaCorreta = HashPassword.checkPassword(senhaAtual, profissionalDeSaude.getSenha(), profissionalDeSaude.getSalt());
        if (!senhaCorreta) {
            throw new CredentialsNotMatchException("Senha atual incorreta");
        }

        PasswordHashResponse novaHash = HashPassword.hashPassword(novaSenha);
        profissionalDeSaude.setSenha(novaHash.hash());
        profissionalDeSaude.setSalt(novaHash.salt());

        profissionalDeSaudeRepository.save(profissionalDeSaude);
    }


    public List<ProfissionalDeSaudeDto> buscarTodos() {
        List<ProfissionalDeSaude> profissionais = profissionalDeSaudeRepository.findAll();
        if (profissionais.isEmpty()) {
            throw new ProfissionalDeSaudeNotFoundException("Nenhum profissional encontrado.");
        }
        return profissionais.stream().map(profissionalDeSaudeMapper::toProfissionalDeSaudeDto).toList();
    }

    public ProfissionalDeSaudeDto atualizarProfissionalDeSaude(String id, @RequestBody ProfissionalDeSaudeDto dto) {
        ProfissionalDeSaude profissionalExistente = profissionalDeSaudeRepository.findById(id)
                .orElseThrow(() -> new ProfissionalDeSaudeNotFoundException("Profissional não encontrado com o ID: " + id));
        ProfissionalDeSaude profissional = profissionalDeSaudeMapper.toProfissionalDeSaude(dto);
        profissional.setId(profissionalExistente.getId());
        return profissionalDeSaudeMapper.toProfissionalDeSaudeDto(profissionalDeSaudeRepository.save(profissional));
    }

    public void deletarProfissionalDeSaude(String id) {
        profissionalDeSaudeRepository.findById(id).orElseThrow(() -> new ProfissionalDeSaudeNotFoundException("Não foi possível deletar usuário com ID: " + id));
        profissionalDeSaudeRepository.deleteById(id);
    }

    public ProfissionalDeSaudeResponse buscarPorNumeroRegistro(String numero){
        ProfissionalDeSaude profissional = profissionalDeSaudeRepository.findByNumeroRegistro(numero).orElseThrow(() ->
                new ProfissionalDeSaudeNotFoundException("Número não encotrado."));
        return profissionalDeSaudeMapper.toResponse(profissional);
    }


}