package com.bancobase.payments.web;


public record FieldErrorDetail(String field, String message, Object rejectedValue) {
}
