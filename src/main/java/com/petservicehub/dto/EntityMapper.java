package com.petservicehub.dto;

import com.petservicehub.model.OfferedService;
import com.petservicehub.model.Review;
import com.petservicehub.model.ServiceRequest;
import com.petservicehub.model.User;

public final class EntityMapper {

	private EntityMapper() {
	}

	public static UserResponse toResponse(User user) {
		return new UserResponse(
				user.getId(),
				user.getFullName(),
				user.getEmail(),
				user.getPhone(),
				user.getBio(),
				user.getAvatarUrl(),
				user.getRole(),
				user.getActive(),
				user.getCreatedAt(),
				user.getUpdatedAt());
	}

	public static UserSummary toSummary(User user) {
		return new UserSummary(user.getId(), user.getFullName(), user.getEmail(), user.getRole());
	}

	public static OfferedServiceResponse toResponse(OfferedService service) {
		return new OfferedServiceResponse(
				service.getId(),
				service.getTitle(),
				service.getDescription(),
				service.getPrice(),
				service.getCategory(),
				toSummary(service.getProvider()),
				service.getActive(),
				service.getCreatedAt());
	}

	public static OfferedServiceSummary toSummary(OfferedService service) {
		return new OfferedServiceSummary(service.getId(), service.getTitle(), service.getPrice(), service.getCategory());
	}

	public static ServiceRequestResponse toResponse(ServiceRequest request) {
		return new ServiceRequestResponse(
				request.getId(),
				toSummary(request.getService()),
				toSummary(request.getClient()),
				request.getStatus(),
				request.getScheduledAt(),
				request.getNotes(),
				request.getTotalPrice(),
				request.getCreatedAt(),
				request.getUpdatedAt());
	}

	public static ReviewResponse toResponse(Review review) {
		return new ReviewResponse(
				review.getId(),
				review.getRequest().getId(),
				toSummary(review.getReviewer()),
				review.getRating(),
				review.getComment(),
				review.getCreatedAt());
	}
}
