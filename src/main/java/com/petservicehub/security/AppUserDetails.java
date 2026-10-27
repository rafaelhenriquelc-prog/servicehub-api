package com.petservicehub.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.petservicehub.model.User;
import com.petservicehub.model.UserRole;

public class AppUserDetails implements UserDetails {

	private final Long id;
	private final String email;
	private final String passwordHash;
	private final UserRole role;
	private final boolean active;

	public AppUserDetails(User user) {
		this.id = user.getId();
		this.email = user.getEmail();
		this.passwordHash = user.getPasswordHash();
		this.role = user.getRole();
		this.active = Boolean.TRUE.equals(user.getActive());
	}

	public Long getId() {
		return id;
	}

	public UserRole getRole() {
		return role;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}

	@Override
	public String getPassword() {
		return passwordHash;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return active;
	}
}
