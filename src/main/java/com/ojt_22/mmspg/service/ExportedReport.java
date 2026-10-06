package com.ojt_22.mmspg.service;

import org.springframework.core.io.ByteArrayResource;

public record ExportedReport(ByteArrayResource resource, String filename, String contentType) {}
