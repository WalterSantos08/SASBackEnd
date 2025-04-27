package com.sas.sas_backend.service;

import com.sas.sas_backend.dtos.EnderecoDto;
import com.sas.sas_backend.mappers.EnderecoMapper;
import com.sas.sas_backend.models.Endereco;
import com.sas.sas_backend.repository.EnderecoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnderecoService {

    private final EnderecoMapper enderecoMapper;
    private final EnderecoRepository enderecoRepository;

    public EnderecoDto cadastrarEndereco(EnderecoDto dto) {

        Endereco endereco = enderecoMapper.toEndereco(dto);
        Endereco enderecoSalvo = enderecoRepository.save(endereco);
        return enderecoMapper.toEnderecoDto(enderecoSalvo);
    }

    public EnderecoDto atualizarEndereco(String id, EnderecoDto dto) {
        Endereco enderecoExistente = enderecoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Endereço não encontrado."));
        Endereco endereco = enderecoMapper.toEndereco(dto);
        endereco.setId(enderecoExistente.getId());
        return enderecoMapper.toEnderecoDto(enderecoRepository.save(endereco));
    }

    public void deletarEndereco(String id) {
        enderecoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Endereço não encontrado."));
        enderecoRepository.deleteById(id);

    }


}
