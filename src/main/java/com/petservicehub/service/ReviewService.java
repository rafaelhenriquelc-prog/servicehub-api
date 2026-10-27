package com.petservicehub.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
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
import com.petservicehub.model.UserRole;
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
		User reviewer = userService.usuarioAutenticado();
		if (reviewer.getRole() != UserRole.CLIENT) {
			throw new AccessDeniedException("Somente um CLIENT pode avaliar uma solicitação");
		}
		ServiceRequest serviceRequest = validarAvaliacao(request, reviewer, null);
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
		User current = userService.usuarioAutenticado();
		Long filtroAvaliador = reviewerId;
		Long filtroPrestador = null;
		if (current.getRole() == UserRole.CLIENT) {
			filtroAvaliador = current.getId();
		} else if (current.getRole() == UserRole.PROVIDER) {
			filtroPrestador = current.getId();
		}
		return reviewRepository.findAllWithRelations(filtroAvaliador, requestId, filtroPrestador)
				.stream()
				.map(EntityMapper::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public ReviewResponse buscarPorId(Long id) {
		Review review = buscarEntidade(id);
		garantirAcesso(review);
		return EntityMapper.toResponse(review);
	}

	@Transactional
	public ReviewResponse atualizar(Long id, ReviewRequest request) {
		Review review = buscarEntidade(id);
		garantirDonoOuAdmin(review);
		User reviewer = userService.usuarioAutenticado();
		User avaliador = reviewer.getRole() == UserRole.ADMIN ? review.getReviewer() : reviewer;
		ServiceRequest serviceRequest = validarAvaliacao(request, avaliador, review);
		review.setRequest(serviceRequest);
		review.setReviewer(avaliador);
		review.setRating(request.rating());
		review.setComment(request.comment());
		reviewRepository.save(review);
		return EntityMapper.toResponse(buscarEntidade(id));
	}

	@Transactional
	public void excluir(Long id) {
		Review review = buscarEntidade(id);
		garantirDonoOuAdmin(review);
		reviewRepository.delete(review);
	}

	private ServiceRequest validarAvaliacao(ReviewRequest request, User reviewer, Review avaliacaoAtual) {
		ServiceRequest serviceRequest = serviceRequestService.buscarEntidade(request.requestId());
		if (serviceRequest.getStatus() != RequestStatus.COMPLETED) {
			throw new BusinessException(HttpStatus.BAD_REQUEST, "A avaliação só pode ser cadastrada para uma solicitação COMPLETED");
		}
		if (!serviceRequest.getClient().getId().equals(reviewer.getId())) {
			throw new AccessDeniedException("O avaliador deve ser o cliente da solicitação");
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

	private void garantirAcesso(Review review) {
		User current = userService.usuarioAutenticado();
		if (current.getRole() == UserRole.ADMIN) {
			return;
		}
		if (current.getRole() == UserRole.CLIENT && review.getReviewer().getId().equals(current.getId())) {
			return;
		}
		if (current.getRole() == UserRole.PROVIDER
				&& review.getRequest().getService().getProvider().getId().equals(current.getId())) {
			return;
		}
		throw new AccessDeniedException("Você não pode acessar a avaliação de outro usuário");
	}

	private void garantirDonoOuAdmin(Review review) {
		User current = userService.usuarioAutenticado();
		if (current.getRole() == UserRole.ADMIN) {
			return;
		}
		if (current.getRole() == UserRole.CLIENT && review.getReviewer().getId().equals(current.getId())) {
			return;
		}
		throw new AccessDeniedException("Você não pode alterar a avaliação de outro usuário");
	}

	private Review buscarEntidade(Long id) {
		return reviewRepository.findByIdWithRelations(id)
				.orElseThrow(() -> new ResourceNotFoundException("Avaliação não encontrada com id " + id));
	}
}
