package com.petservicehub.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.petservicehub.model.Servico;

public interface ServicoRepository extends JpaRepository<Servico, Long> {

	List<Servico> findByAtivo(Boolean ativo);
}
