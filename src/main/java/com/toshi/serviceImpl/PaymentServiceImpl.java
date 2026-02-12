
package com.toshi.serviceImpl;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.toshi.dto.PaymentRequestDto;
import com.toshi.dto.PaymentResponseDto;
import com.toshi.entity.Payment;
import com.toshi.enums.PaymentStatus;
import com.toshi.repository.PaymentRepository;
import com.toshi.service.PaymentService;
import org.apache.commons.codec.digest.HmacUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    private final RazorpayClient razorpayClient;

    @Value("${razorpay.secret}")
    private String razorpaySecret;

    public PaymentServiceImpl(RazorpayClient razorpayClient, PaymentRepository paymentRepository) {
        this.razorpayClient = razorpayClient;
        this.paymentRepository = paymentRepository;
    }


    @Override
    public Mono<PaymentResponseDto> createOrder(PaymentRequestDto dto) {
        JSONObject options = new JSONObject();
        options.put("amount", dto.getAmount() * 100); // paise
        options.put("currency", "INR");
        options.put("receipt", "txn_" + System.currentTimeMillis());

        // Wrap blocking call in Mono
        return Mono.fromCallable(() -> razorpayClient.orders.create(options))
                .flatMap(order -> {
                    Payment payment = new Payment();
                    payment.setFirstname(dto.getFirstname());
                    payment.setLastname(dto.getLastname());
                    payment.setEmail(dto.getEmail());
                    payment.setPhone(dto.getPhone());
                    payment.setAmount(dto.getAmount());
                    payment.setRazorpayOrderId(order.get("id"));
                    payment.setStatus(PaymentStatus.PENDING);

                    return paymentRepository.save(payment)
                            .map(saved -> new PaymentResponseDto(
                                    saved.getId(),
                                    saved.getRazorpayOrderId(),
                                    saved.getStatus().name()
                            ));
                });
    }
    // Verify Payment
    public Mono<Payment> verifyPayment(String orderId, String paymentId, String signature, String secret) {
        return paymentRepository.findByRazorpayOrderId(orderId)
                .switchIfEmpty(Mono.error(new RuntimeException("Payment not found")))
                .flatMap(payment -> {
                    String payload = orderId + "|" + paymentId;
                    String expectedSignature = HmacUtils.hmacSha256Hex(secret, payload);
                    if (expectedSignature.equals(signature)) {
                        payment.setStatus(PaymentStatus.SUCCESS);
                        payment.setRazorpayPaymentId(paymentId);
                        payment.setRazorpaySignature(signature);
                    } else {
                        payment.setStatus(PaymentStatus.FAILED);
                    }
                    return paymentRepository.save(payment);
                });
    }

    // Get Payment Status
    public Mono<Payment> getPaymentStatus(Long id) {
        return paymentRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Payment not found")));
    }



}