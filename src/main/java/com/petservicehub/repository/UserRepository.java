package com.petservicehub.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.petservicehub.model.User;
import com.petservicehub.model.UserRole;

public interface UserRepository extends JpaRepository<User, Long> {

	List<User> findByActive(Boolean active);

	List<User> findByRole(UserRole role);

	List<User> findByRoleAndActive(UserRole role, Boolean active);

	boolean existsByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
