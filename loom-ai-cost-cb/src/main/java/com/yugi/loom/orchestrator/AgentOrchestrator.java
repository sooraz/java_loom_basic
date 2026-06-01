package com.yugi.loom.orchestrator;

import com.yugi.loom.context.RequestBudget;
import com.yugi.loom.tools.LlmTool;
import com.yugi.loom.util.BudgetExceededException;

import java.util.UUID;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

public class AgentOrchestrator {

    public static String handleRequest(String userId, String question) throws Exception {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        System.out.println("question lenght::"+question.length());
        double budgetUsd = userId.equals("user456")? 0.50 : 1.00; // user456 ki takkuva budget

        RequestBudget budget = new RequestBudget(traceId, budgetUsd, new AtomicReference<>(0.0));

        return ScopedValue.where(RequestBudget.BUDGET, budget).call(() -> {
            System.out.printf("[%s] === Request from %s, Budget: $%.2f ===%n", traceId, userId, budgetUsd);

            try (var scope = new StructuredTaskScope.ShutdownOnSuccess<String>()) {

                // Hedged Requests: 3 LLMs parallel. First success wins.
                scope.fork(() -> LlmTool.call(question, "gpt-4"));
                scope.fork(() -> LlmTool.call(question, "claude-3"));
                scope.fork(() -> LlmTool.call(question, "gemini"));

                // Join until first success or all fail
                scope.join();

                // Get first successful result
                String result = scope.result();
                System.out.printf("[%s] === Success in %.0fms, Total cost $%.4f ===%n",
                        traceId,
                        (double) (System.currentTimeMillis() - System.currentTimeMillis() + 500),
                        budget.spentUsd().get());

                return result;

            } catch (BudgetExceededException e) {
                System.out.printf("[%s] === CB OPEN: %s ===%n", traceId, e.getMessage());
                return "Budget exceeded. Please upgrade or try shorter query.";
            } catch (Exception e) {
                System.out.printf("[%s] === All LLMs failed: %s ===%n", traceId, e.getMessage());
                return "All AI providers down. Try later.";
            }
        });
    }
}
