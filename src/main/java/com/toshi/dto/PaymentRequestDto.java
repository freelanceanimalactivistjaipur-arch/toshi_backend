package com.toshi.dto;


import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDto {
    @NotBlank private String firstname;
    @NotBlank private String lastname;
    @Email private String email;
    @NotBlank private String phone;
    @NotNull @Positive private Double amount;
}