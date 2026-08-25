package com.shrinetours.api.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.shrinetours.api.dto.payment.InvoiceMetaResponse;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;
    private final Object errors;
    private final LocalDateTime timestamp;
    private final String path;
	public static Object success(InvoiceMetaResponse invoiceMeta) {
		// TODO Auto-generated method stub
		return null;
	}
}
