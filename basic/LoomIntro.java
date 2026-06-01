package base;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class LoomIntro {
    
    static void blockingCall() {
        try {
            Thread.sleep(100); // 100ms block
        } catch (InterruptedException e) {}
    }
    
    public static void main(String[] args) throws Exception {
        int TASK_COUNT = 10_000;
        
        // Test 1: Platform Threads - Old Java
        System.out.println("Testing Platform Threads...");
        Instant start1 = Instant.now();
        try (var executor = Executors.newFixedThreadPool(200)) {
            var futures = IntStream.range(0, TASK_COUNT)
                .mapToObj(i -> executor.submit(LoomIntro::blockingCall))
                .toList();
            for (var f : futures) f.get();
        }
        long time1 = Duration.between(start1, Instant.now()).toMillis();
        System.out.println("200 Platform Threads: " + time1 + "ms");
        
        // Test 2: Virtual Threads - Loom
        System.out.println("\nTesting Virtual Threads...");
        Instant start2 = Instant.now();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = IntStream.range(0, TASK_COUNT)
                .mapToObj(i -> executor.submit(LoomIntro::blockingCall))
                .toList();
            for (var f : futures) f.get();
        }
        long time2 = Duration.between(start2, Instant.now()).toMillis();
        System.out.println("10k Virtual Threads: " + time2 + "ms");
        
        System.out.println("\nSpeed up: " + (time1 / time2) + "x faster");
    }
}
