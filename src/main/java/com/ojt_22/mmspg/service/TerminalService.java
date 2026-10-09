package com.ojt_22.mmspg.service;

import java.util.List;
import java.util.UUID;
import com.ojt_22.mmspg.dto.TerminalRequestDto;
import com.ojt_22.mmspg.dto.TerminalResponseDto;

public interface TerminalService {
    TerminalResponseDto provisionTerminal(TerminalRequestDto request);
    List<TerminalResponseDto> getAllTerminals();
    void updateTerminalStatus(UUID terminalId, String status);
}
