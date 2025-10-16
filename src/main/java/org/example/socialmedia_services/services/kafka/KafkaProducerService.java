package org.example.socialmedia_services.services.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendLikeEvent(String senderId, String receiverId, String senderName,
                              String senderProfileUrl, String postId, String postName) {
        try {
            Map<String, String> eventData = new HashMap<>();
            eventData.put("senderId", senderId);
            eventData.put("receiverId", receiverId);
            eventData.put("senderName", senderName);
            eventData.put("senderProfileUrl", senderProfileUrl);
            eventData.put("postId", postId);
            eventData.put("postName", postName);

            String message = objectMapper.writeValueAsString(eventData);
            kafkaTemplate.send("like-events", message);
            log.info("Like event sent: senderId={}, receiverId={}, postId={}", senderId, receiverId, postId);
        } catch (Exception e) {
            log.error("Failed to send like event: {}", e.getMessage(), e);
        }
    }

    public void sendCommentEvent(String senderId, String receiverId, String senderName,
                                  String senderProfileUrl, String postId, String postName,
                                  String commentId, String commentText) {
        try {
            Map<String, String> eventData = new HashMap<>();
            eventData.put("senderId", senderId);
            eventData.put("receiverId", receiverId);
            eventData.put("senderName", senderName);
            eventData.put("senderProfileUrl", senderProfileUrl);
            eventData.put("postId", postId);
            eventData.put("postName", postName);
            eventData.put("commentId", commentId);
            eventData.put("commentText", commentText);

            String message = objectMapper.writeValueAsString(eventData);
            kafkaTemplate.send("comment-events", message);
            log.info("Comment event sent: senderId={}, receiverId={}, postId={}, commentId={}",
                    senderId, receiverId, postId, commentId);
        } catch (Exception e) {
            log.error("Failed to send comment event: {}", e.getMessage(), e);
        }
    }

    public void sendFollowEvent(Long followId, String senderId, String receiverId, String senderName,
                                String senderProfileUrl) {
        try {
            Map<String, String> eventData = new HashMap<>();
            eventData.put("followId", String.valueOf(followId));
            eventData.put("senderId", senderId);
            eventData.put("receiverId", receiverId);
            eventData.put("senderName", senderName);
            eventData.put("senderProfileUrl", senderProfileUrl);

            String message = objectMapper.writeValueAsString(eventData);
            kafkaTemplate.send("follow-events", message);
            log.info("Follow event sent: followId={}, senderId={}, receiverId={}", followId, senderId, receiverId);
        } catch (Exception e) {
            log.error("Failed to send follow event: {}", e.getMessage(), e);
        }
    }
}
