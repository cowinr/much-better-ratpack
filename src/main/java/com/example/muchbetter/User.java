package com.example.muchbetter;

import lombok.Data;
import lombok.NonNull;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user, an owner of a balance and a set of transactions. Created each time on login.
 *
 * @see com.example.muchbetter.RatpackConfig.LoginHandler
 */
@Data
@RedisHash("User")
public class User implements Serializable {

    @NonNull
    private String id;

    @NonNull
    private BigDecimal balance;

    @NonNull
    private String currency;

    private List<Transaction> transactions;

    public List<Transaction> getTransactions() {
        if (transactions == null)
            transactions = new ArrayList<>();
        return transactions;
    }

    /**
     * Apply a transaction and update the user's balance.
     *
     * @param transaction The transaction to apply.
     * @throws InsifficientFundsException If the transaction is greater than the remaining balance.
     */
    public void applyTransaction(Transaction transaction) throws InsifficientFundsException {
        Assert.isTrue(transaction.getCurrency().equals(getCurrency()), "Transaction currency must match user's currency");

        BigDecimal newBalance = balance.subtract(transaction.getAmount());
        if (newBalance.compareTo(BigDecimal.ZERO) < 0)
            throw new InsifficientFundsException("New balance will be less than 0");

        this.balance = newBalance;
        this.getTransactions().add(transaction);
    }

    public MoneterayAmount getBalanceAmount() {
        return new MoneterayAmount(getBalance(), getCurrency());
    }
}
