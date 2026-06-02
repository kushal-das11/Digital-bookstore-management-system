package com.cts.catalogservice.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ErrorResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final Instant timestamp;

    private final int status;
    private final String error;
    private final String message;
    private final String path;
}
