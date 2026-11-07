package com.ojt_22.mmspg.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.ojt_22.mmspg.entity.Merchant;
import com.ojt_22.mmspg.enums.TerminalType;
import com.ojt_22.mmspg.repository.BranchRepository;
import com.ojt_22.mmspg.repository.MerchantRepository;
import com.ojt_22.mmspg.repository.TerminalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CodeGeneratorService {

	private final MerchantRepository merchantRepository;
	private final BranchRepository branchRepository;
	private final TerminalRepository terminalRepository;

	/**
	 * Generates serialized Merchant Code: MC-000001, MC-000002...
	 */
	public synchronized String generateMerchantCode() {
		String maxCode = merchantRepository.findMaxMerchantCode().orElse(null);
		long nextNum = 1;
		if (maxCode != null && maxCode.startsWith("MC-")) {
			try {
				nextNum = Long.parseLong(maxCode.substring(3)) + 1;
			} catch (NumberFormatException e) {
				log.warn("Could not parse numeric part of merchant code: {}", maxCode);
			}
		}

		String code;
		do {
			code = String.format("MC-%06d", nextNum++);
		} while (merchantRepository.existsByMerchantCode(code));

		log.info("Auto-generated Merchant Code: {}", code);
		return code;
	}

	/**
	 * Generates hierarchical Branch Code: BR-<merchantCode>-01, BR-<merchantCode>-02...
	 */
	public synchronized String generateBranchCode(Merchant merchant) {
		String merchantCode = merchant.getMerchantCode();
		String prefix = "BR-" + merchantCode + "-";
		List<String> existingCodes = branchRepository.findBranchCodesByMerchantAndPrefix(merchant.getId(), prefix + "%");

		int maxSeq = 0;
		for (String c : existingCodes) {
			if (c != null && c.startsWith(prefix)) {
				try {
					int seq = Integer.parseInt(c.substring(prefix.length()));
					if (seq > maxSeq) {
						maxSeq = seq;
					}
				} catch (NumberFormatException ignored) {}
			}
		}

		int nextSeq = maxSeq + 1;
		String branchCode;
		do {
			branchCode = String.format("%s%02d", prefix, nextSeq++);
		} while (branchRepository.existsByBranchCodeAndMerchantId(branchCode, merchant.getId()));

		log.info("Auto-generated Branch Code: {} for merchant: {}", branchCode, merchantCode);
		return branchCode;
	}

	/**
	 * Generates serialized Terminal Code: POS-000001, API-000001...
	 */
	public synchronized String generateTerminalCode(TerminalType type) {
		String prefix = (type == TerminalType.PHYSICAL_POS) ? "POS-" : "API-";
		String maxCode = terminalRepository.findMaxTerminalCodeByPrefix(prefix + "%").orElse(null);

		long nextNum = 1;
		if (maxCode != null && maxCode.startsWith(prefix)) {
			try {
				nextNum = Long.parseLong(maxCode.substring(prefix.length())) + 1;
			} catch (NumberFormatException e) {
				log.warn("Could not parse numeric part of terminal code: {}", maxCode);
			}
		}

		String code;
		do {
			code = String.format("%s%06d", prefix, nextNum++);
		} while (terminalRepository.existsByTerminalCode(code));

		log.info("Auto-generated Terminal Code: {} for type: {}", code, type);
		return code;
	}
}
