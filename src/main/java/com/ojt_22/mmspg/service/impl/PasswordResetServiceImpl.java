package com.ojt_22.mmspg.service.impl;



import java.time.LocalDateTime;

import java.util.Optional;

import java.util.UUID;



import org.springframework.beans.factory.annotation.Value;

import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.mail.javamail.MimeMessageHelper;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;



import com.ojt_22.mmspg.entity.Merchant;

import com.ojt_22.mmspg.entity.StaffUser;

import com.ojt_22.mmspg.repository.MerchantRepository;

import com.ojt_22.mmspg.repository.StaffUserRepository;

import com.ojt_22.mmspg.service.PasswordResetService;



import jakarta.mail.internet.MimeMessage;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;



@Service

@RequiredArgsConstructor

@Slf4j

public class PasswordResetServiceImpl implements PasswordResetService {



    private final StaffUserRepository staffUserRepository;

    private final MerchantRepository merchantRepository;

    private final PasswordEncoder passwordEncoder;

    private final JavaMailSender mailSender;



    @Value("${spring.mail.username:winwin555620@gmail.com}")

    private String fromEmail;



    @Value("${app.frontend.url:http://localhost:5173}")

    private String frontendUrl;



    @Value("${app.jwt.reset-token.expiration-minutes:10}")

    private int resetTokenExpiryMinutes;

    

    

    @Override
    @Transactional
    public void forgotPassword(String email) {
        Optional<StaffUser> staffOpt = staffUserRepository.findByEmail(email);
        Optional<Merchant> merchantOpt = staffOpt.isEmpty() ? merchantRepository.findByEmail(email) : Optional.empty();

        if (staffOpt.isEmpty() && merchantOpt.isEmpty()) {
            log.warn("Forgot password requested for non-existent email: {}", email);
            throw new IllegalArgumentException("This email address is not registered in our system.");
        }

        String token = String.format("%06d", new java.security.SecureRandom().nextInt(1_000_000));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(resetTokenExpiryMinutes);

        if (staffOpt.isPresent()) {
            StaffUser staff = staffOpt.get();
            staff.setResetToken(token);
            staff.setResetTokenExpiry(expiry);
            staffUserRepository.save(staff);
        } else if (merchantOpt.isPresent()) {
            Merchant merchant = merchantOpt.get();
            merchant.setResetToken(token);
            merchant.setResetTokenExpiry(expiry);
            merchantRepository.save(merchant);
        }

        log.info("Generated Password Reset Code for {}: {}", email, token);
        sendPasswordResetEmail(email, token);
    }

    private void sendPasswordResetEmail(String toEmail, String token) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "MMSPG");
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Request - MMSPG");

            String htmlContent = "<!DOCTYPE html>"
                    + "<html>"
                    + "<head>"
                    + "<meta charset=\"UTF-8\">"
                    + "<style>"
                    + "  body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f8fafc; margin: 0; padding: 24px; color: #1e293b; }"
                    + "  .container { max-width: 520px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.08); border: 1px solid #e2e8f0; }"
                    + "  .header { background: #2563eb; padding: 24px; text-align: center; color: #ffffff; }"
                    + "  .header h1 { margin: 0; font-size: 20px; font-weight: 700; }"
                    + "  .content { padding: 32px 24px; font-size: 15px; line-height: 1.6; color: #334155; }"
                    + "  .greeting { font-size: 16px; margin-bottom: 20px; color: #0f172a; }"
                    + "  .greeting a { color: #2563eb; text-decoration: underline; }"
                    + "  .code-title { margin: 24px 0 10px 0; font-weight: 600; color: #1e293b; }"
                    + "  .code-box { background-color: #eff6ff; border: 2px dashed #93c5fd; padding: 16px; margin: 12px 0 20px 0; text-align: center; border-radius: 8px; font-size: 32px; font-weight: 800; letter-spacing: 6px; color: #1d4ed8; font-family: monospace; }"
                    + "  .btn-wrapper { text-align: center; margin: 24px 0; }"
                    + "  .btn { display: inline-block; background-color: #2563eb; color: #ffffff !important; padding: 12px 28px; font-size: 14px; font-weight: 600; text-decoration: none; border-radius: 8px; }"
                    + "  .expiry { font-size: 13px; color: #64748b; margin-top: 16px; }"
                    + "  .footer { background-color: #f8fafc; padding: 16px 24px; text-align: center; font-size: 12px; color: #94a3b8; border-top: 1px solid #e2e8f0; }"
                    + "</style>"
                    + "</head>"
                    + "<body>"
                    + "<div class=\"container\">"
                    + "  <div class=\"header\">"
                    + "    <h1>MMSPG Payment Gateway</h1>"
                    + "  </div>"
                    + "  <div class=\"content\">"
                    + "    <p class=\"greeting\">Hi <a href=\"mailto:" + toEmail + "\">" + toEmail + "</a>,</p>"
                    + "    <p class=\"code-title\">Your Password Reset Code is: " + token + "</p>"
                    + "    <div class=\"code-box\">" + token + "</div>"
                    + "    <p class=\"expiry\">This code will expire in " + resetTokenExpiryMinutes + " minutes.</p>"
                    + "    <div class=\"btn-wrapper\">"
                    + "      <a href=\"" + resetLink + "\" class=\"btn\">Reset Password Directly</a>"
                    + "    </div>"
                    + "    <p style=\"font-size: 12px; color: #94a3b8; margin-top: 24px;\">If you did not request a password reset, you can safely ignore this email.</p>"
                    + "  </div>"
                    + "  <div class=\"footer\">"
                    + "    &copy; 2026 MMSPG Payment Gateway System. All rights reserved."
                    + "  </div>"
                    + "</div>"
                    + "</body>"
                    + "</html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Successfully sent password reset email to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Email delivery failed: " + e.getMessage(), e);
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



