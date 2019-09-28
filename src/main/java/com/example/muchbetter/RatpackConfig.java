package com.example.muchbetter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ratpack.exec.Blocking;
import ratpack.func.Action;
import ratpack.handling.Chain;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.http.Response;
import ratpack.jackson.Jackson;
import ratpack.registry.Registry;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Configuration of Ratpack handler chain
 */
@Configuration
public class RatpackConfig {

    private UserRepository repository;

    private TokenGenerator tokenGenerator;

    public RatpackConfig(UserRepository repository, TokenGenerator tokenGenerator) {
        this.repository = repository;
        this.tokenGenerator = tokenGenerator;
    }

    /**
     * Runs on Netty port 5050
     */
    @Bean
    public Action<Chain> api() {
        return chain -> chain

            // Creates a new user and gives them a preset balance in a preset currency
            .post("login", new LoginHandler())

            // Find user by auth token. This is like login plus user lookup.
            .all(this::handleAuthAndUserLookup)

            // Apply spend transaction to user
            .post("spend", new SpendHandler())

            // Retrieve user's list of transactions
            .get("transactions", ctx -> ctx
                .render(Jackson.json(ctx.get(User.class).getTransactions())))

            // Retrieve user's balance along with the currency code
            .get("balance", ctx -> ctx
                .render(Jackson.json(ctx.get(User.class).getBalanceAmount()))
            );
    }

    /**
     * Find user by auth token. This is like login plus user lookup.
     * The User object is stored in the registry for downstream lookup.
     *
     * @param ctx Ratpack context.
     */
    private void handleAuthAndUserLookup(Context ctx) {
        Optional<String> authorization = ctx.header("Authorization");
        if (authorization.isPresent()) {
            Blocking.get(() -> repository.findById(authorization.get().substring(7)))
                .then(user -> {
                    if (user.isPresent()) {
                        // Store User object for later handling
                        ctx.next(Registry.single(User.class, user.get()));
                    }
                    else sendError(ctx, 403, "Forbidden");
                });

        }
        else sendError(ctx, 400, "Missing Authorization token");
    }

    /**
     * Send an error response.
     *
     * @param ctx     Ratpack context.
     * @param status  HTTP Status code to send.
     * @param message Message written as plain text to response.
     */
    private void sendError(Context ctx, int status, String message) {
        Response response = ctx.getResponse();
        response.status(status);
        response.send(message);
    }

    /**
     * Handles user login by creating a new User in Redis.
     * User's ID is a generated token which is returned as a response header.
     * Subsequent requests use this token for authentication and user identification.
     */
    private class LoginHandler implements Handler {

        @Override
        public void handle(Context ctx) {
            String token = tokenGenerator.generateToken();
            User user = new User(token, new BigDecimal("100"), "GBP");
            Blocking
                .get(() -> repository.save(user))
                .then(u -> {
                    ctx.header("Authorization", token);
                    Response response = ctx.getResponse();
                    response.status(201);
                    response.send();
                });
        }
    }

    /**
     * Handles requests to spend money, i.e. apply a transaction that reduces User's balance.
     */
    private class SpendHandler implements Handler {

        @Override
        public void handle(Context ctx) {
            User user = ctx.get(User.class);
            ctx.parse(Jackson.fromJson(Transaction.class))
                .then(transaction -> {
                    user.applyTransaction(transaction);
                    repository.save(user);
                });
            Response response = ctx.getResponse();
            response.status(201);
            response.send();
        }
    }
}
