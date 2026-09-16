package com.petservicehub.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petservicehub.dto.EntityMapper;
import com.petservicehub.dto.ReviewRequest;
import com.petservicehub.dto.ReviewResponse;
import com.petservicehub.exception.BusinessException;
import com.petservicehub.exception.ResourceNotFoundException;
import com.petservicehub.model.RequestStatus;
import com.petservicehub.model.Review;
import com.petservicehub.model.ServiceRequest;
import com.petservicehub.model.User;
import com.petservicehub.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final ServiceRequestService serviceRequestService;
	private final UserService userService;

	@Transactional
	public ReviewResponse criar(ReviewRequest request) {
		ServiceRequest serviceRequest = validarAvaliacao(request, null);
		User reviewer = userService.buscarEntidade(request.reviewerId());
		Review review = Review.builder()
				.request(serviceRequest)
				.reviewer(reviewer)
				.rating(request.rating())
				.comment(request.comment())
				.build();
		Review salvo = reviewRepository.save(review);
		return EntityMapper.toResponse(buscarEntidade(salvo.getId()));
	}

	@Transactional(readOnly = true)
	public List<ReviewResponse> listar(Long reviewerId, Long requestId) {
		return reviewRepository.findAllWithRelations(reviewerId, requestId)
				.stream()
				.map(EntityMapper::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public ReviewResponse buscarPorId(Long id) {
		return EntityMapper.toResponse(buscarEntidade(id));
	}

	@Transactional
	public ReviewResponse atualizar(Long id, ReviewRequest request) {
		Review review = buscarEntidade(id);
		ServiceRequest serviceRequest = validarAvaliacao(request, review);
		User reviewer = userService.buscarEntidade(request.reviewerId());
		review.setRequest(serviceRequest);
		review.setReviewer(reviewer);
		review.setRating(request.rating());
		review.setComment(request.comment());
		reviewRepository.save(review);
		return EntityMapper.toResponse(buscarEntidade(id));
	}

	@Transactional
	public void excluir(Long id) {
		Review review = buscarEntidade(id);
		reviewRepository.delete(review);
	}

	private ServiceRequest validarAvaliacao(ReviewRequest request, Review avaliacaoAtual) {
		ServiceRequest serviceRequest = serviceRequestService.buscarEntidade(request.requestId());
		if (serviceRequest.getStatus() != RequestStatus.COMPLETED) {
			throw new BusinessException(HttpStatus.BAD_REQUEST, "A avaliação só pode ser cadastrada para uma solicitação COMPLETED");
		}
		if (!serviceRequest.getClient().getId().equals(request.reviewerId())) {
			throw new BusinessException(HttpStatus.BAD_REQUEST, "O avaliador deve ser o cliente da solicitação");
		}
		boolean outraAvaliacaoNaSolicitacao = reviewRepository.existsByRequestId(request.requestId());
		if (avaliacaoAtual != null && avaliacaoAtual.getRequest().getId().equals(request.requestId())) {
			outraAvaliacaoNaSolicitacao = false;
		}
		if (outraAvaliacaoNaSolicitacao) {
			throw new BusinessException(HttpStatus.CONFLICT, "Esta solicitação já possui uma avaliação");
		}
		return serviceRequest;
	}

	private Review buscarEntidade(Long id) {
		return reviewRepository.findByIdWithRelations(id)
				.orElseThrow(() -> new ResourceNotFoundException("Avaliação não encontrada com id " + id));
	}
}
