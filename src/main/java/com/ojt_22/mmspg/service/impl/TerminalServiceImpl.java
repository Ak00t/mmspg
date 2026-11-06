package com.ojt_22.mmspg.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ojt_22.mmspg.dto.TerminalRequestDto;
import com.ojt_22.mmspg.dto.TerminalResponseDto;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.entity.MerchantBranch;
import com.ojt_22.mmspg.entity.StaffUser;
import com.ojt_22.mmspg.entity.Terminal;
import com.ojt_22.mmspg.enums.TerminalStatus;
import com.ojt_22.mmspg.enums.TerminalType;
import com.ojt_22.mmspg.repository.BranchRepository;
import com.ojt_22.mmspg.repository.MerchantRepository;
import com.ojt_22.mmspg.repository.StaffUserRepository;
import com.ojt_22.mmspg.repository.TerminalRepository;
import com.ojt_22.mmspg.service.NotificationService;
import com.ojt_22.mmspg.service.TerminalService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TerminalServiceImpl implements TerminalService {

	private final TerminalRepository terminalRepository;
	private final MerchantRepository merchantRepository;
	private final BranchRepository branchRepository;
	private final StaffUserRepository staffUserRepository;
	private final NotificationService notificationService;

	@Override
	@Transactional
	public TerminalResponseDto provisionTerminal(TerminalRequestDto request) {
		if (request.getMerchantId() == null) {
			throw new IllegalArgumentException("Merchant is required");
		}
		if (request.getBranchId() == null) {
			throw new IllegalArgumentException("Branch is required");
		}

		Merchant merchant = merchantRepository.findById(request.getMerchantId())
				.orElseThrow(
						() -> new IllegalArgumentException("Merchant not found with ID: " + request.getMerchantId()));

		MerchantBranch branch = branchRepository.findById(request.getBranchId())
				.orElseThrow(() -> new IllegalArgumentException("Branch not found with ID: " + request.getBranchId()));

		if (!branch.getMerchant().getId().equals(merchant.getId())) {
			throw new IllegalArgumentException("Branch does not belong to the specified Merchant.");
		}

		String type = request.getTerminalType() != null ? request.getTerminalType().trim().toUpperCase() : "PHYSICAL_POS";
		if (!type.equals("PHYSICAL_POS") && !type.equals("VIRTUAL_API")) {
			throw new IllegalArgumentException("Invalid Terminal Type. Must be PHYSICAL_POS or VIRTUAL_API.");
		}

		String code = request.getTerminalCode();
		if (code == null || code.trim().isEmpty()) {
			String prefix = type.equals("PHYSICAL_POS") ? "POS-" : "API-";
			code = prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
		} else {
			code = code.trim();
		}

		if (terminalRepository.existsByTerminalCode(code)) {
			throw new IllegalArgumentException("Terminal code already exists: " + code);
		}

		StaffUser currentStaff = getCurrentStaffUser();

		Terminal terminal = new Terminal();
		terminal.setMerchant(merchant); 
		terminal.setBranch(branch);
		terminal.setTerminalCode(code);
		terminal.setTerminalName(request.getTerminalName() != null ? request.getTerminalName().trim() : "Terminal " + code);
		terminal.setTerminalType(TerminalType.valueOf(type));
		terminal.setStatus(TerminalStatus.ONLINE);
		terminal.setCreatedBy(currentStaff);

		Terminal savedTerminal = terminalRepository.save(terminal);

		notificationService.sendNotificationToCurrentStaff(
				"Terminal Provisioned",
				"You provisioned terminal \"" + savedTerminal.getTerminalCode() + "\" (" + savedTerminal.getTerminalType() + ") for merchant \"" + merchant.getBusinessName() + "\".",
				"TERMINAL"
		);

		// Broadcast to all other staff/admins
		notificationService.sendNotificationToAllStaff(
				"Terminal Provisioned",
				"Terminal \"" + savedTerminal.getTerminalCode() + "\" (" + savedTerminal.getTerminalType() + ") has been provisioned for merchant \"" + merchant.getBusinessName() + "\".",
				"TERMINAL"
		);

		return mapToResponseDto(savedTerminal);
	}

	@Override
	@Transactional(readOnly = true)
	public List<TerminalResponseDto> getAllTerminals() {
		return terminalRepository.findAllWithDetails()
				.stream()
				.map(this::mapToResponseDto)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public void updateTerminalStatus(UUID terminalId, String status) {
		Terminal terminal = terminalRepository.findById(terminalId)
				.orElseThrow(() -> new IllegalArgumentException("Terminal not found with ID: " + terminalId));

		String upperStatus = status != null ? status.trim().toUpperCase() : "";
		if (upperStatus.equals("MAINTENANCE") || upperStatus.equals("INACTIVE")) {
			upperStatus = "OFFLINE";
		}
		if (upperStatus.equals("BAN") || upperStatus.equals("BANNED")) {
			upperStatus = "SUSPENDED";
		}
		if (upperStatus.equals("CANCEL_BAN") || upperStatus.equals("CANCELBAN") || upperStatus.equals("UNBAN") || upperStatus.equals("ACTIVE")) {
			upperStatus = "ONLINE";
		}

		if (!upperStatus.equals("ONLINE") && !upperStatus.equals("OFFLINE") && !upperStatus.equals("SUSPENDED")) {
			throw new IllegalArgumentException("Invalid status. Must be ONLINE, OFFLINE, SUSPENDED, MAINTENANCE, or INACTIVE.");
		}

		terminal.setStatus(TerminalStatus.valueOf(upperStatus));
		terminal.setUpdatedAt(LocalDateTime.now());
		terminalRepository.save(terminal);

		notificationService.sendNotificationToCurrentStaff(
				"Terminal Status Changed",
				"You updated terminal \"" + terminal.getTerminalCode() + "\" status to " + upperStatus + ".",
				"TERMINAL"
		);

		// Broadcast to other staff/admins
		notificationService.sendNotificationToAllStaff(
				"Terminal Status Changed",
				"Terminal \"" + terminal.getTerminalCode() + "\" status was updated to " + upperStatus + ".",
				"TERMINAL"
		);
	}

	@Override
	@Transactional
	public TerminalResponseDto updateTerminal(UUID id, TerminalRequestDto request) {
		Terminal terminal = terminalRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Terminal not found with ID: " + id));

		if (request.getTerminalName() != null && !request.getTerminalName().trim().isEmpty()) {
			terminal.setTerminalName(request.getTerminalName().trim());
		}
		
		if (request.getTerminalType() != null && !request.getTerminalType().trim().isEmpty()) {
			String type = request.getTerminalType().toUpperCase();
			if (!type.equals("PHYSICAL_POS") && !type.equals("VIRTUAL_API")) {
				throw new IllegalArgumentException("Invalid Terminal Type. Must be PHYSICAL_POS or VIRTUAL_API.");
			}
			terminal.setTerminalType(TerminalType.valueOf(type));
		}

		terminal.setUpdatedAt(LocalDateTime.now());
		
		Terminal updatedTerminal = terminalRepository.save(terminal);
		
		notificationService.sendNotificationToCurrentStaff(
				"Terminal Updated",
				"Details for terminal \"" + updatedTerminal.getTerminalCode() + "\" have been successfully updated.",
				"TERMINAL"
		);
		return mapToResponseDto(updatedTerminal);
	}
	
	private TerminalResponseDto mapToResponseDto(Terminal terminal) {
		TerminalResponseDto dto = new TerminalResponseDto();
		dto.setTerminalId(terminal.getId());
		dto.setTerminalCode(terminal.getTerminalCode());
		dto.setTerminalName(terminal.getTerminalName());
		dto.setTerminalType(terminal.getTerminalType().name());
        
		dto.setMerchantId(terminal.getMerchant().getId());
		dto.setMerchantName(terminal.getMerchant().getBusinessName());
        
		dto.setBranchId(terminal.getBranch().getId());
		dto.setBranchName(terminal.getBranch().getBranchName());
		dto.setStatus(terminal.getStatus().name());
		if (terminal.getCreatedBy() != null) {
			dto.setCreatedByStaffId(terminal.getCreatedBy().getId());
			dto.setCreatedByName(terminal.getCreatedBy().getFullName());
		}
		dto.setCreatedAt(terminal.getCreatedAt());
		return dto;
	}

	private StaffUser getCurrentStaffUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()
				|| "anonymousUser".equals(authentication.getPrincipal())) {
			throw new AccessDeniedException("Authentication is required to provision a terminal");
		}
		String identifier = authentication.getName();
		try {
			UUID staffId = UUID.fromString(identifier);
			return staffUserRepository.findById(staffId)
					.orElseThrow(() -> new AccessDeniedException("Authenticated staff user was not found for ID: " + staffId));
		} catch (IllegalArgumentException e) {
			return staffUserRepository.findByEmail(identifier)
					.orElseThrow(() -> new AccessDeniedException("Authenticated staff user was not found for email: " + identifier));
		}
	}
}
