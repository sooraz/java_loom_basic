package tools;

import loomLLM.context.RequestContext;

public class DbTool {
    public static String call(String userId) throws InterruptedException {
        var ctx = RequestContext.current();
        System.out.println("[" + ctx.traceId() + "] DbTool: Querying user " + userId);
        Thread.sleep(300); // DB latency simulate
        if (userId.equals("fail")) {
            throw new RuntimeException("DB connection timeout");
        }
        System.out.println("[" + ctx.traceId() + "] DbTool: Done");
        return "User: Yugi, City: Hyderabad, Tier: Premium";
    }
}
