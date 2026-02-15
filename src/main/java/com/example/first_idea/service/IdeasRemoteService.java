package com.example.first_idea.service;

import com.example.first_idea.controller.IdeasController.IdeaRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class IdeasRemoteService {

    private static final Logger log = LoggerFactory.getLogger(IdeasRemoteService.class);

    private final WebClient webClient;

    public IdeasRemoteService(WebClient webClient) {
        this.webClient = webClient;
    }

    public void createRemoteIdea(IdeaRequest request) {
        log.info("Calling remote /ideas with ideaName={}, ideaRate={}", request.ideaName(), request.ideaRate());
        webClient.post()
                .uri("/ideas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .block();
        log.info("Remote /ideas call completed");
    }
}
