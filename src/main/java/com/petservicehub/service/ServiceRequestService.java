package com.petservicehub.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petservicehub.dto.EntityMapper;
import com.petservicehub.dto.ServiceRequestInput;
import com.petservicehub.dto.ServiceRequestResponse;
import com.petservicehub.exception.BusinessException;
import com.petservicehub.exception.ResourceNotFoundException;
import com.petservicehub.model.OfferedService;
import com.petservicehub.model.RequestStatus;
import com.petservicehub.model.ServiceRequest;
import com.petservicehub.model.User;
import com.petservicehub.model.UserRole;
import com.petservicehub.repository.ReviewRepository;
import com.petservicehub.repository.ServiceRequestRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceRequestService {

	private final ServiceRequestRepository serviceRequestRepository;
	private final OfferedServiceService offeredServiceService;
	private final UserService userService;
	private final ReviewRepository reviewRepository;

	@Transactional
	public ServiceRequestResponse criar(ServiceRequestInput request) {
		User client = userService.usuarioAutenticado();
		if (client.getRole() != UserRole.CLIENT) {
			throw new AccessDeniedException("Somente um CLIENT pode contratar um serviço");
		}
		if (!Boolean.TRUE.equals(client.getActive())) {
			throw new BusinessException(HttpStatus.BAD_REQUEST, "O cliente autenticado está inativo");
		}
		OfferedService service = offeredServiceService.buscarEntidade(request.serviceId());
		validarServicoAtivo(service);
		ServiceRequest entity = ServiceRequest.builder()
				.service(service)
				.client(client)
				.status(RequestStatus.PENDING)
				.scheduledAt(request.scheduledAt())
				.notes(request.notes())
				.totalPrice(service.getPrice())
				.build();
		ServiceRequest salvo = serviceRequestRepository.save(entity);
		return EntityMapper.toResponse(buscarEntidade(salvo.getId()));
	}

	@Transactional(readOnly = true)
	public List<ServiceRequestResponse> listar(RequestStatus status, Long clientId, Long serviceId) {
		User current = userService.usuarioAutenticado();
		Long filtroCliente = clientId;
		Long filtroPrestador = null;
		if (current.getRole() == UserRole.CLIENT) {
			filtroCliente = current.getId();
		} else if (current.getRole() == UserRole.PROVIDER) {
			filtroPrestador = current.getId();
		}
		return serviceRequestRepository.findAllWithRelations(status, filtroCliente, serviceId, filtroPrestador)
				.stream()
				.map(EntityMapper::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public ServiceRequestResponse buscarPorId(Long id) {
		ServiceRequest entity = buscarEntidade(id);
		garantirAcesso(entity);
		return EntityMapper.toResponse(entity);
	}

	@Transactional
	public ServiceRequestResponse atualizar(Long id, ServiceRequestInput request) {
		ServiceRequest entity = buscarEntidade(id);
		garantirAcesso(entity);
		User current = userService.usuarioAutenticado();
		OfferedService service = offeredServiceService.buscarEntidade(request.serviceId());
		if (current.getRole() == UserRole.CLIENT) {
			validarServicoAtivo(service);
			entity.setService(service);
			entity.setScheduledAt(request.scheduledAt());
			entity.setNotes(request.notes());
			if (request.status() != null && request.status() != RequestStatus.CANCELLED) {
				throw new AccessDeniedException("O cliente só pode cancelar a própria solicitação");
			}
			if (request.status() == RequestStatus.CANCELLED) {
				entity.setStatus(RequestStatus.CANCELLED);
			}
		} else if (current.getRole() == UserRole.PROVIDER) {
			if (request.status() != null) {
				entity.setStatus(request.status());
			}
		} else {
			validarServicoAtivo(service);
			entity.setService(service);
			entity.setScheduledAt(request.scheduledAt());
			entity.setNotes(request.notes());
			if (request.status() != null) {
				entity.setStatus(request.status());
			}
			entity.setTotalPrice(service.getPrice());
		}
		serviceRequestRepository.save(entity);
		return EntityMapper.toResponse(buscarEntidade(id));
	}

	@Transactional
	public void excluir(Long id) {
		ServiceRequest entity = buscarEntidade(id);
		garantirAcesso(entity);
		if (reviewRepository.existsByRequestId(id)) {
			throw new BusinessException(HttpStatus.CONFLICT, "Não é possível excluir a solicitação porque existe uma avaliação vinculada");
		}
		serviceRequestRepository.delete(entity);
	}

	@Transactional(readOnly = true)
	public ServiceRequest buscarEntidade(Long id) {
		return serviceRequestRepository.findByIdWithRelations(id)
				.orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada com id " + id));
	}

	private void garantirAcesso(ServiceRequest entity) {
		User current = userService.usuarioAutenticado();
		if (current.getRole() == UserRole.ADMIN) {
			return;
		}
		if (current.getRole() == UserRole.CLIENT && entity.getClient().getId().equals(current.getId())) {
			return;
		}
		if (current.getRole() == UserRole.PROVIDER && entity.getService().getProvider().getId().equals(current.getId())) {
			return;
		}
		throw new AccessDeniedException("Você não pode acessar a solicitação de outro usuário");
	}

	private void validarServicoAtivo(OfferedService service) {
		if (!Boolean.TRUE.equals(service.getActive())) {
			throw new BusinessException(HttpStatus.BAD_REQUEST, "O serviço informado está inativo");
		}
	}
}
