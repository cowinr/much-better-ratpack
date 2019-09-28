package com.example.muchbetter;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.PositiveOrZero;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
class Transaction implements Serializable {

    @NonNull
    @PastOrPresent
    private LocalDateTime date;

    @NonNull
    private String description;

    @NonNull
    @PositiveOrZero
    private BigDecimal amount;

    @NonNull
    private String currency;

}