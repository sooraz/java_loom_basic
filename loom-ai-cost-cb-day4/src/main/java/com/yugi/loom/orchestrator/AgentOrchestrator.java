package com.yugi.loom.orchestrator;

import com.yugi.loom.context.UserContext;
import com.yugi.loom.context.RequestBudget;
import com.yugi.loom.tools.LlmTool;
import com.yugi.loom.ratelimit.RedisRateLimiter;
import redis.clients.jedis.JedisPool;

import java.util.UUID;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.atomic.AtomicReference;

public class AgentOrchestrator {
    private static final RedisRateLimiter rateLimiter = new RedisRateLimiter(
        new JedisPool("localhost", 6379),
        10, // 10 requests
        10.0 / 60 // 10 per minute = 0.166 per second
    );

    public static String handleRequest(String userId, String question) throws Exception {
        String traceId = UUID.randomUUID().toString().substring(0, 8);

        // ScopedValue lo user context set
        UserContext ctx = new UserContext(userId, traceId);

        return ScopedValue.where(UserContext.CURRENT, ctx).call(() -> {
            // Step 1: RateLimit check
            var rateResult = rateLimiter.tryConsume(userId);
            System.out.println("sooraz rateresult allowed ::"+rateResult.allowed());
            if (!rateResult.allowed()) {
                System.out.printf("[%s] RateLimit 429 for user %s. Tokens left: %d%n",
                    traceId, userId, rateResult.remainingTokens());
                return "429 Too Many Requests. Try after 1 minute.";
            }

            System.out.printf("[%s] Rate OK. Tokens left: %d%n", traceId, rateResult.remainingTokens());

            // Step 2: Budget context - Day 3 logic
            RequestBudget budget = new RequestBudget(traceId, 1.0, new AtomicReference<>(0.0));

            return ScopedValue.where(RequestBudget.BUDGET, budget).call(() -> {
                try (var scope = new StructuredTaskScope.ShutdownOnSuccess<String>()) {
                    scope.fork(() -> LlmTool.call(question, "gpt-4"));
                    scope.fork(() -> LlmTool.call(question, "claude-3"));
                    scope.join();
                    return scope.result();
                } catch (Exception e) {
                    return "Error: " + e.getMessage();
                }
            });
        });
    }
}
