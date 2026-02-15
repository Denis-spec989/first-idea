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
import org.springframework.boot.web.server.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(OutputCaptureExtension.class)
class ProxyIdeasIntegrationTest {

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
    void shouldProxyRequestFromInstanceBToInstanceA(CapturedOutput output) throws Exception {
        // Start Instance B on a random port, pointing to Instance A
        instanceBContext = SpringApplication.run(FirstIdeaApplication.class,
                "--server.port=0",
                "--ideas.remote.base-url=http://localhost:" + instanceAPort);
        int instanceBPort = ((ServletWebServerApplicationContext) instanceBContext)
                .getWebServer().getPort();

        // Call Instance B's /remote-ideas endpoint
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + instanceBPort + "/remote-ideas"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{\"ideaName\": \"Proxied idea\", \"ideaRate\": 9}"))
                .build();

        // This will block ~10 seconds (the sleep in Instance A's /ideas)
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);

        // Instance B logs: proxy service called remote
        assertThat(output).contains("Calling remote /ideas with ideaName=Proxied idea, ideaRate=9");
        assertThat(output).contains("Remote /ideas call completed");

        // Instance A logs: the actual endpoint processed the request
        assertThat(output).contains("Received idea request: ideaName=Proxied idea, ideaRate=9");
        assertThat(output).contains("Starting 10 second pause...");
        assertThat(output).contains("Pause finished, idea created");
    }
}
