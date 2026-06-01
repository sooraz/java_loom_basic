package com.yugi.loom;

import com.yugi.loom.orchestrator.AgentOrchestrator;

public class LoomAiApplication {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Loom AI Cost CircuitBreaker Demo ===\n");

        // Test 1: Normal request - budget lo untundi
        String ans1 = AgentOrchestrator.handleRequest("user123", "What to do today in Hyderabad?");
        System.out.println("\nFinal Answer: " + ans1);

        System.out.println("\n" + "=".repeat(50) + "\n");

        // Test 2: Budget cross - long query
        String longQuery = "Explain quantum computing in detail " + "with examples ".repeat(200);
        String ans2 = AgentOrchestrator.handleRequest("user456", longQuery);
        System.out.println("\nFinal Answer: " + ans2);
    }
}
