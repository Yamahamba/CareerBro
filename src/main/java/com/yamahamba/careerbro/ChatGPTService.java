package com.yamahamba.careerbro;

import com.plexpt.chatgpt.ChatGPT;
import com.plexpt.chatgpt.entity.chat.ChatCompletion;
import com.plexpt.chatgpt.entity.chat.ChatCompletionResponse;
import com.plexpt.chatgpt.entity.chat.Message;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatGPTService {

    private final ChatGPT chatGPT;
    private final String modelName;
    private final int maxTokens;
    private final double temperature;
    private final boolean isGpt5; // автоопределение модели
    private List<Message> messageHistory = new ArrayList<>();

    public ChatGPTService(String token) {
        Dotenv env = Dotenv.load();

        this.modelName   = env.get("OPENAI_MODEL", "gpt-4.1");
        this.maxTokens   = parseIntSafe(env.get("OPENAI_MAX_TOKENS"), 9000);
        this.temperature = parseDoubleSafe(env.get("OPENAI_TEMPERATURE"), 0.6);
        int timeoutSeconds = parseIntSafe(env.get("OPENAI_TIMEOUT_SECONDS"), 90);

        // если модель содержит "gpt-5", автоматически переключаемся в новый режим
        this.isGpt5 = modelName.startsWith("gpt-5");

        this.chatGPT = ChatGPT.builder()
                .apiKey(token)
                .apiHost("https://api.openai.com/")
                .timeout(timeoutSeconds)
                .build()
                .init();
    }

    public String sendMessage(String systemPrompt, String userMessage) {
        return sendMessage(systemPrompt, userMessage, this.maxTokens);
    }

    public String sendMessage(String systemPrompt, String userMessage, int maxTokensOverride) {
        Message system  = Message.ofSystem(systemPrompt);
        Message message = Message.of(userMessage);
        messageHistory = new ArrayList<>(Arrays.asList(system, message));
        return sendMessagesToChatGPT(maxTokensOverride);
    }

    public void setPrompt(String systemPrompt) {
        Message system = Message.ofSystem(systemPrompt);
        messageHistory = new ArrayList<>(List.of(system));
    }

    public String addMessage(String userMessage) {
        messageHistory.add(Message.of(userMessage));
        return sendMessagesToChatGPT(this.maxTokens);
    }

    private String sendMessagesToChatGPT(int maxTokensToUse) {
        long start = System.currentTimeMillis();
        System.out.println(">>> [ChatGPT] model=" + modelName + (isGpt5 ? " (GPT-5 mode)" : " (GPT-4 mode)") + ", maxTokens=" + maxTokensToUse);

        ChatCompletion.ChatCompletionBuilder builder = ChatCompletion.builder()
                .model(modelName)
                .messages(messageHistory)
                .temperature(temperature);

        // GPT-5.x не принимает max_tokens, поэтому пропускаем
        if (!isGpt5) {
            builder.maxTokens(maxTokensToUse);
        }

        ChatCompletion chatCompletion = builder.build();
        ChatCompletionResponse response;

        try {
            response = chatGPT.chatCompletion(chatCompletion);
        } catch (Exception e) {
            System.err.println("⚠️ Ошибка ChatGPT: " + e.getMessage());
            if (!modelName.equals("gpt-4.1")) {
                System.out.println("↩️ Переход на запасную модель: gpt-4.1");
                return fallbackTo4dot1();
            }
            return "Ошибка при обращении к ChatGPT: " + e.getMessage();
        }

        long took = System.currentTimeMillis() - start;
        Message answer = response.getChoices().get(0).getMessage();
        messageHistory.add(answer);

        System.out.printf("[ChatGPT ✅ %s] ответ за %.2f с%n", modelName, took / 1000.0);
        return answer.getContent();
    }

    private String fallbackTo4dot1() {
        try {
            ChatCompletion chatCompletion = ChatCompletion.builder()
                    .model("gpt-4.1")
                    .messages(messageHistory)
                    .maxTokens(maxTokens)
                    .temperature(temperature)
                    .build();

            ChatCompletionResponse response = chatGPT.chatCompletion(chatCompletion);
            Message answer = response.getChoices().get(0).getMessage();
            return answer.getContent();
        } catch (Exception e) {
            return "⚠️ Ошибка при fallback: " + e.getMessage();
        }
    }

    private int parseIntSafe(String raw, int def) {
        try { return raw == null ? def : Integer.parseInt(raw.trim()); }
        catch (Exception e) { return def; }
    }

    private double parseDoubleSafe(String raw, double def) {
        try { return raw == null ? def : Double.parseDouble(raw.trim()); }
        catch (Exception e) { return def; }
    }
}