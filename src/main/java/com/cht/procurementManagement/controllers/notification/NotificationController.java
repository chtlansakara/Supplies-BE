package com.cht.procurementManagement.controllers.notification;

import com.cht.procurementManagement.dto.NotificationDto;
import com.cht.procurementManagement.entities.User;
import com.cht.procurementManagement.services.notification.NotificationService;
import com.cht.procurementManagement.utils.SseEmitterRegistry;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import org.springframework.security.core.Authentication;
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final SseEmitterRegistry sseEmitterRegistry;
    public NotificationController(NotificationService notificationService, SseEmitterRegistry sseEmitterRegistry) {
        this.notificationService = notificationService;
        this.sseEmitterRegistry = sseEmitterRegistry;
    }


    //SSE Stream endpoint for Angular to connect on login
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(Authentication authentication){
        User user =(User) authentication.getPrincipal();
        return sseEmitterRegistry.register(user.getId());
    }

//    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public SseEmitter stream(@AuthenticationPrincipal UserDetails userDetails){
//        User user =(User)  userDetails;
//        return sseEmitterRegistry.register(user.getId());
//    }

    //get existing notifications when the page load
    @GetMapping
    public ResponseEntity<List<NotificationDto>> getAll(
        @AuthenticationPrincipal UserDetails userDetails){
        User user = (User) userDetails;
        return ResponseEntity.ok(notificationService.getNotificationsByUserId(user.getId()));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id){
        notificationService.markRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllRead(
            @AuthenticationPrincipal UserDetails userDetails){
        User user = (User) userDetails;
        notificationService.markAllRead(user.getId());
        return ResponseEntity.ok().build();
    }

}
