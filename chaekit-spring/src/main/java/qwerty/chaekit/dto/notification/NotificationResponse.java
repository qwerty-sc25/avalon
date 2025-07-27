package qwerty.chaekit.dto.notification;

import qwerty.chaekit.domain.notification.entity.Notification;
import qwerty.chaekit.domain.notification.entity.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String extraData,
        String message,
        NotificationType type,
        boolean isRead
) {
    public static NotificationResponse of(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getExtraData(),
                notification.getMessage(),
                notification.getType(),
                notification.isRead()
        );
    }
} 