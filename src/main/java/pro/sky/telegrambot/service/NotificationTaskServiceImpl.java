package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class NotificationTaskServiceImpl implements NotificationTaskService{
    private final NotificationTaskRepository notificationTaskRepository;
    private final TelegramBot telegramBot;

    public NotificationTaskServiceImpl(NotificationTaskRepository notificationTaskRepository, TelegramBot telegramBot) {
        this.notificationTaskRepository = notificationTaskRepository;
        this.telegramBot = telegramBot;
    }
    @Override
    public void saveNotificationToDb(long chatId, String text, LocalDateTime date){
        LocalDateTime time = date.truncatedTo(ChronoUnit.MINUTES);
        NotificationTask notificationTask = new NotificationTask(chatId,text,time);
        notificationTaskRepository.save(notificationTask);
    }
    @Scheduled(cron = "0 0/1 * * * *")
    @Override
    public void findNotificationFromDb() {
        LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        List<NotificationTask> tasks = notificationTaskRepository.findByDateTime(dateTime);
        tasks.forEach(t->{
            SendMessage sendNotification  = new SendMessage(t.getChatId(),"\"let me\" remind you that it is NECESSARY: \uD83D\uDC49 "
            +t.getText().toUpperCase());
            telegramBot.execute(sendNotification);
        });

    }
}
