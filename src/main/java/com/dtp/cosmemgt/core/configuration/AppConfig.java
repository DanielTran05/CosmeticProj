package com.dtp.cosmemgt.core.configuration;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppConfig {
    @Value("${cloudinary.cloud_name}")
    String CLOUD_NAME;
    @Value("${cloudinary.api_key}")
    String API_KEY;
    @Value("${cloudinary.api_secret}")
    String API_SECRET;

    @Bean
    public Cloudinary cloudinary(){
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", CLOUD_NAME,
                "api_key", API_KEY,
                "api_secret", API_SECRET,
                "secure", true));
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper mapper(){
        return new ObjectMapper();
    }
}
