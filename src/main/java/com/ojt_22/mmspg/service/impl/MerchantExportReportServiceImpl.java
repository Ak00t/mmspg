package com.ojt_22.mmspg.service.impl;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.ojt_22.mmspg.repository.MerchantRepository;
import com.ojt_22.mmspg.repository.reporting.MerchantExportReportRepository;
import com.ojt_22.mmspg.service.MerchantExportReportService;
import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor
public class MerchantExportReportServiceImpl implements MerchantExportReportService {
    private final MerchantRepository merchants;
    private final MerchantExportReportRepository reports;

    private java.util.UUID merchantId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) throw new AccessDeniedException("Authentication is required");
        return merchants.findByEmail(auth.getName()).orElseThrow(() -> new AccessDeniedException("Merchant account was not found")).getId();
    }
    private void validate(LocalDate from, LocalDate to) { if (from == null || to == null || from.isAfter(to)) throw new IllegalArgumentException("Invalid report date range"); }
    private String csv(String... values) { return java.util.Arrays.stream(values).map(v -> { String s = v == null ? "" : v; return "\"" + s.replace("\"", "\"\"") + "\""; }).collect(java.util.stream.Collectors.joining(",")); }

    public ByteArrayResource salesLedger(LocalDate from, LocalDate to) {
        validate(from, to); var out = new StringBuilder(csv("Ledger ID","Transaction Reference","Order ID","Entry Type","Balance Type","Amount","Description","Transaction Status","Settlement Reference","Created At","Settlement Date")).append('\n');
        reports.salesLedger(merchantId(), from, to).forEach(r -> out.append(csv(r.getLedgerId(),r.getTransactionReference(),r.getOrderId(),r.getEntryType(),r.getBalanceType(),String.valueOf(r.getAmount()),r.getDescription(),r.getTransactionStatus(),r.getSettlementReference(),String.valueOf(r.getCreatedAt()),String.valueOf(r.getSettlementDate()))).append('\n'));
        return new ByteArrayResource(out.toString().getBytes(StandardCharsets.UTF_8));
    }
    public ByteArrayResource settlements(LocalDate from, LocalDate to) {
        validate(from, to); var out = new StringBuilder(csv("Settlement ID","Settlement Reference","Transaction Reference","Settlement Date","Gross Amount","Fee Amount","Net Amount","Bank Account","Status","Processed At","Created At")).append('\n');
        reports.settlements(merchantId(), from, to).forEach(r -> out.append(csv(r.getSettlementId(),r.getSettlementReference(),r.getTransactionReference(),String.valueOf(r.getSettlementDate()),String.valueOf(r.getGrossAmount()),String.valueOf(r.getFeeAmount()),String.valueOf(r.getNetAmount()),r.getBankAccountNo(),r.getStatus(),String.valueOf(r.getProcessedAt()),String.valueOf(r.getCreatedAt()))).append('\n'));
        return new ByteArrayResource(out.toString().getBytes(StandardCharsets.UTF_8));
    }
}
