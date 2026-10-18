package com.ojt_22.mmspg.security;

import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.ojt_22.mmspg.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthenticatedMerchantService {
    private final MerchantRepository merchantRepository;

    public UUID resolveMerchantId() {
        Authentication authentication = currentAuthentication();
        if (hasRole(authentication, "API_CLIENT")) {
            try { return UUID.fromString(authentication.getName()); }
            catch (IllegalArgumentException ex) { throw new AccessDeniedException("Invalid API client merchant identity"); }
        }
        if (hasRole(authentication, "MERCHANT")) {
            return merchantRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new AccessDeniedException("Authenticated merchant was not found")).getId();
        }
        throw new AccessDeniedException("A merchant identity is required");
    }

    public void assertMerchantAccess(UUID merchantId) {
        Authentication authentication = currentAuthentication();
        if (hasRole(authentication, "ADMIN") || hasRole(authentication, "STAFF")) return;
        if (!resolveMerchantId().equals(merchantId)) throw new AccessDeniedException("You cannot access another merchant's data");
    }

    private Authentication currentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal()))
            throw new AccessDeniedException("Authentication is required");
        return authentication;
    }
    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream().anyMatch(a -> ("ROLE_" + role).equals(a.getAuthority()));
    }
}
