package com.toshi.service;


import com.toshi.dto.PaymentRequestDto;
import com.toshi.dto.PaymentResponseDto;
import com.toshi.entity.Payment;
import reactor.core.publisher.Mono;

public interface PaymentService {
    Mono<PaymentResponseDto> createQrPayment(PaymentRequestDto dto);
    Mono<Payment> scanQr(Long paymentId);
    Mono<Payment> verifyPayment(Long paymentId, String transactionId);
}
