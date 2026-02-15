package com.example.first_idea.controller;

import com.example.first_idea.controller.IdeasController.IdeaRequest;
import com.example.first_idea.service.IdeasRemoteService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/remote-ideas")
public class ProxyIdeasController {

    private final IdeasRemoteService remoteService;

    public ProxyIdeasController(IdeasRemoteService remoteService) {
        this.remoteService = remoteService;
    }

    @PostMapping
    public void createRemoteIdea(@RequestBody IdeaRequest request) {
        remoteService.createRemoteIdea(request);
    }
}
