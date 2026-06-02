package com.yugi.loom;

import com.yugi.loom.orchestrator.AgentOrchestrator;

public class LoomAiApplication {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Day 4: Redis RateLimiter Test ===\n");

        String userId = "user123";

        // 12 requests rapid fire - 10 matrame allow avvali
        for (int i = 1; i <= 12; i++) {
            String result = AgentOrchestrator.handleRequest(userId, "Query " + i);
            System.out.println("Req " + i + ": " + result.substring(0, Math.min(40, result.length())));
            Thread.sleep(100); // 100ms gap
        }
    }
}
