package com.ciav.staceymeals.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;

@RestControllerAdvice
@Slf4j
class RecipeControllerAdvice {

    private static final String MDN_DOC_BASE_URL = "https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/";

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatusException(ResponseStatusException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(ex.getStatusCode());
        problemDetail.setDetail(ex.getReason());
        problemDetail.setTitle(getReasonPhrase(ex.getStatusCode().value()));
        problemDetail.setType(URI.create(MDN_DOC_BASE_URL + ex.getStatusCode().value()));

        return problemDetail;
    }

    // Kinda insane that Spring Web doesn't have a better way to get the reason phrase
    private String getReasonPhrase(int statusCode) {
        HttpStatus status = HttpStatus.resolve(statusCode);
        return status != null ? status.getReasonPhrase() : "Unknown Status";
    }
}