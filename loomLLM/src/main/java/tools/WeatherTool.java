package tools;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import loomLLM.context.RequestContext;

public class WeatherTool {
    // Free API - key 
    private static final String URL = "https://wttr.in/Visakhapatnam?format=3";
    private static final HttpClient client = HttpClient.newHttpClient();

    public static String call() throws Exception {
        var ctx = RequestContext.current();
        System.out.println("[" + ctx.traceId() + "] WeatherTool: Starting");

        HttpRequest req = HttpRequest.newBuilder()
           .uri(URI.create(URL))
           .timeout(Duration.ofSeconds(2)) // Tool level timeout
           .GET()
           .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        System.out.println("[" + ctx.traceId() + "] WeatherTool: Done " + res.statusCode());
        return res.body(); 
    }
}