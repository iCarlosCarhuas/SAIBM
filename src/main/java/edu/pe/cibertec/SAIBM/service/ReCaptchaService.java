package edu.pe.cibertec.SAIBM.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ReCaptchaService {

    @Value("${recaptcha.secret}")
    private String recaptchaSecret;

    public boolean validateCaptcha(String response) {
        String url = "https://www.google.com/recaptcha/api/siteverify"
                + "?secret=" + recaptchaSecret
                + "&response=" + response;

        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> googleResponse = restTemplate.postForObject(url, null, Map.class);
        return (Boolean) googleResponse.get("success");
    }
}
