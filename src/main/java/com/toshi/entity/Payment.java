package com.toshi.entity;


import com.toshi.enums.PaymentMode;
import com.toshi.enums.PaymentStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;


@Table(name = "payments")
public class Payment {

    @Id
    private Long id;

    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private Double amount;
    private PaymentMode mode; // QR_CODE

    private PaymentStatus status; // PENDING, SUCCESS, FAILED

    private String upiId;

    private String transactionId;

}
