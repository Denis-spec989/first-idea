package com.example.first_idea.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ideas")
public class IdeasController {

    private static final Logger log = LoggerFactory.getLogger(IdeasController.class);

    public record IdeaRequest(String ideaName, int ideaRate) {}

    @PostMapping
    public void createIdea(@RequestBody IdeaRequest request) {
        log.info("Received idea request: ideaName={}, ideaRate={}", request.ideaName(), request.ideaRate());
    }
}
