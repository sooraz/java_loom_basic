package com.yugi.loom.tools;

import com.yugi.loom.context.RequestBudget;
import com.yugi.loom.util.TokenCounter;
import com.yugi.loom.util.BudgetExceededException;

public class LlmTool {

    public static String call(String prompt, String model) throws Exception {
        var budget = RequestBudget.current();

        double estimatedCost = TokenCounter.estimateCostUsd(prompt, model);

        System.out.printf("[%s] %s: Est cost $%.4f, Budget left $%.4f%n",
                budget.traceId(), model, estimatedCost, budget.budgetUsd() - budget.spentUsd().get());

        // Budget check before call
        if (!budget.trySpend(estimatedCost)) {
            throw new BudgetExceededException(
                    String.format("Budget $%.2f exceeded. Spent: $%.4f",
                            budget.budgetUsd(), budget.spentUsd().get())
            );
        }

        // Simulate LLM call - blocking but VT lo ok
        Thread.sleep(500);

        if (prompt.contains("quantum")) {
            Thread.sleep(200); // Long query = more cost
        }

        String response = model + " response for: " + prompt.substring(0, Math.min(30, prompt.length()));

        double actualCost = TokenCounter.estimateCostUsd(response, model);
        double diff = actualCost - estimatedCost;
        if (Math.abs(diff) > 0.0001) {
            budget.spentUsd().updateAndGet(v -> v + diff);
        }

        System.out.printf("[%s] %s: Actual cost $%.4f, Total spent $%.4f%n",
                budget.traceId(), model, actualCost, budget.spentUsd().get());

        return response;
    }
}
