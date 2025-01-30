package com.deepsalunkhee.cfdqServer.interceptors;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuthInterceptor.class);
    private static final String API_ENDPOINT = "https://codeforces.com/api";
    private static final String METHOD = "user.friends"; // Example method

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        String handle = request.getHeader("handle");
        String apiKey = request.getHeader("key");
        String apiSecret = request.getHeader("secret");

        logger.info("Handle: " + handle);
        logger.info("API Key: " + apiKey);
        logger.info("API Secret: " + apiSecret);

        if (handle == null || apiKey == null || apiSecret == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing authentication headers");
            return false;
        }

        Map<String, String> params = new TreeMap<>();
        params.put("apiKey", apiKey);
        params.put("handles", handle);
        params.put("time", String.valueOf(System.currentTimeMillis() / 1000));
        params.put("OnlyOnline", "true");

        // Random string for apiSig
        String randomString = UUID.randomUUID().toString().substring(0, 6);

        // Generate the apiSig
        String apiSig = generateApiSig(apiSecret, METHOD, params, randomString);
        params.put("apiSig", apiSig);

        // Build the full URL
        StringBuilder urlBuilder = new StringBuilder(API_ENDPOINT)
                .append("/")
                .append(METHOD)
                .append("?");
        params.forEach((key, value) -> urlBuilder.append(key).append("=").append(value).append("&"));
        String requestUrl = urlBuilder.substring(0, urlBuilder.length() - 1); // Remove trailing "&"

        // logger.info("Request URL: " + requestUrl);

        // Forward the request to the Codeforces API
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(requestUrl, String.class);

            // Parse the response JSON into CodeforcesResponse object
            ObjectMapper objectMapper = new ObjectMapper();
            CodeforcesResponse codeforcesResponse = objectMapper.readValue(responseEntity.getBody(),
                    CodeforcesResponse.class);

            // Access the "status" field

            String status = codeforcesResponse.getStatus();
            // logger.info("Response: " + codeforcesResponse.getResult());
            logger.info("Status: " + status);

            if (status.equals("OK")) {
                return true;
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid authentication headers");
                return false;
            }
        } catch (HttpClientErrorException e) {
            // Log the exception details for debugging
            e.printStackTrace();

            // In case of an error communicating with the external service, return
            // Unauthorized
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Authentication error with external service");
            return false; // Stop further processing
        }

    }

    private String generateApiSig(String apiSecret, String method, Map<String, String> params, String randomString)
            throws NoSuchAlgorithmException {
        // Sort parameters lexicographically
        Map<String, String> sortedParams = new TreeMap<>(params);

        // Build the parameter string
        StringBuilder paramString = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            paramString.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        paramString.deleteCharAt(paramString.length() - 1); // Remove trailing "&"

        // Construct the signature string
        String signatureString = randomString + "/" + method + "?" + paramString + "#" + apiSecret;

        // Hash the signature string using SHA-512
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        byte[] hashBytes = digest.digest(signatureString.getBytes(StandardCharsets.UTF_8));

        // Convert the hash to hexadecimal
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }

        // Return the final apiSig
        return randomString + hexString.toString();
    }

    public static class CodeforcesResponse {

        private String status;
        private List<String> result;
        private String comment;

        // Getters and Setters
        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public List<String> getResult() {
            return result;
        }

        public void setResult(List<String> result) {
            this.result = result;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
    }

}
