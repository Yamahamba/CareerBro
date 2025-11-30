package com.yamahamba.careerbro;

import java.util.StringJoiner;

public class PromptManager {

    /**
     * Склеивает несколько промптов в один.
     * Аргументы — относительные пути внутри resources/prompts (БЕЗ .txt).
     * Например: "base/gpt", "partials/rus_ux_rules", "modes/support"
     */
    public static String compose(String... parts) {
        StringJoiner joiner = new StringJoiner("\n\n---\n\n");
        for (String p : parts) {
            String text = MultiSessionTelegramBot.loadPrompt(p); // уже есть утилита загрузки
            if (text != null && !text.isBlank()) {
                joiner.add(text.trim());
            }
        }
        return joiner.toString();
    }
}