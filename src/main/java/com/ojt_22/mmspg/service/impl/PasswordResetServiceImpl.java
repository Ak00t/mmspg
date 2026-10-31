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

        String token = UUID.randomUUID().toString();

        LocalDateTime expiry = LocalDateTime.now().plusMinutes(resetTokenExpiryMinutes);

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

            sendPasswordResetEmail(email, token);

        } else {

            log.warn("Forgot password requested for non-existent email: {}", email);

        }

    }



    

    private void sendPasswordResetEmail(String toEmail, String token) {

        String resetLink = frontendUrl + "/reset-password?token=" + token;



        try {

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");



            helper.setFrom(fromEmail, "MMSPG Support");

            helper.setTo(toEmail);

            helper.setSubject("Password Reset Request - MMSPG Payment Gateway");



            String htmlContent = "<!DOCTYPE html>"

                    + "<html>"

                    + "<head>"

                    + "<meta charset=\"UTF-8\">"

                    + "<style>"

                    + "  body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f8fafc; margin: 0; padding: 20px; color: #334155; }"

                    + "  .container { max-width: 580px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); border: 1px solid #e2e8f0; }"

                    + "  .header { background: linear-gradient(135deg, #2563eb, #1d4ed8); padding: 32px 24px; text-align: center; color: #ffffff; }"

                    + "  .header h1 { margin: 0; font-size: 24px; font-weight: 700; letter-spacing: -0.5px; }"

                    + "  .header p { margin: 8px 0 0; font-size: 14px; opacity: 0.9; }"

                    + "  .content { padding: 32px 24px; }"

                    + "  .btn-wrapper { text-align: center; margin: 30px 0; }"

                    + "  .btn { display: inline-block; background-color: #2563eb; color: #ffffff !important; padding: 14px 32px; font-size: 15px; font-weight: 600; text-decoration: none; border-radius: 8px; box-shadow: 0 2px 4px rgba(37, 99, 235, 0.2); }"

                    + "  .token-box { background-color: #f1f5f9; border-left: 4px solid #2563eb; padding: 16px; margin: 24px 0; border-radius: 4px; }"

                    + "  .token-box p { margin: 0 0 6px 0; font-size: 12px; text-transform: uppercase; letter-spacing: 0.5px; color: #64748b; font-weight: 600; }"

                    + "  .token-box code { font-size: 15px; font-family: monospace; color: #0f172a; word-break: break-all; font-weight: bold; }"

                    + "  .footer { background-color: #f8fafc; padding: 20px 24px; text-align: center; font-size: 12px; color: #94a3b8; border-top: 1px solid #e2e8f0; }"

                    + "</style>"

                    + "</head>"

                    + "<body>"

                    + "<div class=\"container\">"

                    + "  <div class=\"header\">"

                    + "    <h1>MMSPG Payment Gateway</h1>"

                    + "    <p>Password Reset Request</p>"

                    + "  </div>"

                    + "  <div class=\"content\">"

                    + "    <p>Hello,</p>"

                    + "    <p>We received a request to reset the password for your account (<strong>" + toEmail + "</strong>). Click the button below to choose a new password:</p>"

                    + "    <div class=\"btn-wrapper\">"

                    + "      <a href=\"" + resetLink + "\" class=\"btn\">Reset Password</a>"

                    + "    </div>"

                    + "    <div class=\"token-box\">"

                    + "      <p>Reset Token</p>"

                    + "      <code>" + token + "</code>"

                    + "    </div>"

                    + "    <p style=\"font-size: 13px; color: #64748b;\">If the button above does not work, copy and paste this link into your browser:<br>"

                    + "    <a href=\"" + resetLink + "\" style=\"color: #2563eb; word-break: break-all;\">" + resetLink + "</a></p>"

                    + "    <p style=\"font-size: 13px; color: #e11d48; margin-top: 24px;\">&#9888;&#65039; <strong>Notice:</strong> This reset token and link will expire in <strong>" + resetTokenExpiryMinutes + " minutes</strong>.</p>"

                    + "    <p style=\"font-size: 13px; color: #64748b;\">If you did not request a password reset, you can safely ignore this email or reach out to our security support team.</p>"

                    + "  </div>"

                    + "  <div class=\"footer\">"

                    + "    <p>&copy; 2026 MMSPG Payment Gateway System. All rights reserved.</p>"

                    + "  </div>"

                    + "</div>"

                    + "</body>"

                    + "</html>";



            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info("Successfully sent password reset email to: {}", toEmail);



        } catch (Exception e) {

            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage(), e);

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



