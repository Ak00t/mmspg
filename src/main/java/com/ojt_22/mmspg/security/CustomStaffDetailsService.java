package com.ojt_22.mmspg.security;

import java.util.Collections;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ojt_22.mmspg.entity.StaffUser;
import com.ojt_22.mmspg.repository.StaffUserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomStaffDetailsService implements UserDetailsService {

	private final StaffUserRepository staffUserRepository;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		StaffUser staff = staffUserRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("Staff not found with email: " + email));

		if (!"ACTIVE".equalsIgnoreCase(staff.getStatus()
				.name())) {
			throw new DisabledException("Staff account is not active. Current status: " + staff.getStatus());
		}

		return new org.springframework.security.core.userdetails.User(staff.getEmail(), staff.getPasswordHash(),
				Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + staff.getRole()
						.name())));
	}
}
