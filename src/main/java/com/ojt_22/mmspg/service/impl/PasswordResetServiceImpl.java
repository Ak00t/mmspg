package com.ojt_22.mmspg.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.entity.StaffUser;
import com.ojt_22.mmspg.repository.MerchantRepository;
import com.ojt_22.mmspg.repository.StaffUserRepository;
import com.ojt_22.mmspg.service.PasswordResetService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    private final StaffUserRepository staffUserRepository;
    private final MerchantRepository merchantRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void forgotPassword(String email) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(15);
        boolean found = false;

        Optional<StaffUser> staffOpt = staffUserRepository.findByEmail(email);
        if (staffOpt.isPresent()) {
            StaffUser staff = staffOpt.get();
            staff.setResetToken(token);
            staff.setResetTokenExpiry(expiry);
            staffUserRepository.save(staff);
            found = true;
        } else {
            Optional<Merchant> merchantOpt = merchantRepository.findByEmail(email);
            if (merchantOpt.isPresent()) {
                Merchant merchant = merchantOpt.get();
                merchant.setResetToken(token);
                merchant.setResetTokenExpiry(expiry);
                merchantRepository.save(merchant);
                found = true;
            }
        }

        if (found) {
            log.info("Generated Password Reset Token for {}: {}", email, token);
            // TODO: Implement JavaMailSender logic here to actually email the token
        } else {
            log.warn("Forgot password requested for non-existent email: {}", email);
        }
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        
        Optional<StaffUser> staffOpt = staffUserRepository.findByResetToken(token);
        if (staffOpt.isPresent()) {
            StaffUser staff = staffOpt.get();
            if (staff.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Reset token has expired");
            }
            staff.setPasswordHash(passwordEncoder.encode(newPassword));
            staff.setResetToken(null);
            staff.setResetTokenExpiry(null);
            staffUserRepository.save(staff);
            return;
        }

        Optional<Merchant> merchantOpt = merchantRepository.findByResetToken(token);
        if (merchantOpt.isPresent()) {
            Merchant merchant = merchantOpt.get();
            if (merchant.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Reset token has expired");
            }
            merchant.setPasswordHash(passwordEncoder.encode(newPassword));
            merchant.setResetToken(null);
            merchant.setResetTokenExpiry(null);
            merchantRepository.save(merchant);
            return;
        }

        throw new IllegalArgumentException("Invalid reset token");
    }
}
