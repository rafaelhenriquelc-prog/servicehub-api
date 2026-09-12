package com.petservicehub.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petservicehub.dto.EntityMapper;
import com.petservicehub.dto.ServicoRequest;
import com.petservicehub.dto.ServicoResponse;
import com.petservicehub.exception.BusinessException;
import com.petservicehub.exception.ResourceNotFoundException;
import com.petservicehub.model.Servico;
import com.petservicehub.repository.AgendamentoRepository;
import com.petservicehub.repository.ServicoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServicoService {

	private final ServicoRepository servicoRepository;
	private final AgendamentoRepository agendamentoRepository;

	@Transactional
	public ServicoResponse criar(ServicoRequest request) {
		Servico servico = Servico.builder()
				.nome(request.nome())
				.descricao(request.descricao())
				.preco(request.preco())
				.duracaoMinutos(request.duracaoMinutos())
				.ativo(request.ativo() != null ? request.ativo() : true)
				.build();
		return EntityMapper.toResponse(servicoRepository.save(servico));
	}

	@Transactional(readOnly = true)
	public List<ServicoResponse> listar(Boolean ativo) {
		List<Servico> servicos = ativo == null ? servicoRepository.findAll() : servicoRepository.findByAtivo(ativo);
		return servicos.stream().map(EntityMapper::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public ServicoResponse buscarPorId(Long id) {
		return EntityMapper.toResponse(buscarEntidade(id));
	}

	@Transactional
	public ServicoResponse atualizar(Long id, ServicoRequest request) {
		Servico servico = buscarEntidade(id);
		servico.setNome(request.nome());
		servico.setDescricao(request.descricao());
		servico.setPreco(request.preco());
		servico.setDuracaoMinutos(request.duracaoMinutos());
		if (request.ativo() != null) {
			servico.setAtivo(request.ativo());
		}
		return EntityMapper.toResponse(servicoRepository.save(servico));
	}

	@Transactional
	public void excluir(Long id) {
		Servico servico = buscarEntidade(id);
		if (agendamentoRepository.existsByServicoId(id)) {
			throw new BusinessException(HttpStatus.CONFLICT, "Não é possível excluir o serviço porque existem agendamentos vinculados");
		}
		servicoRepository.delete(servico);
	}

	@Transactional(readOnly = true)
	public Servico buscarEntidade(Long id) {
		return servicoRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado com id " + id));
	}
}
