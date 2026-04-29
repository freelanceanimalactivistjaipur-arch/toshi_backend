package com.toshi.controller;

import com.razorpay.RazorpayException;
import com.toshi.dto.ApiResponse;
import com.toshi.dto.PaymentRequestDto;
import com.toshi.dto.PaymentResponseDto;
import com.toshi.entity.Payment;
import com.toshi.service.PaymentService;
import jakarta.validation.Valid;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;


@RestController
@RequestMapping("/api/payment")
@CrossOrigin
@Validated
public class PaymentController {

    private final PaymentService paymentService;

    @Value("${razorpay.secret}")
    private String razorpaySecret;


    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(PaymentController.class);

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Create Razorpay Order
     */
    @PostMapping("/create-order")
    public Mono<ResponseEntity<ApiResponse<PaymentResponseDto>>> createOrder(
            @Valid @RequestBody PaymentRequestDto dto) {

      log.info("Received create-order request: {}", dto);

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
        log.info("Fetching payment status for id={}", id);

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


    @PostMapping("/webhook")
    public Mono<ResponseEntity<String>> handleWebhook(
            @RequestHeader("X-Razorpay-Signature") String signature,
            @RequestBody String payload) {

        log.info("Received Razorpay webhook");

        return Mono.fromCallable(() -> {

            // 1️⃣ Verify signature
            if (!verifyWebhookSignature(payload, signature, webhookSecret)) {
                log.error("Invalid webhook signature");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
            }

            // 2️⃣ Parse payload
            org.json.JSONObject json = new org.json.JSONObject(payload);
            String event = json.getString("event");

            log.info("Webhook event: {}", event);

            switch (event) {

                case "payment.captured":
                    handlePaymentCaptured(json);
                    break;

                case "payment.failed":
                    handlePaymentFailed(json);
                    break;

                case "order.paid":
                    handleOrderPaid(json);
                    break;

                default:
                    log.info("Unhandled event: {}", event);
            }

            return ResponseEntity.ok("Webhook processed");

        }).onErrorResume(ex -> {
            log.error("Webhook error: {}", ex.getMessage(), ex);
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing webhook"));
        });
    }


    /**
     * 🔐 Signature Verification
     */
    private boolean verifyWebhookSignature(String payload, String actualSignature, String secret) throws Exception {

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));

        byte[] hash = mac.doFinal(payload.getBytes());
        String generatedSignature = Base64.getEncoder().encodeToString(hash);

        return generatedSignature.equals(actualSignature);
    }

    /**
     * ✅ Payment Success
     */
    private void handlePaymentCaptured(JSONObject json) {

        var payment = json.getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity");

        String paymentId = payment.getString("id");
        String orderId = payment.getString("order_id");

        log.info("Payment captured: orderId={}, paymentId={}", orderId, paymentId);

        paymentService.updateStatus(orderId, paymentId, "SUCCESS");
    }

    /**
     * ❌ Payment Failed
     */
    private void handlePaymentFailed(JSONObject json) {

        var payment = json.getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity");

        String paymentId = payment.getString("id");
        String orderId = payment.optString("order_id");

        log.info("Payment failed: orderId={}, paymentId={}", orderId, paymentId);

        paymentService.updateStatus(orderId, paymentId, "FAILED");
    }

    /**
     * 💰 Order Paid
     */
    private void handleOrderPaid(JSONObject json) {

        var order = json.getJSONObject("payload")
                .getJSONObject("order")
                .getJSONObject("entity");

        String orderId = order.getString("id");

        log.info("Order paid: orderId={}", orderId);

        paymentService.markOrderPaid(orderId);
    }
}
