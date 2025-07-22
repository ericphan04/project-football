package com.swp.myleague.utils.openai_matchevent;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OpenAiService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    public String askChatGPT(String userMessage) {
        // Tạo prompt
        ChatMessage user = new ChatMessage("user", userMessage);
        ChatRequest request = new ChatRequest(model, List.of(user));

        // Header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<ChatRequest> httpEntity = new HttpEntity<>(request, headers);

        // Gửi request
        ResponseEntity<ChatResponse> response = restTemplate.postForEntity(
            apiUrl, httpEntity, ChatResponse.class
        );

        // Trả về kết quả
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody().getChoices().get(0).getMessage().getContent();
        } else {
            throw new RuntimeException("Failed to get response from ChatGPT");
        }
    }
}
