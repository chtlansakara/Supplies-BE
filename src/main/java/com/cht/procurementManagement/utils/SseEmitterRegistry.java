package com.cht.procurementManagement.utils;

import com.cht.procurementManagement.dto.NotificationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class SseEmitterRegistry {

    private static final Logger log = LoggerFactory.getLogger(SseEmitterRegistry.class);
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    public SseEmitter register(Long userId){
        //remove any existing connection to the user
        SseEmitter existing = emitters.get(userId);
        if(existing != null){
            existing.complete();
        }

        //no time out
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        emitter.onCompletion( ()-> {
            emitters.remove(userId);
            log.info("SSE Connection completed for user {}", userId);
        });

        emitter.onTimeout( () -> {
            emitters.remove(userId);
            emitter.complete();
            log.info("SSE Connection timed out for user {}", userId);
        });

        emitter.onError(e -> {
            emitters.remove(userId);
            emitter.completeWithError(e);
            log.warn("SSE Connection error for user {}: {}",userId, e.getMessage());
        });

        emitters.put(userId, emitter);

        //send a confirmation event to Angular
        try{
            emitter.send(SseEmitter.event().name("CONNECTED").data("connected"));
        }catch(IOException e){
            emitters.remove(userId);
        }

        return emitter;
    }

    public void sendToUser(Long userId, NotificationDto dto){
        SseEmitter emitter = emitters.get(userId);
        if(emitter!= null){
            try{
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(dto, MediaType.APPLICATION_JSON));
            }catch (IOException | IllegalStateException e){
                emitters.remove(userId);
                emitter.complete();
                log.warn("Failed to send SSE to user {},removed emitter", userId);
            }
        }
    }

    public void sendToUsers(List<Long> userIds, NotificationDto dto){
        userIds.forEach(id ->sendToUser(id, dto));
    }


}
