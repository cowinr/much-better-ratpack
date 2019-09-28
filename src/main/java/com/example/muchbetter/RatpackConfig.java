package com.example.muchbetter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ratpack.exec.Blocking;
import ratpack.func.Action;
import ratpack.handling.Chain;
import ratpack.handling.Context;
import ratpack.http.Response;
import ratpack.jackson.Jackson;
import ratpack.registry.Registry;

import java.math.BigDecimal;
import java.util.Optional;

@Configuration
public class RatpackConfig {

    private UserRepository repository;

    private TokenGenerator tokenGenerator;

    public RatpackConfig(UserRepository repository, TokenGenerator tokenGenerator) {
        this.repository = repository;
        this.tokenGenerator = tokenGenerator;
    }

    /**
     * Netty on port 5050
     */
    @Bean
    public Action<Chain> api() {
        return chain -> chain

            // Creates a new user and gives them a preset balance in a preset currency
            .post("login", ctx -> {
                String token = tokenGenerator.generateToken();
                User user = new User(token, new BigDecimal("100"), "GBP");
                Blocking.get(() -> repository.save(user)).then(u -> {
                    ctx.header("Authorization", token);
                    Response response = ctx.getResponse();
                    response.status(201);
                    response.send();
                });
            })

            // Find user by auth token. This is like login plus user lookup.
            .all(ctx -> {
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
            })

            // Retrieve user's list of transactions
            .get("transactions", ctx -> {
                User user = ctx.get(User.class);
                ctx.render(Jackson.json(user.getTransactions()));
            })

            // Apply spend transaction to user
            .post("spend", ctx -> {
                User user = ctx.get(User.class);
                ctx.parse(Jackson.fromJson(Transaction.class)).then(transaction -> {
                    user.applyTransaction(transaction);
                    repository.save(user);
                });
                Response response = ctx.getResponse();
                response.status(201);
                response.send();
            })

            // Retrieve user's balance along with the currency code
            .get("balance", ctx -> {
                User user = ctx.get(User.class);
                ctx.render(Jackson.json(user.getBalanceAmount()));
            });
    }

    private void sendError(Context ctx, int status, String message) {
        Response response = ctx.getResponse();
        response.status(status);
        response.send(message);
    }
}
