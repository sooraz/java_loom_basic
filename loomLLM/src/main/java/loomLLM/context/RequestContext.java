package loomLLM.context;

public record RequestContext(String traceId, String userId, long startTime) {
    public static final ScopedValue<RequestContext> CTX = ScopedValue.newInstance();

    public static RequestContext current() {
        return CTX.get();
    }
}
