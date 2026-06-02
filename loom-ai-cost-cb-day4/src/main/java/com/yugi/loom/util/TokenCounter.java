package com.yugi.loom.util;

public class TokenCounter {
    public static double estimateCostUsd(String text, String model) {
        if (text == null || text.isEmpty()) return 0.0;

        int tokens = Math.max(1, text.length() / 4); // Rough estimate

        double pricePer1k = switch (model) {
            case "gpt-4" -> 0.03;
            case "claude-3" -> 0.00025;
            case "gemini" -> 0.01;
            default -> 0.002;
        };

        return (tokens) * pricePer1k;
    }
}
