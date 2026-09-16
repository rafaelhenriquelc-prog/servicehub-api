package com.petservicehub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.petservicehub.model.RequestStatus;
import com.petservicehub.model.ServiceRequest;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

	boolean existsByServiceId(Long serviceId);

	boolean existsByClientId(Long clientId);

	@Query("""
			select r from ServiceRequest r
			join fetch r.service s
			join fetch s.provider
			join fetch r.client
			where r.id = :id
			""")
	Optional<ServiceRequest> findByIdWithRelations(@Param("id") Long id);

	@Query("""
			select r from ServiceRequest r
			join fetch r.service s
			join fetch s.provider
			join fetch r.client
			where (:status is null or r.status = :status)
			and (:clientId is null or r.client.id = :clientId)
			and (:serviceId is null or r.service.id = :serviceId)
			""")
	List<ServiceRequest> findAllWithRelations(
			@Param("status") RequestStatus status,
			@Param("clientId") Long clientId,
			@Param("serviceId") Long serviceId);
}
