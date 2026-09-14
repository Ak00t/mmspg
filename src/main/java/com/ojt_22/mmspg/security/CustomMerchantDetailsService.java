package com.ojt_22.mmspg.security;

import java.util.Collections;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.repository.MerchantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomMerchantDetailsService implements UserDetailsService {

    private final MerchantRepository merchantRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Merchant merchant = merchantRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Merchant not found with email: " + email));

        if (!"ACTIVE".equalsIgnoreCase(merchant.getStatus())) {
            throw new DisabledException("Merchant account is not active. Current status: " + merchant.getStatus());
        }

        return new org.springframework.security.core.userdetails.User(
                merchant.getEmail(),
                merchant.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_MERCHANT"))
        );
    }
}
