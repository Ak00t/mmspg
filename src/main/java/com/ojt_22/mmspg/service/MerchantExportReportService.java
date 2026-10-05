package com.ojt_22.mmspg.service;

import java.time.LocalDate;
import org.springframework.core.io.ByteArrayResource;

public interface MerchantExportReportService {
    ByteArrayResource salesLedger(LocalDate from, LocalDate to);
    ByteArrayResource settlements(LocalDate from, LocalDate to);
}
