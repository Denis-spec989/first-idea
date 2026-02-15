package com.example.first_idea.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.servlet.context.ServletWebServerApplicationContext;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(OutputCaptureExtension.class)
class GracefulShutdownIntegrationTest {

    @Autowired
    private ServletWebServerApplicationContext serverContext;

    @LocalServerPort
    private int port;

    @Test
    void shouldCompleteActiveRequestBeforeShutdown(CapturedOutput output) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/ideas"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{\"ideaName\": \"Graceful test\", \"ideaRate\": 7}"))
                .build();

        // Send request asynchronously — it will be stuck in the 10s pause
        CompletableFuture<HttpResponse<String>> future =
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        // Give Tomcat a moment to accept the request and enter the pause
        Thread.sleep(2_000);

        // Initiate graceful shutdown on the web server (like SIGTERM)
        WebServer webServer = serverContext.getWebServer();
        CountDownLatch shutdownLatch = new CountDownLatch(1);
        webServer.shutDownGracefully(result -> shutdownLatch.countDown());

        // The request should have completed before the shutdown finished
        HttpResponse<String> response = future.get(15, TimeUnit.SECONDS);
        shutdownLatch.await(15, TimeUnit.SECONDS);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(output).contains("Starting 10 second pause...");
        assertThat(output).contains("Pause finished, idea created");
    }
}
