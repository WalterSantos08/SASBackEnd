package com.sas.sas_backend.service;

import com.sas.sas_backend.dtos.UnidadeDeSaudeDto;
import com.sas.sas_backend.exceptions.paciente.CredentialsNotMatchException;
import com.sas.sas_backend.exceptions.paciente.PacienteNotFoundException;
import com.sas.sas_backend.mappers.UnidadeDeSaudeMapper;
import com.sas.sas_backend.models.ProfissionalDeSaude;
import com.sas.sas_backend.models.UnidadeDeSaude;
import com.sas.sas_backend.models.enumerated.RolesEnum;
import com.sas.sas_backend.repository.UnidadeDeSaudeRepository;
import com.sas.sas_backend.utils.password.HashPassword;
import com.sas.sas_backend.utils.password.PasswordHashResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UnidadeDeSaudeService {

    private final UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    private final UnidadeDeSaudeMapper unidadeDeSaudeMapper;
    private final TokenService tokenService;

    @Transactional
    public UnidadeDeSaudeDto create(UnidadeDeSaudeDto unidadeDeSaudeDto){
        unidadeDeSaudeRepository.findByCnpj(unidadeDeSaudeDto.cnpj())
                .ifPresent(unidadeDeSaude -> {
                    throw new RuntimeException("Unidade de Saúde já cadastrada.");
                });

        UnidadeDeSaude unidade = unidadeDeSaudeMapper.toUnidade(unidadeDeSaudeDto);

        PasswordHashResponse passwordHashResponse = HashPassword.hashPassword(unidadeDeSaudeDto.senha());
        unidade.setSenha(passwordHashResponse.hash());
        unidade.setSalt(passwordHashResponse.salt());

        UnidadeDeSaude savedUnidade = unidadeDeSaudeRepository.save(unidade);
        return unidadeDeSaudeMapper.toDto(savedUnidade);
    }

    public String loginUnidade(String email, String password) {
        Optional<UnidadeDeSaude> user = unidadeDeSaudeRepository.findByEmail(email);
        if(!user.isPresent()) {
            throw new CredentialsNotMatchException("");
        }

        boolean passwordMatch = HashPassword.checkPassword(password, user.get().getSenha(), user.get().getSalt());
        if(!passwordMatch) {
            throw new CredentialsNotMatchException("");
        }
        return tokenService.gerarToken(user.get().getId(), RolesEnum.PROFISSIONAL.getNome());
    }

    public UnidadeDeSaudeDto buscarPorCnpj(String cnpj){
        UnidadeDeSaude unidadeDeSaude = unidadeDeSaudeRepository.findByCnpj(cnpj)
                .orElseThrow(RuntimeException::new);
        return unidadeDeSaudeMapper.toDto(unidadeDeSaude);
    }

    public void alterarSenha(String id, String senhaAtual, String novaSenha) {
        UnidadeDeSaude unidadeDeSaude = unidadeDeSaudeRepository.findById(id)
                .orElseThrow(() -> new PacienteNotFoundException("Paciente não encontrado"));

        boolean senhaCorreta = HashPassword.checkPassword(senhaAtual, unidadeDeSaude.getSenha(), unidadeDeSaude.getSalt());
        if (!senhaCorreta) {
            throw new CredentialsNotMatchException("Senha atual incorreta");
        }

        PasswordHashResponse novaHash = HashPassword.hashPassword(novaSenha);
        unidadeDeSaude.setSenha(novaHash.hash());
        unidadeDeSaude.setSalt(novaHash.salt());

        unidadeDeSaudeRepository.save(unidadeDeSaude);
    }
}
