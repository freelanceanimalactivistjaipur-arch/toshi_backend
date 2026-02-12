package com.toshi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

public class PaymentRequestDto {
    @NotBlank private String firstname;
    @NotBlank private String lastname;
    @Email private String email;
    @NotBlank private String phone;
    @NotNull @Positive private Double amount;

    // getters and setters
    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }

    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
}