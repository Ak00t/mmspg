package com.ojt_22.mmspg.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ojt_22.mmspg.dto.TerminalRequestDto;
import com.ojt_22.mmspg.dto.TerminalResponseDto;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.entity.MerchantBranch;
import com.ojt_22.mmspg.entity.Terminal;
import com.ojt_22.mmspg.repository.BranchRepository;
import com.ojt_22.mmspg.repository.MerchantRepository;
import com.ojt_22.mmspg.repository.TerminalRepository;
import com.ojt_22.mmspg.service.TerminalService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TerminalServiceImpl implements TerminalService {

    private final TerminalRepository terminalRepository;
    private final MerchantRepository merchantRepository;
    private final BranchRepository branchRepository;

    @Override
    @Transactional
    public TerminalResponseDto provisionTerminal(TerminalRequestDto request) {
        Merchant merchant = merchantRepository.findById(request.getMerchantId())
                .orElseThrow(() -> new IllegalArgumentException("Merchant not found with ID: " + request.getMerchantId()));

        MerchantBranch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new IllegalArgumentException("Branch not found with ID: " + request.getBranchId()));

        if (!branch.getMerchant().getId().equals(merchant.getId())) {
            throw new IllegalArgumentException("Branch does not belong to the specified Merchant.");
        }

        if (terminalRepository.existsByTerminalCode(request.getTerminalCode())) {
            throw new IllegalArgumentException("Terminal code already exists.");
        }

        String type = request.getTerminalType().toUpperCase();
        if (!type.equals("PHYSICAL_POS") && !type.equals("VIRTUAL_API")) {
            throw new IllegalArgumentException("Invalid Terminal Type. Must be PHYSICAL_POS or VIRTUAL_API.");
        }

        Terminal terminal = new Terminal();
        terminal.setMerchant(merchant);
        terminal.setBranch(branch);
        terminal.setTerminalCode(request.getTerminalCode());
        terminal.setTerminalName(request.getTerminalName());
        terminal.setTerminalType(type);
        terminal.setStatus("ONLINE");
        terminal.setUpdatedAt(LocalDateTime.now());
        
        Terminal savedTerminal = terminalRepository.save(terminal);
        return mapToResponseDto(savedTerminal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TerminalResponseDto> getAllTerminals() {
        return terminalRepository.findAllWithDetails().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateTerminalStatus(UUID terminalId, String status) {
        Terminal terminal = terminalRepository.findById(terminalId)
                .orElseThrow(() -> new IllegalArgumentException("Terminal not found with ID: " + terminalId));
        
        String upperStatus = status.toUpperCase();
        if (!upperStatus.equals("ONLINE") && !upperStatus.equals("OFFLINE") && !upperStatus.equals("SUSPENDED")) {
            throw new IllegalArgumentException("Invalid status. Must be ONLINE, OFFLINE, or SUSPENDED.");
        }
        
        terminal.setStatus(upperStatus);
        terminal.setUpdatedAt(LocalDateTime.now());
        terminalRepository.save(terminal);
    }

    private TerminalResponseDto mapToResponseDto(Terminal terminal) {
        TerminalResponseDto dto = new TerminalResponseDto();
        dto.setTerminalId(terminal.getId());
        dto.setTerminalCode(terminal.getTerminalCode());
        dto.setTerminalName(terminal.getTerminalName());
        dto.setTerminalType(terminal.getTerminalType());
        dto.setMerchantId(terminal.getMerchant().getId());
        dto.setMerchantName(terminal.getMerchant().getBusinessName());
        dto.setBranchId(terminal.getBranch().getId());
        dto.setBranchName(terminal.getBranch().getBranchName());
        dto.setStatus(terminal.getStatus());
        dto.setCreatedAt(terminal.getCreatedAt());
        return dto;
    }
}
