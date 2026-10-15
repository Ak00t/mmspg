package com.ojt_22.mmspg.dto;

import java.time.Instant;

public record ErrorResponse(Instant timestamp, int status, String error, String message) {
}
