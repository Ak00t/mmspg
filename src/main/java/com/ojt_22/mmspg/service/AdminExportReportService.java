package com.ojt_22.mmspg.service;

import java.time.LocalDate;
import java.util.UUID;

public interface AdminExportReportService {
    ExportedReport salesLedger(UUID merchantId, LocalDate from, LocalDate to, String format, String status);
    ExportedReport settlements(UUID merchantId, LocalDate from, LocalDate to, String format, String status);
}
