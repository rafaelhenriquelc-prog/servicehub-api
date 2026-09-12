package com.petservicehub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.petservicehub.model.Agendamento;
import com.petservicehub.model.StatusAgendamento;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

	boolean existsByPetId(Long petId);

	boolean existsByTutorId(Long tutorId);

	boolean existsByServicoId(Long servicoId);

	@Query("""
			select a from Agendamento a
			join fetch a.pet
			join fetch a.tutor
			join fetch a.servico
			where a.id = :id
			""")
	Optional<Agendamento> findByIdWithRelations(@Param("id") Long id);

	@Query("""
			select a from Agendamento a
			join fetch a.pet
			join fetch a.tutor
			join fetch a.servico
			""")
	List<Agendamento> findAllWithRelations();

	@Query("""
			select a from Agendamento a
			join fetch a.pet
			join fetch a.tutor
			join fetch a.servico
			where (:status is null or a.status = :status)
			and (:tutorId is null or a.tutor.id = :tutorId)
			and (:petId is null or a.pet.id = :petId)
			and (:servicoId is null or a.servico.id = :servicoId)
			""")
	List<Agendamento> findAllWithRelations(
			@Param("status") StatusAgendamento status,
			@Param("tutorId") Long tutorId,
			@Param("petId") Long petId,
			@Param("servicoId") Long servicoId);
}
