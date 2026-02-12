package com.toshi.service;


import com.toshi.dto.PaymentRequestDto;
import com.toshi.dto.PaymentResponseDto;
import com.toshi.entity.Payment;
import reactor.core.publisher.Mono;

public interface PaymentService {
    Mono<PaymentResponseDto> createOrder(PaymentRequestDto dto);
    Mono<Payment> verifyPayment(String orderId, String paymentId, String signature, String secret);
    Mono<Payment> getPaymentStatus(Long id);
}
