package com.toshi.dto;


import lombok.*;
@Data
@Builder
@NoArgsConstructor
@Getter
@Setter
public class PaymentResponseDto {
    private Long paymentId;
    private String razorpayOrderId;
    private String status;

    private Double amount;       // in paise
    private String currency;

    public PaymentResponseDto(Long paymentId, String razorpayOrderId, String currency, Double amount, String status ) {
        this.paymentId = paymentId;
        this.razorpayOrderId = razorpayOrderId;
        this.currency = currency;
        this.amount = amount;
        this.status = status;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}