package com.bancobase.payments.web;

import com.bancobase.payments.payment.exception.PaymentNotFoundException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class RestExceptionHandler {

    private static final String ERRORS_PROPERTY = "errors";

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ProblemDetail> notFound(PaymentNotFoundException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Resource not found");
        pd.setInstance(URI.create(request.getRequestURI()));
        pd.setProperty(ERRORS_PROPERTY, List.<FieldErrorDetail>of());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<FieldErrorDetail> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldErrorDetail)
                .collect(Collectors.toList());
        return badRequest("Validation failed", "One or more fields are invalid.", request, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        List<FieldErrorDetail> errors = errorsFromHttpMessageNotReadable(ex);
        return badRequest("Invalid request body", "The request body could not be parsed or has invalid values.", request, errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        List<FieldErrorDetail> errors = ex.getConstraintViolations().stream()
                .map(this::toFieldErrorDetail)
                .collect(Collectors.toList());
        return badRequest("Validation failed", "Constraint validation failed.", request, errors);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleMissingParameter(
            MissingServletRequestParameterException ex,
            HttpServletRequest request
    ) {
        List<FieldErrorDetail> errors = List.of(new FieldErrorDetail(
                ex.getParameterName(),
                "Required request parameter is missing",
                null
        ));
        return badRequest("Missing parameter", ex.getMessage(), request, errors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        String name = ex.getName();
        String required = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        List<FieldErrorDetail> errors = List.of(new FieldErrorDetail(
                name,
                "Invalid value; expected type " + required,
                ex.getValue()
        ));
        return badRequest("Type mismatch", "Invalid value for parameter '" + name + "'.", request, errors);
    }

    private ResponseEntity<ProblemDetail> badRequest(
            String title,
            String detail,
            HttpServletRequest request,
            List<FieldErrorDetail> errors
    ) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        pd.setTitle(title);
        pd.setInstance(URI.create(request.getRequestURI()));
        pd.setProperty(ERRORS_PROPERTY, errors);
        return ResponseEntity.badRequest().body(pd);
    }

    private FieldErrorDetail toFieldErrorDetail(FieldError e) {
        return new FieldErrorDetail(e.getField(), e.getDefaultMessage() != null ? e.getDefaultMessage() : "Invalid", e.getRejectedValue());
    }

    private FieldErrorDetail toFieldErrorDetail(ConstraintViolation<?> v) {
        String path = v.getPropertyPath() != null ? v.getPropertyPath().toString() : "unknown";
        String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
        return new FieldErrorDetail(field, v.getMessage(), v.getInvalidValue());
    }

    private List<FieldErrorDetail> errorsFromHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getMostSpecificCause();
        if (cause instanceof InvalidFormatException ife) {
            String field = ife.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .filter(n -> n != null)
                    .collect(Collectors.joining("."));
            if (field.isEmpty()) {
                field = "body";
            }
            String message = buildInvalidFormatMessage(ife);
            return List.of(new FieldErrorDetail(field, message, ife.getValue()));
        }
        if (cause instanceof JsonParseException jpe) {
            return List.of(new FieldErrorDetail("body", "Malformed JSON: " + jpe.getOriginalMessage(), null));
        }
        return List.of(new FieldErrorDetail(
                "body",
                "Request body could not be read",
                null
        ));
    }

    private static String buildInvalidFormatMessage(InvalidFormatException ife) {
        Class<?> target = ife.getTargetType();
        if (target != null && target.isEnum()) {
            String allowed = Arrays.stream(target.getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            return "Invalid value. Allowed values: " + allowed;
        }
        return ife.getOriginalMessage() != null ? ife.getOriginalMessage() : "Invalid format";
    }
}
