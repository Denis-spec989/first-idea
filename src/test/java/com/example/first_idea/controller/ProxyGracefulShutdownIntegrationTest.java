package com.example.first_idea.controller;

import com.example.first_idea.FirstIdeaApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

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
class ProxyGracefulShutdownIntegrationTest {

    @LocalServerPort
    private int instanceAPort;

    private ConfigurableApplicationContext instanceBContext;

    @AfterEach
    void tearDown() {
        if (instanceBContext != null) {
            instanceBContext.close();
        }
    }

    @Test
    void shouldCompleteProxyRequestWhenInstanceBReceivesSigterm(CapturedOutput output) throws Exception {
        // Start Instance B on a random port, pointing to Instance A
        instanceBContext = SpringApplication.run(FirstIdeaApplication.class,
                "--server.port=0",
                "--ideas.remote.base-url=http://localhost:" + instanceAPort);
        ServletWebServerApplicationContext instanceBServerContext =
                (ServletWebServerApplicationContext) instanceBContext;
        int instanceBPort = instanceBServerContext.getWebServer().getPort();

        // Send async request to Instance B's /remote-ideas
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + instanceBPort + "/remote-ideas"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{\"ideaName\": \"Shutdown proxy test\", \"ideaRate\": 3}"))
                .build();

        CompletableFuture<HttpResponse<String>> future =
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        // Wait 3 seconds, then SIGTERM Instance B
        Thread.sleep(3_000);

        WebServer instanceBWebServer = instanceBServerContext.getWebServer();
        CountDownLatch shutdownLatch = new CountDownLatch(1);
        instanceBWebServer.shutDownGracefully(result -> shutdownLatch.countDown());

        // Instance B should wait for the proxy call to finish before shutting down
        HttpResponse<String> response = future.get(30, TimeUnit.SECONDS);
        shutdownLatch.await(30, TimeUnit.SECONDS);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(output).contains("Remote /ideas call completed");
        assertThat(output).contains("Pause finished, idea created");
        assertThat(output).contains("Graceful shutdown complete");
    }
}
