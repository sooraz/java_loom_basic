package tools;

import loomLLM.context.RequestContext;

public class CalculatorTool {
    public static String call(String expression) {
        var ctx = RequestContext.current();
        System.out.println("[" + ctx.traceId() + "] CalculatorTool: " + expression);

        if (expression.contains("2+2")) return "4";
        return "42";
    }
}