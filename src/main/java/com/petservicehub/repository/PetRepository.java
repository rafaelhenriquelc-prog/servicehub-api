package com.petservicehub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.petservicehub.model.Pet;

public interface PetRepository extends JpaRepository<Pet, Long> {

	List<Pet> findByAtivo(Boolean ativo);

	List<Pet> findByTutorId(Long tutorId);

	List<Pet> findByTutorIdAndAtivo(Long tutorId, Boolean ativo);

	boolean existsByTutorId(Long tutorId);

	@Query("select p from Pet p join fetch p.tutor where p.id = :id")
	Optional<Pet> findByIdWithTutor(@Param("id") Long id);

	@Query("select p from Pet p join fetch p.tutor")
	List<Pet> findAllWithTutor();

	@Query("select p from Pet p join fetch p.tutor where p.ativo = :ativo")
	List<Pet> findByAtivoWithTutor(@Param("ativo") Boolean ativo);

	@Query("select p from Pet p join fetch p.tutor where p.tutor.id = :tutorId")
	List<Pet> findByTutorIdWithTutor(@Param("tutorId") Long tutorId);

	@Query("select p from Pet p join fetch p.tutor where p.tutor.id = :tutorId and p.ativo = :ativo")
	List<Pet> findByTutorIdAndAtivoWithTutor(@Param("tutorId") Long tutorId, @Param("ativo") Boolean ativo);
}
