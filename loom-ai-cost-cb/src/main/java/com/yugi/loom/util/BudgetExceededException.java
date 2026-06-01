package com.yugi.loom.util;

public class BudgetExceededException extends RuntimeException {
    public BudgetExceededException(String msg) {
        super(msg);
    }
}
