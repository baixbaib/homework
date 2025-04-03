package com.hsbc.homework.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDTO {
    private Long id;

    @NotBlank(message = "dto Account owner is required")
    private String owner;

    @NotNull(message = "dto Balance is required")
    private Double balance;
}