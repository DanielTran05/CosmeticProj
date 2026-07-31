package com.dtp.cosmemgt.core.commonService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MailService {
    final RestTemplate restTemplate;

    @Value("${brevo.api.key}")
    String API_KEY;
    @Value("${brevo.sender.email}")
    String SENDER_EMAIL;
    @Value("${brevo.sender.name}")
    String SENDER_NAME;
    @Value("${brevo.brevo_url}")
    String BREVO_URL;

    @Async
    public void sendEmail(String toEmail, String toName,
                           String subject, String htmlContent){
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", API_KEY);
            headers.set("accept", "application/json");

            Map<String, Object> body = new HashMap<>();
            body.put("sender", Map.of(
                    "name", SENDER_NAME,
                    "email", SENDER_EMAIL));
            body.put("to", List.of(Map.of("email", toEmail, "name", toName)));
            body.put("subject", subject);
            body.put("htmlContent", htmlContent);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            restTemplate.postForEntity(BREVO_URL, entity, String.class);

            log.info("Brevo has been sent to email: {}", toEmail);
        }catch(Exception e){
            log.error("Error when sending email to {}: {}" ,toEmail ,e.getMessage());
        }
    }

}