package pro.sky.telegrambot.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

public interface NotificationTaskService {
    void saveNotificationToDb(long chatId, String text, LocalDateTime date);
    void findNotificationFromDb();
}
