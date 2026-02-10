package com.toshi.dto;


import lombok.*;

public class PaymentResponseDto {
    private Long paymentId;
    private String status;
    private String qrUrl;   // QR code link
}