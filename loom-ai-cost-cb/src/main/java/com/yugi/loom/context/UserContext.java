package com.yugi.loom.context;

public record UserContext(String userId, String traceId) {
    public static final ScopedValue<UserContext> CURRENT = ScopedValue.newInstance();

    public static UserContext current() {
        if (!CURRENT.isBound()) throw new IllegalStateException("No user context");
        return CURRENT.get();
    }
}
