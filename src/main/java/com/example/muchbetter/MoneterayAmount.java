package com.example.muchbetter;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Wraps an amount and currency code.
 * There is probably a better implementation in https://javamoney.github.io/ri.html such as
 * https://github.com/JavaMoney/jsr354-ri/blob/master/moneta-core/src/main/java/org/javamoney/moneta/Money.java
 */
@Getter
@AllArgsConstructor
public class MoneterayAmount {
    private BigDecimal amount;
    private String currency;
    public static final MoneterayAmount ZERO = new MoneterayAmount(BigDecimal.ZERO, "---");
}
