package com.toshi.serviceImpl;


import com.toshi.dto.PaymentRequestDto;
import com.toshi.dto.PaymentResponseDto;
import com.toshi.entity.Payment;
import com.toshi.enums.PaymentMode;
import com.toshi.enums.PaymentStatus;
import com.toshi.repository.PaymentRepository;
import com.toshi.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final String OWNER_UPI = "7060532399@ikwik";
    private static final String OWNER_NAME = "Toshi";
    @Autowired
    private PaymentRepository paymentRepository;
    @Override
    public PaymentResponseDto createQrPayment(PaymentRequestDto dto) {
        Payment payment = new Payment();
        payment.setFirstname(dto.getFirstname());
        payment.setLastname(dto.getLastname());
        payment.setEmail(dto.getEmail());
        payment.setPhone(dto.getPhone());
        payment.setAmount(dto.getAmount());
        payment.setMode(PaymentMode.QR_CODE);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setUpiId(OWNER_UPI);
        payment = paymentRepository.save(payment);
        // 🔥 REAL UPI QR STRING
        String upiQrUrl =
                "upi://pay?pa=" + OWNER_UPI +
                        "&pn=" + URLEncoder.encode(OWNER_NAME, StandardCharsets.UTF_8) +
                        "&am=" + payment.getAmount() +
                        "&cu=INR";
        PaymentResponseDto response = new PaymentResponseDto();
        response.setPaymentId(payment.getId());
        response.setStatus(payment.getStatus().name());
        response.setQrUrl(upiQrUrl);

        return response;

    }

    @Override
    public Payment scanQr(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus(PaymentStatus.SUCCESS); // or FAILED based on logic
        return paymentRepository.save(payment);
    }

    @Override
    public Payment verifyPayment(Long paymentId, String transactionId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus(PaymentStatus.SUCCESS); // or FAILED based on logic
        payment.setTransactionId(transactionId);
        return paymentRepository.save(payment);
    }
}
