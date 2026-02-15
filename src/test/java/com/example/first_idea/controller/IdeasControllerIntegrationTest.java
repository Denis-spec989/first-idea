package com.example.first_idea.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class IdeasControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createIdea_shouldLogRequest(CapturedOutput output) throws Exception {
        mockMvc.perform(post("/ideas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ideaName": "My first idea", "ideaRate": 5}
                                """))
                .andExpect(status().isOk());

        assertThat(output).contains("Received idea request: ideaName=My first idea, ideaRate=5");
    }
}
