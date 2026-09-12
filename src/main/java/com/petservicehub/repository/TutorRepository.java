package com.petservicehub.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.petservicehub.model.Tutor;

public interface TutorRepository extends JpaRepository<Tutor, Long> {

	List<Tutor> findByAtivo(Boolean ativo);

	boolean existsByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
