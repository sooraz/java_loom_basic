package tools;

import loomLLM.context.RequestContext;

public class LlmTool {
    public static String call(String prompt) throws InterruptedException {
        var ctx = RequestContext.current();
        System.out.println("[" + ctx.traceId() + "] LlmTool: Processing " + prompt.length() + " chars");

        Thread.sleep(600); // LLM latency

        if (prompt.contains("error")) {
            throw new RuntimeException("LLM rate limit");
        }

        System.out.println("[" + ctx.traceId() + "] LlmTool: Done");
        return "Based on tools: User Yugi in Hyd, weather 28C. Suggest indoor activities.";
    }
}