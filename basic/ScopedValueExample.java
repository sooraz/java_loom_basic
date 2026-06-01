package base;

public class ScopedValueExample {
    
    private static final ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();
    
    static void toolA() {
        System.out.println("ToolA: RequestID = " + REQUEST_ID.get());
        toolB(); 
    }
    
    static void toolB() {
        System.out.println("ToolB: RequestID = " + REQUEST_ID.get());
    }
    
    public static void main(String[] args) {
        ScopedValue.where(REQUEST_ID, "req-abc-123").run(() -> {
        	//all virtaul
            System.out.println("Main: Starting request");
            toolA(); 
        });
        
        // System.out.println(REQUEST_ID.get()); // Error
    }
}
