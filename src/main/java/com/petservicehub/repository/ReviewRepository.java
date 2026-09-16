package com.petservicehub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.petservicehub.model.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	boolean existsByRequestId(Long requestId);

	boolean existsByReviewerId(Long reviewerId);

	@Query("""
			select r from Review r
			join fetch r.request req
			join fetch req.service
			join fetch req.client
			join fetch r.reviewer
			where r.id = :id
			""")
	Optional<Review> findByIdWithRelations(@Param("id") Long id);

	@Query("""
			select r from Review r
			join fetch r.request req
			join fetch req.service
			join fetch req.client
			join fetch r.reviewer
			where (:reviewerId is null or r.reviewer.id = :reviewerId)
			and (:requestId is null or r.request.id = :requestId)
			""")
	List<Review> findAllWithRelations(
			@Param("reviewerId") Long reviewerId,
			@Param("requestId") Long requestId);
}
