package com.petservicehub.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

	private SecurityUtils() {
	}

	public static AppUserDetails currentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof AppUserDetails details)) {
			throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("Não autenticado");
		}
		return details;
	}
}
