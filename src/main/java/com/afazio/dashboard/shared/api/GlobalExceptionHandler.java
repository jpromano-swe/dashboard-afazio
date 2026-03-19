package com.afazio.dashboard.shared.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiErrorResponse handleIllegalArgument(
    IllegalArgumentException ex,
    HttpServletRequest request
  ) {
    return new ApiErrorResponse(
      OffsetDateTime.now(),
      HttpStatus.BAD_REQUEST.value(),
      HttpStatus.BAD_REQUEST.getReasonPhrase(),
      ex.getMessage(),
      request.getRequestURI()
    );
  }
}
