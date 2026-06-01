package base;

import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Subtask;

public class ToolCallExample {
    
    // Tool 1: Weather API - 200ms =
    static String callWeatherTool() throws InterruptedException {
        Thread.sleep(200);
        return "Weather: 25C";
    }
    
    // Tool 2: DB Query - 300ms
    static String callDatabaseTool() throws InterruptedException {
        Thread.sleep(300);
        return "User: Yugi from Hyd";
    }
    
    // Tool 3: LLM call - 500ms
    static String callLlmTool() throws InterruptedException {
        Thread.sleep(500);
        throw new RuntimeException("LLM timeout!");
    }
    
    public static void main(String[] args) throws Exception {
        System.out.println("Starting 3 tools parallel...");
        long start = System.currentTimeMillis();
        
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            Subtask<String> weather = scope.fork(ToolCallExample::callWeatherTool);
            Subtask<String> db = scope.fork(ToolCallExample::callDatabaseTool);
            Subtask<String> llm = scope.fork(ToolCallExample::callLlmTool);
            
            scope.join(); 
            scope.throwIfFailed(); 
            
            String result = weather.get() + " | " + db.get() + " | " + llm.get();
            System.out.println("All success: " + result);
            
        } catch (Exception e) {
            long end = System.currentTimeMillis();
            System.out.println("FAILED in " + (end - start) + "ms");
            System.out.println("Error: " + e.getMessage());
        }
    }
}
