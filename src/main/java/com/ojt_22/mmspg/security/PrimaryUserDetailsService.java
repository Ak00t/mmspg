package com.ojt_22.mmspg.security;

import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@Primary
@RequiredArgsConstructor
public class PrimaryUserDetailsService implements UserDetailsService {

    private final CustomStaffDetailsService customStaffDetailsService;
    private final CustomMerchantDetailsService customMerchantDetailsService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            // First attempt to load as Staff/Admin
            return customStaffDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            // If not found in staff, attempt to load as Merchant
            return customMerchantDetailsService.loadUserByUsername(username);
        }
    }
}
