package com.javarush.telegram;

import com.plexpt.chatgpt.ChatGPT;
import com.plexpt.chatgpt.entity.chat.ChatCompletion;
import com.plexpt.chatgpt.entity.chat.ChatCompletionResponse;
import com.plexpt.chatgpt.entity.chat.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatGPTService {
    private final ChatGPT chatGPT;
    private List<Message> messageHistory = new ArrayList<>();

    public ChatGPTService(String token) {
        // Используем чистый токен без прокси и без извращений
        this.chatGPT = ChatGPT.builder()
                .apiKey(token)
                .apiHost("https://api.openai.com/") // важно: / на конце
                .build()
                .init();
    }

    /**
     * Одиночный запрос без сохранения истории
     */
    public String sendMessage(String prompt, String question) {
        Message system = Message.ofSystem(prompt);
        Message message = Message.of(question);
        messageHistory = new ArrayList<>(Arrays.asList(system, message));

        return sendMessagesToChatGPT();
    }

    /**
     * Установить системный prompt (контекст)
     */
    public void setPrompt(String prompt) {
        Message system = Message.ofSystem(prompt);
        messageHistory = new ArrayList<>(List.of(system));
    }

    /**
     * Добавить пользовательское сообщение в диалог и получить ответ
     */
    public String addMessage(String question) {
        Message message = Message.of(question);
        messageHistory.add(message);
        return sendMessagesToChatGPT();
    }

    /**
     * Отправить весь текущий диалог и получить ответ от GPT
     */
    private String sendMessagesToChatGPT() {
        ChatCompletion chatCompletion = ChatCompletion.builder()
                .model(ChatCompletion.Model.GPT_3_5_TURBO) // или GPT4, если у тебя доступ
                .messages(messageHistory)
                .maxTokens(1500)
                .temperature(0.9)
                .build();

        ChatCompletionResponse response = chatGPT.chatCompletion(chatCompletion);

        Message answer = response.getChoices().get(0).getMessage();
        messageHistory.add(answer);

        return answer.getContent();
    }
}
