package com.yugi.loom.context;

import java.util.concurrent.atomic.AtomicReference;

public record RequestBudget(String traceId, double budgetUsd, AtomicReference<Double> spentUsd) {
    public static final ScopedValue<RequestBudget> BUDGET = ScopedValue.newInstance();

    public static RequestBudget current() {
        if (!BUDGET.isBound()) {
            throw new IllegalStateException("RequestBudget not bound to current scope");
        }
        return BUDGET.get();
    }

    public boolean trySpend(double cost) {
        while (true) {
            Double currentRef = spentUsd.get();
            double current = currentRef != null ? currentRef : 0.0;
            double newSpent = current + cost;
            if (newSpent > budgetUsd) {
                return false; // Budget cross
            }
            if (spentUsd.compareAndSet(currentRef, newSpent)) {
                return true;
            }
        }
    }
}
