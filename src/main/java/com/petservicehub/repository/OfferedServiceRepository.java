package com.petservicehub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.petservicehub.model.OfferedService;

public interface OfferedServiceRepository extends JpaRepository<OfferedService, Long> {

	boolean existsByProviderId(Long providerId);

	@Query("select s from OfferedService s join fetch s.provider where s.id = :id")
	Optional<OfferedService> findByIdWithProvider(@Param("id") Long id);

	@Query("""
			select s from OfferedService s
			join fetch s.provider
			where (:active is null or s.active = :active)
			and (:providerId is null or s.provider.id = :providerId)
			and (:category is null or lower(s.category) = lower(:category))
			""")
	List<OfferedService> findAllWithProvider(
			@Param("active") Boolean active,
			@Param("providerId") Long providerId,
			@Param("category") String category);
}
