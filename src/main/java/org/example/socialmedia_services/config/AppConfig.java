package org.example.socialmedia_services.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    // Fixed Redis properties
    @Value("${app.REDIS_HOST}")
    private String redisHost;

    @Value("${app.REDIS_PORT}")
    private int redisPort;

    @Value("${app.REDIS_AUTH}")
    private String redisAuth;

    @Value("${app.EXPIRATION_TIME}")
    private long expirationTime;

    // Fixed SMTP properties
    @Value("${app.smtp_host}")
    private String smtpHost;

    @Value("${app.smtp_port}")
    private int smtpPort;

    @Value("${app.smtp_username}")
    private String smtpUsername;

    @Value("${app.smtp_password}")
    private String smtpPassword;

    // Getters remain the same
    public String getJwtSecret() {
        return jwtSecret;
    }

    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }


    public String getRedisHost() {
        return redisHost;
    }
    public int getRedisPort() {
        return redisPort;
    }
    public String getRedisAuth() {
        return redisAuth;
    }
    public long getExpirationTime() {
        return expirationTime;
    }

    public String getSmtpHost() {
        return smtpHost;
    }
    public int getSmtpPort() {
        return smtpPort;
    }
    public String getSmtpUsername() {
        return smtpUsername;
    }
    public String getSmtpPassword() {
        return smtpPassword;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
