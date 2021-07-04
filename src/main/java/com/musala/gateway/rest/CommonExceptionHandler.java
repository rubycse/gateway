package com.musala.gateway.rest;

import com.musala.gateway.exceptions.ExceptionResponse;
import com.musala.gateway.exceptions.NotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;
import java.util.stream.Collectors;

/**
 * @author lutfun
 * @since 6/23/21
 */
@ControllerAdvice
@RestController
public class CommonExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ExceptionResponse> handleAllException(Exception ex, WebRequest request) {
        return new ResponseEntity<>(getExceptionResponse(ex, request), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NotFoundException.class)
    public final ResponseEntity<ExceptionResponse> handleNotFoundException(NotFoundException ex, WebRequest request) {
        return new ResponseEntity<>(getExceptionResponse(ex, request), HttpStatus.NOT_FOUND);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ObjectError globalError = ex.getBindingResult().getGlobalError();
        String errorMessages = (globalError != null ? globalError.getDefaultMessage() + ", " : "")
                + ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), errorMessages, request.getDescription(false));

        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    private ExceptionResponse getExceptionResponse(Exception ex, WebRequest request) {
        return new ExceptionResponse(new Date(), ex.getMessage(), request.getDescription(false));
    }
}
