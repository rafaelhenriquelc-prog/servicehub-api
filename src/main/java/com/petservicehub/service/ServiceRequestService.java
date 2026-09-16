package com.petservicehub.service;

import java.util.List;

import org.springframework.http.HttpStatus;
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
		OfferedService service = offeredServiceService.buscarEntidade(request.serviceId());
		User client = clienteAtivo(request.clientId());
		validarServicoAtivo(service);
		ServiceRequest entity = ServiceRequest.builder()
				.service(service)
				.client(client)
				.status(request.status() != null ? request.status() : RequestStatus.PENDING)
				.scheduledAt(request.scheduledAt())
				.notes(request.notes())
				.totalPrice(service.getPrice())
				.build();
		ServiceRequest salvo = serviceRequestRepository.save(entity);
		return EntityMapper.toResponse(buscarEntidade(salvo.getId()));
	}

	@Transactional(readOnly = true)
	public List<ServiceRequestResponse> listar(RequestStatus status, Long clientId, Long serviceId) {
		return serviceRequestRepository.findAllWithRelations(status, clientId, serviceId)
				.stream()
				.map(EntityMapper::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public ServiceRequestResponse buscarPorId(Long id) {
		return EntityMapper.toResponse(buscarEntidade(id));
	}

	@Transactional
	public ServiceRequestResponse atualizar(Long id, ServiceRequestInput request) {
		ServiceRequest entity = buscarEntidade(id);
		OfferedService service = offeredServiceService.buscarEntidade(request.serviceId());
		User client = clienteAtivo(request.clientId());
		validarServicoAtivo(service);
		entity.setService(service);
		entity.setClient(client);
		entity.setScheduledAt(request.scheduledAt());
		entity.setNotes(request.notes());
		entity.setTotalPrice(service.getPrice());
		if (request.status() != null) {
			entity.setStatus(request.status());
		}
		serviceRequestRepository.save(entity);
		return EntityMapper.toResponse(buscarEntidade(id));
	}

	@Transactional
	public void excluir(Long id) {
		ServiceRequest entity = buscarEntidade(id);
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

	private User clienteAtivo(Long clientId) {
		User client = userService.buscarEntidade(clientId);
		if (client.getRole() != UserRole.CLIENT) {
			throw new BusinessException(HttpStatus.BAD_REQUEST, "Somente um usuário CLIENT pode contratar um serviço");
		}
		if (!Boolean.TRUE.equals(client.getActive())) {
			throw new BusinessException(HttpStatus.BAD_REQUEST, "O cliente informado está inativo");
		}
		return client;
	}

	private void validarServicoAtivo(OfferedService service) {
		if (!Boolean.TRUE.equals(service.getActive())) {
			throw new BusinessException(HttpStatus.BAD_REQUEST, "O serviço informado está inativo");
		}
	}
}
