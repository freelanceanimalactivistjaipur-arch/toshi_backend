package com.toshi.controller;

import com.razorpay.RazorpayException;
import com.toshi.dto.ApiResponse;
import com.toshi.dto.PaymentRequestDto;
import com.toshi.dto.PaymentResponseDto;
import com.toshi.entity.Payment;
import com.toshi.service.PaymentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/api/payment")
@CrossOrigin
@Validated
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @Value("${razorpay.secret}")
    private String razorpaySecret;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Create Razorpay Order
     */
    @PostMapping("/create-order")
    public Mono<ResponseEntity<ApiResponse<PaymentResponseDto>>> createOrder(
            @Valid @RequestBody PaymentRequestDto dto) {

       // log.info("Received create-order request: {}", dto);

        return paymentService.createOrder(dto)
                .map(resp -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(new ApiResponse<>(
                                "SUCCESS",
                                "Order created successfully",
                                HttpStatus.CREATED.value(),
                                resp
                        )))
                .onErrorResume(RazorpayException.class, ex -> {


                     //  log.error("RazorpayException: {}", ex.getMessage(), ex);
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ApiResponse<>(
                                    "ERROR",
                                    ex.getMessage(),
                                    HttpStatus.BAD_REQUEST.value(),
                                    null
                            )));
                })
                .onErrorResume(Exception.class, ex -> {
                  //  log.error("Unhandled Exception: {}", ex.getMessage(), ex);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new ApiResponse<>(
                                    "ERROR",
                                    "Internal server error",
                                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                    null
                            )));
                });
    }

    /**
     * Verify Payment
     */
    @PostMapping("/verify")
    public Mono<ResponseEntity<ApiResponse<Payment>>> verifyPayment(
            @RequestParam String orderId,
            @RequestParam String paymentId,
            @RequestParam String signature) {

       // log.info("Verifying payment: orderId={}, paymentId={}", orderId, paymentId);

        return paymentService.verifyPayment(orderId, paymentId, signature, razorpaySecret)
                .map(payment -> ResponseEntity.ok(
                        new ApiResponse<>(
                                "SUCCESS",
                                "Payment verified successfully",
                                HttpStatus.OK.value(),
                                payment
                        )
                ))
                .onErrorResume(RazorpayException.class, ex -> {
                  //  log.error("RazorpayException: {}", ex.getMessage(), ex);
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ApiResponse<>(
                                    "ERROR",
                                    ex.getMessage(),
                                    HttpStatus.BAD_REQUEST.value(),
                                    null
                            )));
                })
                .onErrorResume(Exception.class, ex -> {
                   // log.error("Unhandled Exception: {}", ex.getMessage(), ex);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new ApiResponse<>(
                                    "ERROR",
                                    "Internal server error",
                                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                    null
                            )));
                });
    }

    /**
     * Get Payment Status
     */
    @GetMapping("/status/{id}")
    public Mono<ResponseEntity<ApiResponse<Payment>>> getPaymentStatus(@PathVariable Long id) {
      //  log.info("Fetching payment status for id={}", id);

        return paymentService.getPaymentStatus(id)
                .map(payment -> ResponseEntity.ok(
                        new ApiResponse<>(
                                "SUCCESS",
                                "Payment status fetched successfully",
                                HttpStatus.OK.value(),
                                payment
                        )
                ))
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(
                                "ERROR",
                                "Payment not found",
                                HttpStatus.NOT_FOUND.value(),
                                null
                        ))))
                .onErrorResume(Exception.class, ex -> {
                  //  log.error("Unhandled Exception: {}", ex.getMessage(), ex);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new ApiResponse<>(
                                    "ERROR",
                                    "Internal server error",
                                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                    null
                            )));
                });
    }
}
