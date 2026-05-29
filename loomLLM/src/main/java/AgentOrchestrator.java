
import tools.*;
import java.time.Instant;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Subtask;

import loomLLM.context.RequestContext;

import java.util.UUID;

public class AgentOrchestrator {

    // Main entry - 1 user request
    public static String handleUserQuery(String userId, String question) throws Exception {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        RequestContext ctx = new RequestContext(traceId, userId, System.currentTimeMillis());

        return ScopedValue.where(RequestContext.CTX, ctx).call(() -> {
            System.out.println("\n[" + traceId + "] === Starting request: " + question + " ===");
            return executeAgenticFlow(question);
        });
    }

    private static String executeAgenticFlow(String question) throws Exception {
        var ctx = RequestContext.current();

        // STRATEGY: ShutdownOnFailure - 1 tool fail = all cancel
        // Use case: Cost save. LLM call not wasted if DB fail
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

            // Step 1: Parallel Tools
            Subtask<String> weatherF = scope.fork(WeatherTool::call);
            Subtask<String> dbF = scope.fork(() -> DbTool.call(ctx.userId()));
            Subtask<String> calcF = scope.fork(() -> CalculatorTool.call("2+2"));
            Subtask<String> searchF = scope.fork(() -> WebSearchTool.call(question));

            // Timeout: 1.5s 
            scope.joinUntil(Instant.now().plusMillis(1500));
            scope.throwIfFailed(); // throw exception if fail

            // Step 2: Gather results
            String weather = weatherF.get();
            String db = dbF.get();
            String calc = calcF.get();
            String search = searchF.get();

            System.out.println("[" + ctx.traceId() + "] All tools success. Calling LLM...");

            // Step 3: Sequential
            String finalAnswer = callLlmWithRetry(weather, db, calc, search, question);

            long totalTime = System.currentTimeMillis() - ctx.startTime();
            System.out.println("[" + ctx.traceId() + "] === Done in " + totalTime + "ms ===");

            return finalAnswer;

        } catch (Exception e) {
            var ctx2 = RequestContext.current();
            long totalTime = System.currentTimeMillis() - ctx2.startTime();
            System.out.println("[" + ctx2.traceId() + "] === FAILED in " + totalTime + "ms: " + e.getMessage() + " ===");
            throw e;
        }
    }

    // Retry logic with Loom - Simple and clean
    private static String callLlmWithRetry(String... data) throws Exception {
        int maxRetries = 2;
        Exception lastEx = null;

        for (int i = 0; i < maxRetries; i++) {
            try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                Subtask<String> llmF = scope.fork(() -> LlmTool.call(String.join(" | ", data)));
                scope.joinUntil(Instant.now().plusMillis(800)); // LLM timeout 800ms
                scope.throwIfFailed();
                return llmF.get(); // Success
            } catch (Exception e) {
                lastEx = e;
                System.out.println("[Retry] LLM attempt " + (i+1) + " failed: " + e.getMessage());
                if (i < maxRetries - 1) Thread.sleep(100); // Backoff
            }
        }
        throw new RuntimeException("LLM failed after " + maxRetries + " retries", lastEx);
    }

    // Test
    public static void main(String[] args) throws Exception {
        // Test 1: All success
        String ans1 = handleUserQuery("user123", "What to do today?");
        System.out.println("ANSWER: " + ans1);

        // Test 2: DB fail test
        try {
            handleUserQuery("fail", "Test fail");
        } catch (Exception e) {
            System.out.println("Expected failure: " + e.getMessage());
        }
    }
}