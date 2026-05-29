package tools;

import loomLLM.context.RequestContext;

public class WebSearchTool {
    public static String call(String query) throws InterruptedException {
        var ctx = RequestContext.current();
        System.out.println("[" + ctx.traceId() + "] WebSearchTool: " + query);

        Thread.sleep(400); // Search latency

        System.out.println("[" + ctx.traceId() + "] WebSearchTool: Done");
        return "Top result: Project Loom makes Java async easy";
    }
}