package com.petservicehub.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petservicehub.dto.EntityMapper;
import com.petservicehub.dto.UserRequest;
import com.petservicehub.dto.UserResponse;
import com.petservicehub.exception.BusinessException;
import com.petservicehub.exception.ResourceNotFoundException;
import com.petservicehub.model.User;
import com.petservicehub.model.UserRole;
import com.petservicehub.repository.OfferedServiceRepository;
import com.petservicehub.repository.ReviewRepository;
import com.petservicehub.repository.ServiceRequestRepository;
import com.petservicehub.repository.UserRepository;
import com.petservicehub.util.PasswordHasher;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final OfferedServiceRepository offeredServiceRepository;
	private final ServiceRequestRepository serviceRequestRepository;
	private final ReviewRepository reviewRepository;

	@Transactional
	public UserResponse criar(UserRequest request) {
		validarEmailUnico(request.email(), null);
		User user = User.builder()
				.fullName(request.fullName())
				.email(request.email())
				.passwordHash(PasswordHasher.hash(request.password()))
				.phone(request.phone())
				.bio(request.bio())
				.avatarUrl(request.avatarUrl())
				.role(request.role())
				.active(request.active() != null ? request.active() : true)
				.build();
		return EntityMapper.toResponse(userRepository.save(user));
	}

	@Transactional(readOnly = true)
	public List<UserResponse> listar(UserRole role, Boolean active) {
		List<User> users;
		if (role != null && active != null) {
			users = userRepository.findByRoleAndActive(role, active);
		} else if (role != null) {
			users = userRepository.findByRole(role);
		} else if (active != null) {
			users = userRepository.findByActive(active);
		} else {
			users = userRepository.findAll();
		}
		return users.stream().map(EntityMapper::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public UserResponse buscarPorId(Long id) {
		return EntityMapper.toResponse(buscarEntidade(id));
	}

	@Transactional
	public UserResponse atualizar(Long id, UserRequest request) {
		User user = buscarEntidade(id);
		validarEmailUnico(request.email(), id);
		user.setFullName(request.fullName());
		user.setEmail(request.email());
		user.setPasswordHash(PasswordHasher.hash(request.password()));
		user.setPhone(request.phone());
		user.setBio(request.bio());
		user.setAvatarUrl(request.avatarUrl());
		user.setRole(request.role());
		if (request.active() != null) {
			user.setActive(request.active());
		}
		return EntityMapper.toResponse(userRepository.save(user));
	}

	@Transactional
	public void excluir(Long id) {
		User user = buscarEntidade(id);
		if (offeredServiceRepository.existsByProviderId(id)) {
			throw new BusinessException(HttpStatus.CONFLICT, "Não é possível excluir o usuário porque existem serviços vinculados");
		}
		if (serviceRequestRepository.existsByClientId(id)) {
			throw new BusinessException(HttpStatus.CONFLICT, "Não é possível excluir o usuário porque existem solicitações vinculadas");
		}
		if (reviewRepository.existsByReviewerId(id)) {
			throw new BusinessException(HttpStatus.CONFLICT, "Não é possível excluir o usuário porque existem avaliações vinculadas");
		}
		userRepository.delete(user);
	}

	@Transactional(readOnly = true)
	public User buscarEntidade(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com id " + id));
	}

	private void validarEmailUnico(String email, Long idAtual) {
		boolean emailEmUso = idAtual == null
				? userRepository.existsByEmailIgnoreCase(email)
				: userRepository.existsByEmailIgnoreCaseAndIdNot(email, idAtual);
		if (emailEmUso) {
			throw new BusinessException(HttpStatus.CONFLICT, "Já existe um usuário cadastrado com o e-mail " + email);
		}
	}
}
