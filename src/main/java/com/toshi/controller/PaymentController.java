package com.toshi.controller;


import com.toshi.dto.ApiResponse;
import com.toshi.dto.PaymentRequestDto;
import com.toshi.dto.PaymentResponseDto;
import com.toshi.entity.Payment;
import com.toshi.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/qr")
    public Mono<ResponseEntity<ApiResponse<PaymentResponseDto>>> createQr(
            @RequestBody PaymentRequestDto dto) {

        return paymentService.createQrPayment(dto)
                .map(response ->
                        ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<PaymentResponseDto>builder()
                                        .status("SUCCESS")
                                        .message("QR payment created successfully")
                                        .statusCode(201)
                                        .data(response)
                                        .build()
                                ));
    }

    @GetMapping("/scan/{paymentId}")
    public ResponseEntity<ApiResponse<Payment>> scanQr(
            @PathVariable Long paymentId) {

       return paymentService.scanQr(paymentId);

        ApiResponse<Payment> apiResponse =
                new ApiResponse<>(
                        "SUCCESS",
                        "QR scanned successfully",
                        payment,
                        HttpStatus.OK.value()
                );

        return ResponseEntity.ok(apiResponse);
    }


    // ✅ NEW: VERIFY PAYMENT STATUS
    @PostMapping("/verify/{paymentId}/{transactionId}")
    public ResponseEntity<ApiResponse<Payment>> verifyPayment(
            @PathVariable Long paymentId, @PathVariable String transactionId) {

        Payment payment = paymentService.verifyPayment(paymentId, transactionId);

        ApiResponse<Payment> apiResponse =
                new ApiResponse<>(
                        "SUCCESS",
                        "Payment status fetched successfully",
                        payment,
                        HttpStatus.OK.value()
                );

        return ResponseEntity.ok(apiResponse);
    }
}
