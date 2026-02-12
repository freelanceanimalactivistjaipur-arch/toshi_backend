package com.toshi.dto;


import lombok.*;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PaymentResponseDto {
    private Long paymentId;
    private String razorpayOrderId;
    private String status;

    public PaymentResponseDto(Long paymentId, String razorpayOrderId, String status) {
        this.paymentId = paymentId;
        this.razorpayOrderId = razorpayOrderId;
        this.status = status;
    }

    // getters
    public Long getPaymentId() { return paymentId; }
    public String getRazorpayOrderId() { return razorpayOrderId; }
    public String getStatus() { return status; }


}