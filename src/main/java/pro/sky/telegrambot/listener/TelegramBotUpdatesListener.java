package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.service.NotificationTaskService;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

        private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private final String START = "/start";
//    private final String RUSSIAN = "/russian";
    private static final Pattern PATTERN = Pattern.compile("([0-9.:\\s]{16})(\\s)([\\S+]+)");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final NotificationTaskService notificationTaskService;
    private final TelegramBot telegramBot;

    public TelegramBotUpdatesListener(NotificationTaskService notificationTaskService, TelegramBot telegramBot) {
        this.notificationTaskService = notificationTaskService;
        this.telegramBot = telegramBot;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

        @Override
        public int process (List < Update > updates) {
            try {
                updates.forEach(update -> {
                            logger.info("Processing update: {}", update);
                            if (update.message() == null) {
                                logger.info("Null message was sent");
                                return;
                            }
                            Long chatId = update.message().chat().id();
                            String messageText = update.message().text();
                            if (messageText.equals(START)) {
                                    String userName;
                                    if (update.message().chat().username() == null) {
                                        userName = update.message().chat().lastName() + " " +
                                                update.message().chat().firstName();
                                        startMessage(chatId, userName);
                                    } else {
                                        userName = update.message().chat().username();
                                        startMessage(chatId, userName);
                                        return;
                                    }
                            }
//                                case RUSSIAN:
//                                    String userNameRus;
//                                    if (update.message().chat().username() == null) {
//                                        userName = update.message().chat().lastName() + " " +
//                                                update.message().chat().firstName();
//                                        startMessageRussian(chatId, userName);
//                                    } else {
//                                        userName = update.message().chat().username();
//                                        startMessageRussian(chatId, userName);
//                                        return;
//                                    }
//                            }

                            Matcher matcher = PATTERN.matcher(messageText);
                            if (matcher.matches()) {
                                String date = matcher.group(1);
                                LocalDateTime dateTime = LocalDateTime.parse(date, FORMATTER);
                                if (dateTime.isBefore(LocalDateTime.now())) {
                                    sendMessage(chatId, "That time has passed!" +
                                            "\n Enter the date and time to be " +
                                            "\n For example like this: " +
                                            "\n 20.06.2024 13:00 Miroslav has a birthday , we should congratulate him!");
                                    logger.info("Date is before now {}", chatId);
                                    return;
                                }
                                String notification = matcher.group(3);
                                notificationTaskService.saveNotificationToDb(chatId, notification, dateTime);
                                logger.info("Notification was saved into DB {}", chatId);
                                sendMessage(chatId, "Message was complete"
                                +"\n I will definitely remind you ");
                            }
                            if (!matcher.matches()) {
                                sendMessage(chatId, "Попробуй еще, введены некорректные данные"
                                        +"\n Try again, incorrect data has been entered: " +
                                        "\n 12.12.2023 23:55 New Year, it's about now");
                            }

                        }
                );


            } catch (Exception e) {
                logger.error(e.getMessage(), e);


            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        }


        private void startMessage (Long chatId, String userName){
            logger.info("Was invoked method  startMessage {}", chatId);
            String responseMessage = "Hi ," + userName + "! \uD83D\uDE08" +
                    "\n Enter the date and time in format " +
                    "\ndd.MM.yyyy HH:mm " +
                    "\n and write a reminder message";
            sendMessage(chatId, responseMessage);

        }

//        private void startMessageRussian (Long chatId, String userName){
//            logger.info("Was invoked method  startMessageRussian {}", chatId);
//            String responseMessage = "Здорово ," + userName + "! \uD83D\uDE08" +
//                    "\n введи дату и время в формате " +
//                    "\ndd.MM.yyyy HH:mm " +
//                    "\n и напиши напоминалку";
//            sendMessage(chatId, responseMessage);
//
//        }

        private void sendMessage (Long chatId, String sendingMessage){
            logger.info("Was invoked method  startMessage");
            SendMessage sendMessage = new SendMessage(String.valueOf(chatId), sendingMessage);
            telegramBot.execute(sendMessage);
        }
    }

