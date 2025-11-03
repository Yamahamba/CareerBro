package com.javarush.telegram;

import io.github.cdimascio.dotenv.Dotenv;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.meta.TelegramBotsApi;

import java.util.ArrayList;

public class CareerBroApp extends MultiSessionTelegramBot {

    private static final Dotenv dotenv = Dotenv.load();

    private static final String TELEGRAM_BOT_NAME = dotenv.get("TELEGRAM_BOT_NAME");
    private static final String TELEGRAM_BOT_TOKEN = dotenv.get("TELEGRAM_BOT_TOKEN");
    private static final String OPEN_AI_TOKEN = dotenv.get("OPENAI_API_KEY");

    private final ChatGPTService chatGPT = new ChatGPTService(OPEN_AI_TOKEN);
    private DialogMode currentMode = null;
    private final ArrayList<String> list = new ArrayList<>();
    private UserInfo user;
    private int questionCount;

    public CareerBroApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    @Override
    public void onUpdateEventReceived(Update update) {
        String message = getMessageText();
        if (message == null) message = "";

        // Универсальный выход из любого режима
        if (message.equals("/exit")) {
            currentMode = DialogMode.MAIN;
            hideMainMenu();
            sendTextMessage("Выход из режима. Открываю главное меню…");
            // эмулируем /start
            showMainMenu("главное меню бота", "/start",
                    "создать резюме 📄", "/cv",
                    "сопроводительное письмо ✉️", "/cover",
                    "практика интервью 🎤", "/interview",
                    "психо-поддержка ❤️", "/support",
                    "вопросы GPT 🧠", "/gpt");
            return;
        }

        // Старт
        if (message.equals("/start")) {
            currentMode = DialogMode.MAIN;
            sendPhotoMessage("main");
            String text = safeLoadMessage("main", "Привет! Я карьeрный ассистент.");
            sendTextMessage(text);

            showMainMenu("главное меню бота", "/start",
                    "создать резюме 📄", "/cv",
                    "сопроводительное письмо ✉️", "/cover",
                    "практика интервью 🎤", "/interview",
                    "психо-поддержка ❤️", "/support",
                    "вопросы GPT 🧠", "/gpt");
            return;
        }

        // GPT
        if (message.equals("/gpt")) {
            currentMode = DialogMode.GPT;
            sendPhotoMessage("gpt");
            sendTextMessage(safeLoadMessage("gpt", "Спроси меня что угодно."));
            return;
        }

        if (currentMode == DialogMode.GPT && !isMessageCommand()) {
            String prompt = safeLoadPrompt("gpt", "Ты полезный ассистент.");
            Message msg = sendTextMessage("Подожди пару секунд, chatGPT думает...");
            String answer = chatGPT.sendMessage(prompt, message);
            updateTextMessage(msg, answer);
            return;
        }

        // CV
        if (message.equals("/cv")) {
            currentMode = DialogMode.CV;
            sendPhotoMessage("cv");
            user = new UserInfo();
            questionCount = 1;
            sendTextMessage(safeLoadMessage("cv", "Соберём CV по шагам."));
            sendTextMessage("Кто вы по профессии?");
            return;
        }

        if (currentMode == DialogMode.CV && !isMessageCommand()) {
            switch (questionCount) {
                case 1 -> {
                    user.profession = message;
                    questionCount = 2;
                    sendTextMessage("Сколько лет опыта?");
                }
                case 2 -> {
                    user.experienceYears = message;
                    questionCount = 3;
                    sendTextMessage("Ваши ключевые навыки?");
                }
                case 3 -> {
                    user.keySkills = message;
                    questionCount = 4;
                    sendTextMessage("Чем вы гордитесь в своей карьере?");
                }
                case 4 -> {
                    user.achievements = message;
                    questionCount = 5;
                    sendTextMessage("Куда хотите попасть? Какая цель поиска?");
                }
                case 5 -> {
                    user.jobTarget = message;
                    String about = user.toString();
                    String prompt = safeLoadPrompt("cv",
                            "Сгенерируй краткое и сильное CV по данным пользователя.");
                    Message msg = sendTextMessage("Генерирую резюме...");
                    String answer = chatGPT.sendMessage(prompt, about);
                    updateTextMessage(msg, answer);
                }
            }
            return;
        }

        // COVER
        if (message.equals("/cover")) {
            currentMode = DialogMode.COVER;
            sendPhotoMessage("cover");
            sendTextMessage(safeLoadMessage("cover", "Пришлите текст вакансии или ссылку."));
            sendTextMessage("Вставь текст вакансии или ссылку на неё:");
            return;
        }

        if (currentMode == DialogMode.COVER && !isMessageCommand()) {
            String prompt = safeLoadPrompt("cover",
                    "Составь персональное сопроводительное письмо под этот текст вакансии.");
            Message msg = sendTextMessage("Генерирую сопроводительное письмо...");
            String answer = chatGPT.sendMessage(prompt, message);
            updateTextMessage(msg, answer);
            return;
        }

        // INTERVIEW
        if (message.equals("/interview")) {
            currentMode = DialogMode.INTERVIEW;
            sendPhotoMessage("interview");
            sendTextMessage(safeLoadMessage("interview", "Начинаем интервью."));
            chatGPT.setPrompt(safeLoadPrompt("interview_hr",
                    "Ты HR-интервьюер. Задавай вопросы по очереди."));
            Message msg = sendTextMessage("Начнём интервью. Первый вопрос:");
            String first = chatGPT.addMessage("");
            updateTextMessage(msg, first);
            return;
        }

        if (currentMode == DialogMode.INTERVIEW && !isMessageCommand()) {
            Message msg = sendTextMessage("...");
            String reply = chatGPT.addMessage(message);
            updateTextMessage(msg, reply);
            return;
        }

        // SUPPORT — фикс: сразу отдаём первый ответ и удерживаем режим
        if (message.equals("/support")) {
            currentMode = DialogMode.SUPPORT;
            sendPhotoMessage("support");
            sendTextMessage(safeLoadMessage("support",
                    "Психо-поддержка включена. Напиши, что тревожит, или просто отправь сообщение."));
            // Инициализируем системный промпт и выдаём первый мягкий ответ
            chatGPT.setPrompt(safeLoadPrompt("support",
                    "Ты тёплый, поддерживающий собеседник. Короткие, добрые, по делу ответы."));
            Message msg = sendTextMessage("...");
            String support = chatGPT.addMessage("Мне тяжело, дай совет");
            updateTextMessage(msg, support);
            return;
        }

        if (currentMode == DialogMode.SUPPORT && !isMessageCommand()) {
            // Любой текст в режиме поддержки — продолжение тёплого диалога
            Message msg = sendTextMessage("...");
            String support = chatGPT.addMessage(message);
            updateTextMessage(msg, support);
            return;
        }

        // Фолбэк
        sendTextMessage("Вы написали: " + message);
        sendTextButtonsMessage("Выберите режим работы",
                "Старт", "/start",
                "GPT", "/gpt");
    }

    // Безопасные загрузчики, чтобы не падать, если нет файла support.txt/cover.txt и т.п.
    private String safeLoadMessage(String name, String fallback) {
        try {
            String s = loadMessage(name);
            return (s == null || s.isBlank()) ? fallback : s;
        } catch (Exception e) {
            return fallback;
        }
    }

    private String safeLoadPrompt(String name, String fallback) {
        try {
            String s = loadPrompt(name);
            return (s == null || s.isBlank()) ? fallback : s;
        } catch (Exception e) {
            return fallback;
        }
    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new CareerBroApp());
    }
}